package com.github.keenon.lense.examples.ner;

import com.github.keenon.lense.convenience.StaticBatchLense;
import com.github.keenon.lense.examples.util.GNUPlot;
import com.github.keenon.lense.gameplay.Game;
import com.github.keenon.lense.gameplay.utilities.UncertaintyUtilityWithoutTime;
import com.github.keenon.lense.storage.ModelQueryRecord;
import com.github.keenon.loglinear.model.GraphicalModel;
import com.github.keenon.loglinear.storage.ModelBatch;
import com.github.keenon.lense.human_source.MTurkHumanSource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by keenon on 10/7/15.
 *
 * Runs a simple example based on CoNLL NER data.
 */
public abstract class NERStaticBatch extends StaticBatchLense {
    public String[] CoNLLTags = new String[] {
            "PER",
            "LOC",
            "ORG",
            "O"
    };

    String[] CoNLLTagDescriptions = new String[] {
            "Person's name (eg \"Dave\", \"Count van Troppe\")",
            "Location (eg \"Germany\", \"Britain\", \"Paris\", etc)",
            "Organization (eg \"European Union\", \"Microsoft\", etc)",
            "None of the above"
    };

    @Override
    public String getModelDumpFileLocation() {
        return "src/main/resources/ner-dump.txt";
    }

