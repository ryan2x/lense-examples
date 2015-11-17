package com.github.keenon.lense.examples.ner.runnables;

import com.github.keenon.lense.gameplay.Game;
import com.github.keenon.lense.gameplay.players.GamePlayer;
import com.github.keenon.lense.gameplay.players.GamePlayerMCTS;
import com.github.keenon.lense.gameplay.utilities.UncertaintyUtility;
import com.github.keenon.lense.examples.ner.NERStaticBatch;
import com.github.keenon.lense.human_source.HumanSource;
import com.github.keenon.lense.human_source.ModelTagsHumanSource;

import java.io.IOException;

/**
 * Created by keenon on 10/24/15.
 *
 * Replays the NER data we've accumulated and frozen using ModelTagsHumanSource, good for testing gameplayers.
 */
public class NERMCTSGameplayerTimePenaltyRealData extends NERStaticBatch {
    public static void main(String[] args) throws IOException {
        new NERMCTSGameplayerTimePenaltyRealData().run();
    }

    @Override
    public GamePlayer getGamePlayer() {
        GamePlayer gp = new GamePlayerMCTS();
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
        return "lense-examples/src/main/resources/ner/runs/mcts-gameplayer-with-time-real-data";
    }

    @Override
    public boolean parallelBatchIgnoreRetraining() {
        return false;
    }

    UncertaintyUtility utility = new UncertaintyUtility();

    @Override
    public double utility(Game game) {
        return utility.apply(game);
    }
}
