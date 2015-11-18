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

    public static void main(String[] args) throws IOException {
        ModelBatch batch = new ModelBatch(batchLocation);

        MaxVoteNoMLRealData.maxVote(batch, 5);
    }
}
