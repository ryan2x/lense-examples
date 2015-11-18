package com.github.keenon.lense.examples.sentiment;

import com.github.keenon.lense.gameplay.Game;
import com.github.keenon.lense.gameplay.utilities.UncertaintyUtilityWithoutTime;
import com.github.keenon.loglinear.model.ConcatVector;
import com.github.keenon.loglinear.model.ConcatVectorNamespace;
import com.github.keenon.loglinear.model.GraphicalModel;
import com.github.keenon.loglinear.storage.ModelBatch;
import com.github.keenon.lense.convenience.StaticBatchLense;
import com.github.keenon.lense.examples.util.GNUPlot;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by keenon on 10/28/15.
 *
 * Manages the basics around sentiment classification
 */
public abstract class SentimentStaticBatch extends StaticBatchLense {
    public static String[] tags = new String[]{
            "POS",
            "NEG"
    };

    protected ConcatVectorNamespace namespace = new ConcatVectorNamespace();

    @Override
    public ModelBatch createInitialModelBatch() {
        ModelBatch batch = new ModelBatch();

        // Read out both the recorded sentiment data and the CSVs of recorded human responses

        try {
            BufferedReader br = new BufferedReader(new FileReader("src/main/resources/sentiment/socher_cache.txt"));

            Map<String, GraphicalModel> articleToModel = new HashMap<>();

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                String document = parts[0];
                List<Double> embedding = new ArrayList<>();
                for (int i = 1; i < parts.length; i++) {
                    try {
                        double d = Double.parseDouble(parts[i]);
                        embedding.add(d);
                    }
                    catch (NumberFormatException e) {
                        document += "\t"+parts[i];
                    }
                }

                System.err.println("Document: ");
                System.err.println(document);
                System.err.println("Embedding: ");
                System.err.println(embedding);

                StringBuilder embeddingEncoded = new StringBuilder();
                for (int i = 0; i < embedding.size(); i++) {
                    if (i > 0) embeddingEncoded.append(",");
                    embeddingEncoded.append(Double.toString(embedding.get(i)));
                }

                System.err.println("Embedding CSV: ");
                System.err.println(embeddingEncoded.toString());

                GraphicalModel model = new GraphicalModel();
                model.getVariableMetaDataByReference(0).put("TEXT", document);
                model.getVariableMetaDataByReference(0).put("EMBED", embeddingEncoded.toString());

                articleToModel.put(document, model);

                batch.add(model);
            }
            br.close();

            // Read through the positive and negative articles, and assign codes accordingly

            assignCodes("src/main/resources/sentiment/aclImdb/test/neg/neg.combined", articleToModel, "NEG");
            assignCodes("src/main/resources/sentiment/aclImdb/train/neg/neg.combined", articleToModel, "NEG");
            assignCodes("src/main/resources/sentiment/aclImdb/test/pos/pos.combined", articleToModel, "POS");
            assignCodes("src/main/resources/sentiment/aclImdb/train/pos/pos.combined", articleToModel, "POS");

            // Check that every article has a code assigned

            for (GraphicalModel model : batch) {
                assert(model.getVariableMetaDataByReference(0).containsKey("SENT"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return batch;
    }

    private static void assignCodes(String path, Map<String,GraphicalModel> articleToModel, String tag) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));

        String line;
        while ((line = br.readLine()) != null) {
            if (articleToModel.containsKey(line)) {
                articleToModel.get(line).getVariableMetaDataByReference(0).put("SENT", tag);
            }
        }

        br.close();
    }

    @Override
    public void featurize(GraphicalModel model) {
        String[] embedTokens = model.getVariableMetaDataByReference(0).get("EMBED").split(",");
        double[] embedding = new double[embedTokens.length];
        for (int i = 0; i < embedTokens.length; i++) {
            embedding[i] = Double.parseDouble(embedTokens[i]);
        }

        String text = model.getVariableMetaDataByReference(0).get("TEXT");

        String[] unigrams = text.split(" ");
        String[] bigrams = new String[unigrams.length-1];
        for (int i = 0; i < bigrams.length; i++) {
            bigrams[i] = unigrams[i]+" "+unigrams[i+1];
        }

        model.addFactor(new int[]{0}, new int[]{2}, (assn) -> {
            ConcatVector vector = new ConcatVector(0);
            String tag = tags[assn[0]];

            namespace.setDenseFeature(vector, tag+"BIAS", new double[]{1.0});

            namespace.setDenseFeature(vector, tag+"SocherEmbeddings", embedding);

            for (String unigram : unigrams) {
                namespace.setDenseFeature(vector, tag+"UNIGRAM:"+unigram, new double[]{1.0});
            }

            /*
            for (String bigram : bigrams) {
                namespace.setDenseFeature(vector, tag+"BIGRAM" + bigram, new double[]{1.0});
            }
            */

            return vector;
        });
    }

