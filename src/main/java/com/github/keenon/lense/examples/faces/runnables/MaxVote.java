package com.github.keenon.lense.examples.faces.runnables;

import com.github.keenon.lense.storage.ModelQueryRecord;
import com.github.keenon.loglinear.model.GraphicalModel;
import com.github.keenon.loglinear.storage.ModelBatch;
import com.github.keenon.lense.examples.faces.FacesStaticBatch;

import java.io.IOException;

/**
 * Created by keenon on 10/28/15.
 */
public class MaxVote {
    static int n = 1;

    static String sourceFolder = "src/main/resources/person_recognition/batches";
    static String destFolder = "src/main/resources/person_recognition/runs";

    public static void main(String[] args) throws IOException {
        String batchLocation = "src/main/resources/person_recognition/batches/person.ser";
        if (args.length > 0) {
            sourceFolder = args[0];
            batchLocation = sourceFolder + "/person.ser";
        }
        if (args.length > 1) destFolder = args[1];
        ModelBatch batch = new ModelBatch(batchLocation);

        int numCorrect = 0;
        int total = 0;
        int numQueries = 0;
        long totalDelay = 0;
        int[][] confusion = new int[FacesStaticBatch.tags.length][FacesStaticBatch.tags.length];
        double[] queriesPerType = new double[FacesStaticBatch.tags.length];
        int[] examplesPerType = new int[FacesStaticBatch.tags.length];

        for (GraphicalModel model : batch) {
            int correctTag = 0;

            for (int i = 0; i < FacesStaticBatch.tags.length; i++) {
                if (model.getVariableMetaDataByReference(0).get("NAME").equals(FacesStaticBatch.tags[i])) {
                    correctTag = i;
                    break;
                }
            }

            int[] votes = new int[FacesStaticBatch.tags.length];
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
        for (int i = 0; i < FacesStaticBatch.tags.length; i++) {
            for (int j = 0; j < FacesStaticBatch.tags.length; j++) {
                System.err.println("True: " + FacesStaticBatch.tags[i] + ", Guessed: " + FacesStaticBatch.tags[j] + " -> " + confusion[i][j] + "\n");
            }
        }
        System.err.println("\nQueries Per Type:\n");
        for (int i = 0; i < FacesStaticBatch.tags.length; i++) {
            System.err.println(FacesStaticBatch.tags[i] + ": " + (queriesPerType[i] / examplesPerType[i]) + " (" + queriesPerType[i] + "/" + examplesPerType[i] + ")\n");
        }
    }
}
