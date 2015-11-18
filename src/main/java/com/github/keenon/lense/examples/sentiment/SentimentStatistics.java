package com.github.keenon.lense.examples.sentiment;

import com.github.keenon.loglinear.GraphicalModelProto;
import com.github.keenon.loglinear.model.GraphicalModel;
import com.github.keenon.loglinear.storage.ModelBatch;

import java.io.IOException;

/**
 * Created by keenon on 10/28/15.
 */
public class SentimentStatistics {
    public static void main(String[] args) throws IOException {
        ModelBatch batch = new ModelBatch("src/main/resources/sentiment/batches/sentiment.ser");

        ModelBatch pos = new ModelBatch();
        ModelBatch neg = new ModelBatch();

        for (GraphicalModel m : batch) {
            String tag = m.getVariableMetaDataByReference(0).get("SENT");
            if (tag.equals("POS")) pos.add(m);
            else neg.add(m);
        }

        System.err.println("Positive: "+pos.size());
        System.err.println("Negative: "+neg.size());

        ModelBatch angryReviewerDataset = new ModelBatch();

        int cursor = 0;
        int negRatio = 5;
        outer: while (true) {
            angryReviewerDataset.add(pos.get(cursor));
            for (int i = 0; i < negRatio; i++) {
                int negIndex = (cursor*negRatio)+i;
                if (negIndex >= neg.size()) break outer;
                angryReviewerDataset.add(neg.get(negIndex));
            }
            cursor++;
        }

        angryReviewerDataset.writeToFile("src/main/resources/sentiment/batches/sentiment-angry.ser");
    }
}
