package com.github.keenon.lense.examples.sentiment.runnables;

import com.github.keenon.lense.examples.sentiment.SentimentStaticBatch;
import com.github.keenon.lense.gameplay.Game;
import com.github.keenon.lense.gameplay.players.GamePlayer;
import com.github.keenon.lense.gameplay.players.GamePlayerNVote;
import com.github.keenon.lense.storage.ModelQueryRecord;
import com.github.keenon.loglinear.learning.LogLikelihoodDifferentiableFunction;
import com.github.keenon.loglinear.model.ConcatVector;
import com.github.keenon.loglinear.model.GraphicalModel;
import com.github.keenon.lense.human_source.HumanSource;
import com.github.keenon.lense.human_source.ModelTagsHumanSource;

import java.io.IOException;

/**
 * Created by keenon on 10/24/15.
 *
 * Replays the NER data we've accumulated and frozen using ModelTagsHumanSource, good for testing gameplayers.
 */
public class SentimentMLOnlyNoEmbeddings extends SentimentStaticBatch {
    private int getTagIndex(String tag) {
        for (int i = 0; i < SentimentStaticBatch.tags.length; i++) {
            if (SentimentStaticBatch.tags[i].equals(tag)) {
                return i;
            }
        }
        return -1;
    }

    public SentimentMLOnlyNoEmbeddings() {
        this.overrideSetTrainingLabels = (model) -> {
            ModelQueryRecord mqr = ModelQueryRecord.getQueryRecordFor(model);

            int[] guesses = new int[model.getVariableSizes().length];

            for (int i = 0; i < guesses.length; i++) {
                int[] typesGuessed = new int[SentimentStaticBatch.tags.length];

                for (ModelQueryRecord.QueryRecord qr : mqr.getResponses(i)) {
                    typesGuessed[qr.response] ++;
                }

                int max = 0;
                for (int j = 0; j < typesGuessed.length; j++) {
                    if (typesGuessed[j] > typesGuessed[max]) max = j;
                }

                guesses[i] = max;
            }

            System.err.println("setting label to "+guesses[0]);

            for (int i = 0; i < guesses.length; i++) {
                model.getVariableMetaDataByReference(i).put(LogLikelihoodDifferentiableFunction.VARIABLE_TRAINING_VALUE, ""+guesses[i]);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    public static void main(String[] args) throws IOException {
        new SentimentMLOnlyNoEmbeddings().run();
    }

    @Override
    public GamePlayer getGamePlayer() {
        GamePlayer gp = new GamePlayerNVote(0, false);
        return gp;
    }

    @Override
    public String getBatchFileLocation(){
        return "lense-examples/src/main/resources/sentiment/batches/sentiment.ser";
    }

    @Override
    public String getModelDumpFileLocation() {
        return null;
    }

    @Override
    public HumanSource getHumanSource() {
        return new ModelTagsHumanSource(namespace, observedHumanDelays);
    }

    @Override
    public String getPerformanceReportFolder() {
        return "lense-examples/src/main/resources/sentiment/runs/ml-only-no-embeddings";
    }

    @Override
    public double utility(Game game) {
        return 0;
    }

    @Override
    public boolean parallelBatchIgnoreRetraining() {
        return false;
    }

    @Override
    public double getL2Regularization() {
        return 0.01;
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