    @Override
    public ModelBatch createInitialModelBatch() {
        ModelBatch batch = new ModelBatch();

        int batchSize = 1000;

        try {
            BufferedReader br = new BufferedReader(new FileReader("src/main/resources/ner/conll.iob.4class.train"));

            List<String> tokens = new ArrayList<>();
            List<String> poss = new ArrayList<>();
            List<String> chunks = new ArrayList<>();
            List<String> tags = new ArrayList<>();

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length != 4) continue;

                String token = parts[0];
                String pos = parts[1];
                String chunk = parts[2];
                String tag = parts[3];

                tokens.add(token);
                poss.add(pos);
                chunks.add(chunk);
                tags.add(tag);

                // Terminate the sentence
                if (token.equals(".")) {
                    if (!tags.contains("MISC")) {
                        GraphicalModel model = makeModel(tokens, poss, chunks, tags);
                        featurize(model);
                        batch.add(model);
                    }

                    if (batch.size() >= batchSize) {
                        // quit early
                        br.close();
                        return batch;
                    }

                    tokens.clear();
                    poss.clear();
                    chunks.clear();
                    tags.clear();
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return batch;
    }

    private GraphicalModel makeModel(List<String> tokens, List<String> poss, List<String> chunks, List<String> tags) {
        GraphicalModel model = new GraphicalModel();

        for (int i = 0; i < tokens.size(); i++) {
            Map<String,String> metadata = model.getVariableMetaDataByReference(i);

            // Add info for featurizing later
            metadata.put("TOKEN", tokens.get(i));
            metadata.put("POS", poss.get(i));
            metadata.put("CHUNK", chunks.get(i));
            metadata.put("TAG", tags.get(i));

            // Add the query data
            JSONObject queryData = new JSONObject();

            String html = "What kind of thing is the highlighted word?<br>";
            html +="<span class=\"content\">";
            for (int j = 0; j < tokens.size(); j++) {
                if (j == i) html +="<span class=\"focus\">";
                html += " "+tokens.get(j);
                if (j == i) html +="</span>";
            }
            html +="</span>";
            queryData.put("html", html);

            JSONArray choices = new JSONArray();
            for (String tag : CoNLLTagDescriptions) {
                choices.add(tag);
            }

            queryData.put("choices", choices);

            metadata.put(MTurkHumanSource.QUERY_JSON, queryData.toJSONString());
        }

        return model;
    }

    @Override
    public void featurize(GraphicalModel model) {
        CoNLLFeaturizer.annotate(model, CoNLLTags, namespace);
    }

    UncertaintyUtilityWithoutTime utility = new UncertaintyUtilityWithoutTime();

    @Override
    public double utility(Game game) {
        UncertaintyUtilityWithoutTime.humanQueryCost = 0.02;
        return utility.apply(game);
    }

    @Override
    public void dumpGame(GameRecord gameRecord, BufferedWriter bw) throws IOException {
        Game game = gameRecord.game;

        bw.write("Game Results:\n\n");
        int[] guesses = game.getMAP();
        int[] queries = new int[guesses.length];
        for (Game.Event e : game.stack) {
            if (e instanceof Game.QueryLaunch) {
                Game.QueryLaunch ql = (Game.QueryLaunch)e;
                queries[ql.variable]++;
            }
        }
        bw.append("TOKEN\tTAG\tGUESS\nQUERIES\n-------------\n");
        for (int i = 0; i < game.model.getVariableSizes().length; i++) {
            if (!game.model.getVariableMetaDataByReference(i).containsKey("TOKEN")) break;
            bw.append(game.model.getVariableMetaDataByReference(i).get("TOKEN"));
            bw.append("\t");
            bw.append(game.model.getVariableMetaDataByReference(i).get("TAG"));
            bw.append("\t");
            bw.append(CoNLLTags[guesses[i]]);
            bw.append("\t");
            bw.append(Integer.toString(queries[i]));
            bw.append("\n");
        }

        bw.write("\nGame History:\n\n");
        List<Game.Event> events = new ArrayList<>();
        events.addAll(game.stack);
        game.resetEvents();

        DecimalFormat df = new DecimalFormat("0.000");

        for (Game.Event e : events) {
            bw.write("-----------\n");
            bw.append("TOKEN\tDIST\tTAG\tGUESS\n-------------\n");
            double[][] marginals = game.getMarginals();
            int[] map = game.getMAP();
            for (int i = 0; i < game.model.getVariableSizes().length; i++) {
                if (!game.model.getVariableMetaDataByReference(i).containsKey("TOKEN")) break;
                String token = game.model.getVariableMetaDataByReference(i).get("TOKEN");
                bw.write(token);
                bw.write("\t[");
                for (int j = 0; j < marginals[i].length; j++) {
                    if (j != 0) bw.write(",");
                    bw.write(CoNLLTags[j]);
                    bw.write("=");
                    bw.write(df.format(marginals[i][j]));
                }
                bw.write("]\t");
                bw.append(game.model.getVariableMetaDataByReference(i).get("TAG"));
                bw.append("\t");
                bw.write(CoNLLTags[map[i]]);
                bw.append("\n");
            }
            bw.write("\n-----------");
            bw.write("\nCurrent job postings un-answered: "+game.jobPostings.size());
            bw.write("\nCurrent available humans: "+game.availableHumans.size());
            bw.write("\nCurrent in-flight queries: ");
            for (Game.QueryLaunch ql : game.inFlightRequests) {
                bw.write("\n\t"+game.model.getVariableMetaDataByReference(ql.variable).get("TOKEN"));
            }
            if (e.isGameplayerInitiated()) bw.write("\n-----------\nMove choice:\n");
            else bw.write("\n-----------\nNext event:\n");
            // Debug the actual event
            if (e instanceof Game.QueryLaunch) {
                Game.QueryLaunch ql = (Game.QueryLaunch)e;
                bw.write("Launch query on token "+ql.variable+" \""+game.model.getVariableMetaDataByReference(ql.variable).get("TOKEN")+"\"");
            }
            else if (e instanceof Game.HumanJobPosting) {
                bw.write("Making job posting");
            }
            else if (e instanceof Game.QueryResponse) {
                Game.QueryResponse qr = (Game.QueryResponse)e;
                Game.QueryLaunch ql = qr.request;
                bw.write("Got response on token "+ql.variable+" \""+game.model.getVariableMetaDataByReference(ql.variable).get("TOKEN")+"\" = "+CoNLLTags[qr.response]);
            }
            else {
                bw.write(e.toString());
            }
            bw.write("\n");
            e.push(game);
        }
    }

    private static class CheckpointRecord {
        double avgF1;
        double avgQueries;
        Map<String,Double> tagF1 = new HashMap<>();
        Map<String,Double> tagQueries = new HashMap<>();
    }

    List<CheckpointRecord> checkpointRecords = new ArrayList<>();

    @Override
    public void checkpoint(List<GameRecord> gamesFinished) {
        System.err.println("Finished "+gamesFinished.size()+" games");

        String subfolder = getThisRunPerformanceReportSubFolder();

        // Evaluation method lifted from the CoNLL 2004 perl script

        Map<String,Double> correctChunk = new HashMap<>();
        Map<String,Double> foundCorrect = new HashMap<>();
        Map<String,Double> foundGuessed = new HashMap<>();
        double correct = 0.0;
        double total = 0.0;

        Map<String,Double> queriesPerType = new HashMap<>();
        double queries = 0.0;
        double delays = 0.0;
        int totalTokens = 0;

        synchronized (gamesFinished) {
            for (GameRecord record : gamesFinished) {
                int[] guesses = record.result;
                String[] nerGuesses = new String[guesses.length];
                for (int i = 0; i < guesses.length; i++) {
                    nerGuesses[i] = CoNLLTags[guesses[i]];
                    String trueValue = record.game.model.getVariableMetaDataByReference(i).get("TAG");
                    if (nerGuesses[i].equals(trueValue)) {
                        correct++;
                        correctChunk.put(nerGuesses[i], correctChunk.getOrDefault(nerGuesses[i], 0.) + 1);
                    }
                    total++;
                    foundCorrect.put(trueValue, foundCorrect.getOrDefault(trueValue, 0.) + 1);
                    foundGuessed.put(nerGuesses[i], foundGuessed.getOrDefault(nerGuesses[i], 0.) + 1);
                }

                for (Game.Event e : record.game.stack) {
                    if (e instanceof Game.QueryLaunch) {
                        queries++;
                        Game.QueryLaunch ql = (Game.QueryLaunch) e;
                        String trueValue = record.game.model.getVariableMetaDataByReference(ql.variable).get("TAG");
                        queriesPerType.put(trueValue, queriesPerType.getOrDefault(trueValue, 0.0) + 1.0);
                    }
                }

                delays += record.game.timeSinceGameStart;

                totalTokens += nerGuesses.length;
            }
        }

        CheckpointRecord checkpointRecord = new CheckpointRecord();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(subfolder+"/results.txt"));
            bw.write("Finished "+gamesFinished.size()+" games\n");

            bw.write("\nSystem results:\n");

            bw.write("\nAccuracy: " + (correct / total) + "\n");

            double avgF1 = 0.0;

            for (String tag : CoNLLTags) {
                double precision = foundGuessed.getOrDefault(tag, 0.0) == 0 ? 0.0 : correctChunk.getOrDefault(tag, 0.0) / foundGuessed.get(tag);
                double recall = foundCorrect.getOrDefault(tag, 0.0) == 0 ? 0.0 : correctChunk.getOrDefault(tag, 0.0) / foundCorrect.get(tag);
                double f1 = (precision + recall == 0.0) ? 0.0 : (precision * recall * 2) / (precision + recall);
                bw.write("\n"+tag+" ("+foundCorrect.getOrDefault(tag, 0.0).intValue()+")");
                bw.write("\n\tP:" + precision + " (" + correctChunk.getOrDefault(tag, 0.0).intValue() + "/" + foundGuessed.getOrDefault(tag, 0.0).intValue() + ")");
                bw.write("\n\tR:"+recall+" ("+correctChunk.getOrDefault(tag, 0.0).intValue()+"/"+foundCorrect.getOrDefault(tag, 0.0).intValue()+")");
                bw.write("\n\tF1:" + f1);

                checkpointRecord.tagF1.put(tag, f1);

                if (!tag.equals("O")) {
                    avgF1 += f1;
                }
            }
            avgF1 /= 3;
            bw.write("\n\nAvg F1: "+avgF1);

            checkpointRecord.avgF1 = avgF1;
            checkpointRecord.avgQueries = (queries/totalTokens);

            bw.write("\n\nQueries: ");
            bw.write("\nQ/tok: "+(queries/totalTokens)+" ("+queries+"/"+totalTokens+")");
            for (String tag : CoNLLTags) {
                bw.write("\n"+tag+" ("+foundCorrect.getOrDefault(tag, 0.0).intValue()+")");
                double qs = foundCorrect.getOrDefault(tag, 0.0) == 0 ? 0.0 : queriesPerType.getOrDefault(tag, 0.0) / foundCorrect.get(tag);
                bw.write("\n\tQ/tok: "+qs+" ("+queriesPerType.getOrDefault(tag, 0.0)+"/"+foundCorrect.get(tag)+")");

                checkpointRecord.tagQueries.put(tag, qs);
            }

            bw.write("\n\nDelays: ");
            bw.write("\nms/tok: "+(delays/totalTokens)+" ("+delays+"/"+totalTokens+")");

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        checkpointRecords.add(checkpointRecord);

        GNUPlot queriesPlot = new GNUPlot();
        queriesPlot.title = "Queries/tok vs time, by class";
        queriesPlot.xLabel = "iterations";
        queriesPlot.yLabel = "Queries/tok";

        for (String tag : CoNLLTags) {
            double[] x = new double[checkpointRecords.size()];
            double[] y = new double[checkpointRecords.size()];
            for (int i = 0; i < x.length; i++) {
                x[i] = i;
                y[i] = checkpointRecords.get(i).tagQueries.get(tag);
            }

            queriesPlot.addLine(tag, x, y);
        }

        double[] xAvg = new double[checkpointRecords.size()];
        double[] yAvg = new double[checkpointRecords.size()];
        for (int i = 0; i < xAvg.length; i++) {
            xAvg[i] = i;
            yAvg[i] = checkpointRecords.get(i).avgQueries;
        }

        queriesPlot.addLine("AVG", xAvg, yAvg);

        try {
            queriesPlot.saveAnalysis(subfolder+"/queries/");
        } catch (IOException e) {
            e.printStackTrace();
        }

        GNUPlot f1Plot = new GNUPlot();
        f1Plot.title = "F1 vs time, by class";
        f1Plot.xLabel = "iterations";
        f1Plot.yLabel = "F1";

        for (String tag : CoNLLTags) {
            double[] x = new double[checkpointRecords.size()];
            double[] y = new double[checkpointRecords.size()];
            for (int i = 0; i < x.length; i++) {
                x[i] = i;
                y[i] = checkpointRecords.get(i).tagF1.get(tag);
            }

            f1Plot.addLine(tag, x, y);
        }

        double[] x = new double[checkpointRecords.size()];
        double[] y = new double[checkpointRecords.size()];
        for (int i = 0; i < x.length; i++) {
            x[i] = i;
            y[i] = checkpointRecords.get(i).avgF1;
        }

        f1Plot.addLine("AVG", x, y);

        try {
            f1Plot.saveAnalysis(subfolder + "/f1/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double getL2Regularization() {
        return 0.001;
    }

    @Override
    public String dumpModel(GraphicalModel model) {
        StringBuilder sb = new StringBuilder();

        ModelQueryRecord qr = ModelQueryRecord.getQueryRecordFor(model);
        for (int i = 0; i < model.getVariableSizes().length; i++) {
            if (!model.getVariableMetaDataByReference(i).containsKey("TOKEN")) break;
            sb.append(model.getVariableMetaDataByReference(i).get("TOKEN"));
            sb.append("\t");
            sb.append(model.getVariableMetaDataByReference(i).get("TAG"));
            for (ModelQueryRecord.QueryRecord rec : qr.getResponses(i)) {
                sb.append("\t");
                sb.append("(");
                sb.append(CoNLLTags[rec.response]);
                sb.append(",");
                sb.append(Long.toString(rec.delay));
                sb.append(")");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
