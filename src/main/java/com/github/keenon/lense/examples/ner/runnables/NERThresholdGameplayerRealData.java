package com.github.keenon.lense.examples.ner.runnables;

import com.github.keenon.lense.examples.ner.NERStaticBatch;
import com.github.keenon.lense.gameplay.players.GamePlayer;
import com.github.keenon.lense.gameplay.players.GamePlayerThreshold;
import com.github.keenon.lense.human_source.HumanSource;
import com.github.keenon.lense.human_source.ModelTagsHumanSource;

import java.io.IOException;

/**
 * Created by keenon on 10/24/15.
 *
 * Replays the NER data we've accumulated and frozen using ModelTagsHumanSource, good for testing gameplayers.
 */
public class NERThresholdGameplayerRealData extends NERStaticBatch {
    public static void main(String[] args) throws IOException {
        new NERThresholdGameplayerRealData().run();
    }

    @Override
    public GamePlayer getGamePlayer() {
        GamePlayer gp = new GamePlayerThreshold(); // new GamePlayerNVote(5, false);
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
        return "lense-examples/src/main/resources/ner/runs/threshold-gameplayer-real-data";
    }

    @Override
    public boolean parallelBatchIgnoreRetraining() {
        return false;
    }
}
