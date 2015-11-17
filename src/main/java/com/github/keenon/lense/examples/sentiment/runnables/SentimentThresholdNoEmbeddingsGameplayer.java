package com.github.keenon.lense.examples.sentiment.runnables;

import com.github.keenon.lense.examples.sentiment.SentimentStaticBatch;
import com.github.keenon.lense.gameplay.players.GamePlayer;
import com.github.keenon.loglinear.model.ConcatVector;
import com.github.keenon.loglinear.model.GraphicalModel;
import com.github.keenon.lense.gameplay.players.GamePlayerThreshold;
import com.github.keenon.lense.human_source.HumanSource;
import com.github.keenon.lense.human_source.ModelTagsHumanSource;

import java.io.IOException;

/**
 * Created by keenon on 10/28/15.
 */
public class SentimentThresholdNoEmbeddingsGameplayer extends SentimentStaticBatch {
    public static void main(String[] args) throws IOException {
        new SentimentThresholdNoEmbeddingsGameplayer().run();
    }

    @Override
    public GamePlayer getGamePlayer() {
        GamePlayerThreshold threshold = new GamePlayerThreshold();
        threshold.humanUncertaintyMultiple = 0.2;
        threshold.queryThreshold = 5.0e-3;
        return threshold;
    }

    @Override
    public HumanSource getHumanSource() {
        return new ModelTagsHumanSource(namespace, null);
    }

    @Override
    public String getBatchFileLocation() {
        return "lense-examples/src/main/resources/sentiment/batches/sentiment.ser";
    }

    @Override
    public String getModelDumpFileLocation() {
        return "lense-examples/src/main/resources/sentiment/batches/sentiment-dump.txt";
    }

    @Override
    public String getPerformanceReportFolder() {
        return "lense-examples/src/main/resources/sentiment/runs/threshold-gameplayer-balanced-no-embeddings";
    }

    @Override
    public void featurize(GraphicalModel model) {
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
}
