package com.github.keenon.lense.examples.ner.runnables;

import com.github.keenon.loglinear.storage.ModelBatch;

import java.io.IOException;

/**
 * Created by keenon on 10/25/15.
 *
 * Runs a simple, max-vote system, without involving any ML at all.
 */
public class MaxVoteNoMLSyntheticData {
    static String batchLocation = "src/main/resources/ner/batches/ner-batch-synthetic.ser";

    static String[] CoNLLTags = new String[] {
            "PER",
            "LOC",
            "ORG",
            "O"
    };

    static String sourceFolder = "src/main/resources/ner/batches";
    static String destFolder = "src/main/resources/ner/runs";

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            sourceFolder = args[0];
            batchLocation = sourceFolder + "/ner-batch-synthetic.ser";
        }
        if (args.length > 1) destFolder = args[1];

        ModelBatch batch = new ModelBatch(batchLocation);

        MaxVoteNoMLRealData.maxVote(batch, 5);
    }
}
