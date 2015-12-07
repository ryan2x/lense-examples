package com.github.keenon.lense.examples.sentiment.runnables;

import com.github.keenon.lense.storage.ModelQueryRecord;
import com.github.keenon.loglinear.model.GraphicalModel;
import com.github.keenon.loglinear.storage.ModelBatch;
import com.github.keenon.lense.examples.sentiment.SentimentStaticBatch;

import java.io.IOException;

/**
 * Created by keenon on 10/28/15.
 */
public class MaxVote {
    static int n = 5;

    static String sourceFolder = "src/main/resources/sentiment/batches";
    static String destFolder = "src/main/resources/sentiment/runs";

    public static void main(String[] args) throws IOException {
        String batchLocation = "src/main/resources/sentiment/batches/sentiment.ser";
        if (args.length > 0) {
            sourceFolder = args[0];
            batchLocation = sourceFolder + "/sentiment.ser";
        }
        if (args.length > 1) destFolder = args[1];
        ModelBatch batch = new ModelBatch(batchLocation);

        int numCorrect = 0;
        int total = 0;
        int numQueries = 0;
        long totalDelay = 0;
        int[][] confusion = new int[][]{
                new int[]{0,0},
                new int[]{0,0}
        };
        double[] queriesPerType = new double[2];
        int[] examplesPerType = new int[2];

        for (GraphicalModel model : batch) {
            int correctTag = 0;

            for (int i = 0; i < SentimentStaticBatch.tags.length; i++) {
                if (model.getVariableMetaDataByReference(0).get("SENT").equals(SentimentStaticBatch.tags[i])) {
                    correctTag = i;
                    break;
                }
            }

            int[] votes = new int[SentimentStaticBatch.tags.length];
            long delay = 0;
            ModelQueryRecord mqr = ModelQueryRecord.getQueryRecordFor(model);
            for (int i = 0; i < Math.min(n, mqr.getResponses(0).size()); i++) {
                ModelQueryRecord.QueryRecord qr = mqr.getResponses(0).get(i);
                votes[qr.response] ++;
                if (qr.delay > delay) delay = qr.delay;

                numQueries++;
                queriesPerType[correctTag]++;
            }

            int guess = 0;
            for (int i = 0; i < votes.length; i++) {
                if (votes[i] > votes[guess]) guess = i;
            }

            if (guess == correctTag) {
                numCorrect++;
            }
            total++;

            confusion[correctTag][guess]++;

            examplesPerType[correctTag]++;

            totalDelay += delay;
        }
        double accuracy = (double)numCorrect / total;
        double queriesPerDocument = (double)numQueries / total;
        long delayPerDocument = totalDelay / total;

        System.err.println("Accuracy: " + accuracy + " (" + numCorrect + "/" + total + ")\n");
        System.err.println("Queries per document: " + queriesPerDocument + " (" + numQueries + "/" + total + ")\n");
        System.err.println("Delay per document: " + delayPerDocument + "ms (" + totalDelay + "/" + total + ")\n");
        System.err.println("\nConfusion:\n");
        for (int i = 0; i < SentimentStaticBatch.tags.length; i++) {
            for (int j = 0; j < SentimentStaticBatch.tags.length; j++) {
                System.err.println("True: " + SentimentStaticBatch.tags[i] + ", Guessed: " + SentimentStaticBatch.tags[j] + " -> " + confusion[i][j] + "\n");
            }
        }
        System.err.println("\nQueries Per Type:\n");
        for (int i = 0; i < SentimentStaticBatch.tags.length; i++) {
            System.err.println(SentimentStaticBatch.tags[i] + ": " + (queriesPerType[i] / examplesPerType[i]) + " (" + queriesPerType[i] + "/" + examplesPerType[i] + ")\n");
        }
    }
}
