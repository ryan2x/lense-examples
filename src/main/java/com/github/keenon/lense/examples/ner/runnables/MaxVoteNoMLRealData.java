package com.github.keenon.lense.examples.ner.runnables;

import com.github.keenon.lense.storage.ModelQueryRecord;
import com.github.keenon.loglinear.model.GraphicalModel;
import com.github.keenon.loglinear.storage.ModelBatch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by keenon on 10/25/15.
 *
 * Runs a simple, max-vote system, without involving any ML at all.
 */
public class MaxVoteNoMLRealData {
    static String batchLocation = "lense-examples/src/main/resources/ner/batches/ner-batch-5-vote.ser";

    static String[] CoNLLTags = new String[] {
            "PER",
            "LOC",
            "ORG",
            "O"
    };

    public static void main(String[] args) throws IOException {
        ModelBatch batch = new ModelBatch(batchLocation);

        maxVote(batch, 5);
    }

    public static void maxVote(ModelBatch batch, int n) throws IOException {
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

        int numGraphs = 0;

        for (GraphicalModel model : batch) {
            ModelQueryRecord mqr = ModelQueryRecord.getQueryRecordFor(model);
            int size = 0;
            for (int i = 0; i < model.variableMetaData.size(); i++) {
                if (mqr.getResponses(i).size() > 0) size = i;
                else break;
            }

            if (size > 50 || size == 0) continue;
            numGraphs++;

            int[] guesses = new int[size];

            int numAnnotators = Integer.MAX_VALUE;
            for (int i = 0; i < guesses.length; i++) {
                if (mqr.getResponses(i).size() < numAnnotators) numAnnotators = mqr.getResponses(i).size();
            }

            long[] delayByAnnotator = new long[numAnnotators];

            for (int i = 0; i < guesses.length; i++) {
                int[] typesGuessed = new int[CoNLLTags.length];

                String type = model.variableMetaData.get(i).get("TAG");
                for (int j = 0; j < Math.min(mqr.getResponses(i).size(), n); j++) {
                    ModelQueryRecord.QueryRecord qr = mqr.getResponses(i).get(j);
                    delayByAnnotator[j] += qr.delay;
                    typesGuessed[qr.response] ++;
                    queries++;
                    queriesPerType.put(type, queriesPerType.getOrDefault(type, 0.0) + 1);
                }

                int max = 0;
                for (int j = 0; j < typesGuessed.length; j++) {
                    if (typesGuessed[j] > typesGuessed[max]) max = j;
                }

                long slowestAnnotator = 0;
                for (long delay : delayByAnnotator) {
                    if (delay > slowestAnnotator) slowestAnnotator = delay;
                }
                delays += slowestAnnotator;

                guesses[i] = max;
                totalTokens++;
            }

            String[] nerGuesses = new String[guesses.length];
            for (int i = 0; i < guesses.length; i++) {
                nerGuesses[i] = CoNLLTags[guesses[i]];
                String trueValue = model.getVariableMetaDataByReference(i).get("TAG");
                if (nerGuesses[i].equals(trueValue)) {
                    correct++;
                    correctChunk.put(nerGuesses[i], correctChunk.getOrDefault(nerGuesses[i], 0.) + 1);
                }
                total++;
                foundCorrect.put(trueValue, foundCorrect.getOrDefault(trueValue, 0.) + 1);
                foundGuessed.put(nerGuesses[i], foundGuessed.getOrDefault(nerGuesses[i], 0.) + 1);
            }
        }

        System.err.println("Batch size "+numGraphs);

        System.err.print("\nSystem results:\n");

        System.err.print("\nAccuracy: " + (correct / total) + "\n");

        double avgF1 = 0.0;
        for (String tag : CoNLLTags) {
            double precision = foundGuessed.getOrDefault(tag, 0.0) == 0 ? 0.0 : correctChunk.getOrDefault(tag, 0.0) / foundGuessed.get(tag);
            double recall = foundCorrect.getOrDefault(tag, 0.0) == 0 ? 0.0 : correctChunk.getOrDefault(tag, 0.0) / foundCorrect.get(tag);
            double f1 = (precision + recall == 0.0) ? 0.0 : (precision * recall * 2) / (precision + recall);
            if (!tag.equals("O")) {
                avgF1 += f1 / 3;
            }
            System.err.print("\n"+tag+" ("+foundCorrect.getOrDefault(tag, 0.0).intValue()+")");
            System.err.print("\n\tP:" + precision + " (" + correctChunk.getOrDefault(tag, 0.0).intValue() + "/" + foundGuessed.getOrDefault(tag, 0.0).intValue() + ")");
            System.err.print("\n\tR:"+recall+" ("+correctChunk.getOrDefault(tag, 0.0).intValue()+"/"+foundCorrect.getOrDefault(tag, 0.0).intValue()+")");
            System.err.print("\n\tF1:" + f1);
        }

        System.err.println("\n\nAverage Non-O F1: "+avgF1);

        System.err.print("\n\nQueries: ");
        System.err.print("\nQ/tok: "+(queries/totalTokens)+" ("+queries+"/"+totalTokens+")");
        for (String tag : CoNLLTags) {
            System.err.print("\n"+tag+" ("+foundCorrect.getOrDefault(tag, 0.0).intValue()+")");
            double qs = correctChunk.getOrDefault(tag, 0.0) == 0 ? 0.0 : queriesPerType.getOrDefault(tag, 0.0) / foundCorrect.get(tag);
            System.err.print("\n\tQ/tok: "+qs+" ("+queriesPerType.getOrDefault(tag, 0.0)+"/"+foundCorrect.get(tag)+")");
        }

        System.err.print("\n\nDelays: ");
        System.err.print("\nms/tok: "+(delays/totalTokens)+" ("+delays+"/"+totalTokens+")");
    }
}
