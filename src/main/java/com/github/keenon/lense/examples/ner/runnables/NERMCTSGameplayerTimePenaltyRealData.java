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

    static String sourceFolder = "src/main/resources/ner/batches";
    static String destFolder = "src/main/resources/ner/runs";

    public static void main(String[] args) throws IOException {
        if (args.length > 0) sourceFolder = args[0];
        if (args.length > 1) destFolder = args[1];
        new NERMCTSGameplayerTimePenaltyRealData().run();
    }

    @Override
    public GamePlayer getGamePlayer() {
        GamePlayer gp = new GamePlayerMCTS();
        return gp;
    }

    @Override
    public String getBatchFileLocation(){
        return sourceFolder+"/ner-batch-5-vote.ser";
    }

    @Override
    public HumanSource getHumanSource() {
        return new ModelTagsHumanSource(namespace, observedHumanDelays);
    }

    @Override
    public String getPerformanceReportFolder() {
        return destFolder+"/mcts-gameplayer-with-time-real-data";
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
