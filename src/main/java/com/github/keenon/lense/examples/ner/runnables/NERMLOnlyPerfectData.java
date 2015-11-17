package com.github.keenon.lense.examples.ner.runnables;

import com.github.keenon.loglinear.learning.LogLikelihoodDifferentiableFunction;
import com.github.keenon.lense.examples.ner.NERStaticBatch;
import com.github.keenon.lense.gameplay.players.GamePlayer;
import com.github.keenon.lense.gameplay.players.GamePlayerNVote;
import com.github.keenon.lense.human_source.HumanSource;
import com.github.keenon.lense.human_source.ModelTagsHumanSource;

import java.io.IOException;

/**
 * Created by keenon on 10/24/15.
 *
 * Replays the NER data we've accumulated and frozen using ModelTagsHumanSource, good for testing gameplayers.
 */
public class NERMLOnlyPerfectData extends NERStaticBatch {
    private int getTagIndex(String tag) {
        for (int i = 0; i < CoNLLTags.length; i++) {
            if (CoNLLTags[i].equals(tag)) {
                return i;
            }
        }
        return -1;
    }

    public NERMLOnlyPerfectData() {
        this.overrideSetTrainingLabels = (model) -> {
            for (int i = 0; i < model.getVariableSizes().length; i++) {
                if (model.getVariableMetaDataByReference(i).containsKey("TAG")) {
                    int tagIndex = getTagIndex(model.getVariableMetaDataByReference(i).get("TAG"));
                    assert(tagIndex >= 0);
                    model.getVariableMetaDataByReference(i).put(
                            LogLikelihoodDifferentiableFunction.VARIABLE_TRAINING_VALUE,
                            ""+tagIndex);
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    public static void main(String[] args) throws IOException {
        new NERMLOnlyPerfectData().run();
    }

    @Override
    public GamePlayer getGamePlayer() {
        GamePlayer gp = new GamePlayerNVote(0, false);
        return gp;
    }

    @Override
    public String getBatchFileLocation(){
        return "lense-examples/src/main/resources/ner/batches/ner-batch-5-vote.ser";
    }

    @Override
    public HumanSource getHumanSource() {
        return new ModelTagsHumanSource(namespace, observedHumanDelays);
    }

    @Override
    public String getPerformanceReportFolder() {
        return "lense-examples/src/main/resources/ner/runs/ml-only-perfect-data";
    }

    @Override
    public boolean parallelBatchIgnoreRetraining() {
        return false;
    }
}
