package com.github.keenon.lense.examples.ner;

import com.github.keenon.lense.gameplay.distributions.ContinuousDistribution;
import com.github.keenon.lense.gameplay.distributions.DiscreteSetDistribution;
import com.github.keenon.lense.storage.ModelQueryRecord;
import com.github.keenon.loglinear.model.GraphicalModel;
import com.github.keenon.loglinear.storage.ModelBatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by keenon on 10/25/15.
 */
public class SyntheticDataGen {
    public static void main(String[] args) throws IOException {
        String[] CoNLLTags = new String[] {
                "PER",
                "LOC",
                "ORG",
                "O"
        };
        double[] errorRate = new double[] {
                0.15,
                0.31,
                0.36,
                0.03
        };
        List<String> tags = new ArrayList<>();
        for (String t : CoNLLTags) tags.add(t);

        ModelBatch batch = new ModelBatch("lense-examples/src/main/resources/ner-batch.ser");

        List<Long> delays = new ArrayList<>();
        outer: for (int j = 10; j < batch.size(); j++) {
            GraphicalModel model = batch.get(j);
            for (int i = 0; i < model.variableMetaData.size(); i++) {
                for (ModelQueryRecord.QueryRecord qr : ModelQueryRecord.getQueryRecordFor(model).getResponses(i)) {
                    delays.add(qr.delay);
                    if (delays.size() > 10000) break outer;
                }
            }
        }
        long[] observed = new long[delays.size()];
        for (int i = 0; i < delays.size(); i++) {
            observed[i] = delays.get(i);
        }
        ContinuousDistribution observedHumanDelays = new DiscreteSetDistribution(observed);

        Random r = new Random(42);

        ModelBatch synthetic = new ModelBatch();
        for (GraphicalModel model : batch) {
            GraphicalModel clone = model.cloneModel();

            ModelQueryRecord qr = ModelQueryRecord.getQueryRecordFor(clone);
            int size = 0;
            for (int i = 0; i < clone.variableMetaData.size(); i++) {
                if (clone.getVariableMetaDataByReference(i).containsKey("TAG")) size = i;
                else break;
            }

            for (int i = 0; i < size; i++) {
                int trueLabel = tags.indexOf(model.getVariableMetaDataByReference(i).get("TAG"));
                if (trueLabel == -1) throw new IllegalStateException("blag");

                double errRate = errorRate[trueLabel];

                qr.getResponses(i).clear();

                while (qr.getResponses(i).size() < 6) {
                    long delay = observedHumanDelays.drawSample(r) + r.nextInt(15) - 7;
                    boolean makeError = r.nextDouble() < errRate;
                    int humanLabel = trueLabel;
                    if (makeError) {
                        while (humanLabel == trueLabel) humanLabel = r.nextInt(CoNLLTags.length);
                    }
                    qr.recordResponse(i, humanLabel, delay);
                }
            }

            qr.writeBack();

            synthetic.add(clone);
        }

        synthetic.writeToFileWithoutFactors("lense-examples/src/main/resources/ner-batch-synthetic.ser");
    }
}
