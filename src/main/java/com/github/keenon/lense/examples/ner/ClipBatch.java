package com.github.keenon.lense.examples.ner;

import com.github.keenon.lense.storage.ModelQueryRecord;
import com.github.keenon.loglinear.model.GraphicalModel;
import com.github.keenon.loglinear.storage.ModelBatch;

import java.io.IOException;

/**
 * Created by keenon on 10/25/15.
 */
public class ClipBatch {
    static final int clipUnder = 5;

    public static void main(String[] args) throws IOException {
        ModelBatch batch = new ModelBatch("src/main/resources/ner-batch.ser");

        ModelBatch clippedBatch = new ModelBatch();

        outer: for (GraphicalModel model : batch) {
            ModelQueryRecord mqr = ModelQueryRecord.getQueryRecordFor(model);
            int numTokens = 0;
            for (int i = 0; i < model.variableMetaData.size(); i++) {
                if (!model.getVariableMetaDataByReference(i).containsKey("TOKEN")) continue;
                numTokens++;
                if (mqr.getResponses(i).size() < clipUnder || numTokens > 50) {
                    continue outer;
                }
            }
            clippedBatch.add(model);
        }

        clippedBatch.writeToFileWithoutFactors("src/main/resources/ner-batch-5-vote.ser");
    }
}
