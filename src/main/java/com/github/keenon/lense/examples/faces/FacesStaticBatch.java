package com.github.keenon.lense.examples.faces;

import com.github.keenon.lense.examples.util.GNUPlot;
import com.github.keenon.lense.gameplay.Game;
import com.github.keenon.lense.storage.ModelQueryRecord;
import com.github.keenon.loglinear.model.ConcatVector;
import com.github.keenon.loglinear.model.GraphicalModel;
import com.github.keenon.loglinear.storage.ModelBatch;
import com.github.keenon.lense.convenience.StaticBatchLense;

import java.io.*;
import java.util.*;

/**
 * Created by keenon on 10/28/15.
 *
 * Models a celebrity face classification task, using simple image embeddings as features.
 */
public abstract class FacesStaticBatch extends StaticBatchLense {
    public static final String[] tags = new String[]{
            "Anderson Cooper",
            "Daniel Craig",
            "Miley Cyrus",
            "Scarlett Johansson"
    };

    @Override
    public ModelBatch createInitialModelBatch() {
        ModelBatch batch = new ModelBatch();

        try {
            BufferedReader[] embeddingsReaders = new BufferedReader[tags.length];
            for (int i = 0; i < tags.length; i++) {
                embeddingsReaders[i] = new BufferedReader(new FileReader("src/main/resources/person_recognition/"+tags[i]+" Embeddings.txt"));
            }

            Map<String,GraphicalModel> urlToModel = new HashMap<>();

            BufferedReader br = new BufferedReader(new FileReader("src/main/resources/person_recognition/dev_urls.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) continue;
                String[] parts = line.split("\t");
                if (parts.length != 3) {
                    String name = parts[0];
                    String number = parts[1];
                    String url = parts[2];

                    int index = -1;
                    for (int i = 0; i < tags.length; i++) {
                        if (tags[i].equals(name)) {
                            index = i;
                            break;
                        }
                    }

                    if (index == -1) continue;

                    String embeddingString = embeddingsReaders[index].readLine();
                    assert(embeddingString != null);
                    if (embeddingString.equals("err")) {
                        // This image link is dead
                        continue;
                    }

                    System.err.println("Read name: "+name);
                    System.err.println("Read url: "+url);
                    String[] embeddingParts = embeddingString.split(" ");
                    System.err.println("Embedding size: "+embeddingParts.length);

                    GraphicalModel model = new GraphicalModel();
                    model.getVariableMetaDataByReference(0).put("NAME", name);
                    model.getVariableMetaDataByReference(0).put("EMBED", embeddingString);
                    model.getVariableMetaDataByReference(0).put("URL", url);

                    batch.add(model);

                    urlToModel.put(url, model);
                }
            }

            File f = new File("src/main/resources/person_recognition/context");

            double numCorrect = 0.0;
            double total = 0.0;
            List<Long> delays = new ArrayList<>();

            assert(f.isDirectory());
            assert(f.listFiles() != null);
            for (File child : f.listFiles()) {
                BufferedReader guessReader = new BufferedReader(new FileReader(child));
                String l;
                while ((l = guessReader.readLine()) != null) {
                    String[] parts = l.split("\t");
                    if (parts.length > 1) {
                        String descriptor = parts[0];
                        String url = descriptor.split("\\(")[1].split(",")[2].replace(")","");
                        System.err.println("Descriptor: "+descriptor);
                        System.err.println("URL: "+url);

                        GraphicalModel model = urlToModel.get(url);
                        if (model != null) {
                            ModelQueryRecord mqr = ModelQueryRecord.getQueryRecordFor(model);
                            for (int i = 2; i < parts.length; i++) {
                                String[] queryParts = parts[i].split(",");
                                String response = queryParts[0];
                                long delay = Long.parseLong(queryParts[2]);
                                System.err.println(response+": "+delay);
                                if (response.equals(model.getVariableMetaDataByReference(0).get("NAME"))) {
                                    numCorrect++;
                                }
                                total++;
                                delays.add(delay);

                                int index = -1;
                                for (int j = 0; j < tags.length; j++) {
                                    if (response.equals(tags[j])) {
                                        index = j;
                                        break;
                                    }
                                }
                                assert(index != -1);

                                mqr.recordResponse(0, index, delay);
                            }
                            mqr.writeBack();
                        }
                    }
                }
            }

            double accuracy = (numCorrect / total);
            System.err.println("Human accuracy: "+accuracy);

            Random r = new Random(42);

            ModelBatch randomizedBatch = new ModelBatch();
            while (batch.size() > 0) {
                int index = r.nextInt(batch.size());
                randomizedBatch.add(batch.get(index));
                batch.remove(index);
            }
            batch = randomizedBatch;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return batch;
    }

    @Override
    public void featurize(GraphicalModel model) {
        String embeddingString = model.getVariableMetaDataByReference(0).get("EMBED");
        String[] embeddingComponents = embeddingString.split(" ");
        double[] embedding = new double[embeddingComponents.length];
        for (int i = 0; i < embeddingComponents.length; i++) {
            embedding[i] = Double.parseDouble(embeddingComponents[i]);
        }

        model.addFactor(new int[]{0}, new int[]{tags.length}, (assn) -> {
            ConcatVector vector = new ConcatVector(0);
            namespace.setDenseFeature(vector, tags[assn[0]]+"BIAS", new double[]{1.0});
            namespace.setDenseFeature(vector, tags[assn[0]]+"embedding", embedding);
            return vector;
        });
    }

    @Override
    public boolean parallelBatchIgnoreRetraining() {
        return false;
    }

    private static class GraphCheckpoint {
        double[] queryRate;
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
                if (gameRecord.game.model.getVariableMetaDataByReference(0).get("NAME").equals(tags[i])) {
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
    public void dumpGame(GameRecord gameRecord, BufferedWriter bw) throws IOException {

    }
}