    UncertaintyUtilityWithoutTime utility = new UncertaintyUtilityWithoutTime();

    @Override
    public double utility(Game game) {
        return utility.apply(game);
    }

    @Override
    public boolean parallelBatchIgnoreRetraining() {
        return false;
    }

    private static class GraphCheckpoint {
        double[] queryRate;
        double avgQueryRate;
        double accuracy;
    }

    List<GraphCheckpoint> graphCheckpoints = new ArrayList<>();

    @Override
    public void checkpoint(List<GameRecord> gamesFinished) {
        String subfolder = getThisRunPerformanceReportSubFolder();

        int numCorrect = 0;
        int total = 0;
        int numQueries = 0;
        long totalDelay = 0;
        int[][] confusion = new int[tags.length][tags.length];
        double[] queriesPerType = new double[tags.length];
        int[] examplesPerType = new int[tags.length];

        for (GameRecord gameRecord : gamesFinished) {
            int correctTag = 0;

            for (int i = 0; i < tags.length; i++) {
                if (gameRecord.game.model.getVariableMetaDataByReference(0).get("SENT").equals(tags[i])) {
                    correctTag = i;
                    break;
                }
            }

            if (gameRecord.result[0] == correctTag) {
                numCorrect++;
            }
            total++;

            confusion[correctTag][gameRecord.result[0]]++;

            for (Game.Event e : gameRecord.game.stack) {
                if (e instanceof Game.QueryLaunch) {
                    numQueries++;
                    queriesPerType[correctTag]++;
                }
            }

            examplesPerType[correctTag]++;

            totalDelay += gameRecord.game.timeSinceGameStart;
        }
        double accuracy = (double)numCorrect / total;
        double queriesPerDocument = (double)numQueries / total;
        long delayPerDocument = totalDelay / total;

        GraphCheckpoint graphPoint = new GraphCheckpoint();
        graphPoint.accuracy = accuracy;
        graphPoint.queryRate = new double[tags.length];
        for (int i = 0; i < tags.length; i++) {
            graphPoint.queryRate[i] = queriesPerType[i] / examplesPerType[i];
        }
        graphPoint.avgQueryRate = queriesPerDocument;
        graphCheckpoints.add(graphPoint);

        GNUPlot plot = new GNUPlot();
        plot.title = "Queries by true type of document";
        plot.xLabel = "time";
        plot.yLabel = "queries";
        for (int i = 0; i < tags.length; i++) {
            double[] queryRateHistory = new double[graphCheckpoints.size()];
            double[] xAxis = new double[graphCheckpoints.size()];
            for (int j = 0; j < graphCheckpoints.size(); j++) {
                queryRateHistory[j] = graphCheckpoints.get(j).queryRate[i];
                xAxis[j] = j;
            }
            plot.addLine(tags[i], xAxis, queryRateHistory);
        }
        double[] queryRateHistory = new double[graphCheckpoints.size()];
        double[] xAxis = new double[graphCheckpoints.size()];
        for (int j = 0; j < graphCheckpoints.size(); j++) {
            queryRateHistory[j] = graphCheckpoints.get(j).avgQueryRate;
            xAxis[j] = j;
        }
        plot.addLine("AVG", xAxis, queryRateHistory);

        try {
            plot.saveAnalysis(subfolder);

            BufferedWriter results = new BufferedWriter(new FileWriter(subfolder+"/results.txt"));
            results.write("Completed: "+total+" documents\n\n");
            results.write("Accuracy: "+accuracy+" ("+numCorrect+"/"+total+")\n");
            results.write("Queries per document: "+queriesPerDocument+" ("+numQueries+"/"+total+")\n");
            results.write("Delay per document: "+delayPerDocument+"ms ("+totalDelay+"/"+total+")\n");
            results.write("\nConfusion:\n");
            for (int i = 0; i < tags.length; i++) {
                for (int j = 0; j < tags.length; j++) {
                    results.write("True: "+tags[i]+", Guessed: "+tags[j]+" -> "+confusion[i][j]+"\n");
                }
            }
            results.write("\nQueries Per Type:\n");
            for (int i = 0; i < tags.length; i++) {
                results.write(tags[i]+": "+(queriesPerType[i] / examplesPerType[i])+" ("+queriesPerType[i]+"/"+examplesPerType[i]+")\n");
            }
            results.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String dumpModel(GraphicalModel model) {
        return null;
    }

    @Override
    public double getL2Regularization() {
        return 0.1;
    }

    @Override
    public void dumpGame(GameRecord gameRecord, BufferedWriter bw) throws IOException {

    }
}
