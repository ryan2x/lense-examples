package com.github.keenon.lense.examples.ner.runnables;

import com.github.keenon.lense.gameplay.Game;
import com.github.keenon.lense.gameplay.utilities.UncertaintyUtilityWithoutTime;
import com.github.keenon.lense.examples.ner.NERStaticBatch;
import com.github.keenon.lense.gameplay.players.GamePlayer;
import com.github.keenon.lense.gameplay.players.GamePlayerMCTS;
import com.github.keenon.lense.human_source.HumanSource;
import com.github.keenon.lense.human_source.ModelTagsHumanSource;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Created by keenon on 10/24/15.
 *
 * Replays the NER data we've accumulated and frozen using ModelTagsHumanSource, good for testing gameplayers.
 */
@Slf4j
public class NERMCTSGameplayerRealData extends NERStaticBatch {

    static String destFolder = "logs/ner/runs";

    public static void main(String[] args) throws IOException {
        if (args.length > 0) sourceFolder = args[0];
        if (args.length > 1) destFolder = args[1];
        new NERMCTSGameplayerRealData().run();
        System.exit(0);
    }

    @Override
    public GamePlayer getGamePlayer() {
        GamePlayer gp = new GamePlayerMCTS();
        return gp;
    }

    @Override
    public String getBatchFileLocation(){
//        return sourceFolder+"/ner-batch-5-vote.ser";
//        return sourceFolder+"/ner-batch-5-vote-original.ser";
        return sourceFolder+"/ner-batch-subset.ser";
    }

    @Override
    public HumanSource getHumanSource() {
        return new ModelTagsHumanSource(namespace, observedHumanDelays);
    }

    @Override
    public String getPerformanceReportFolder() {
        return destFolder+"/mcts-gameplayer-real-data";
    }

    @Override
    public boolean parallelBatchIgnoreRetraining() {
        return false;
    }

    UncertaintyUtilityWithoutTime utility = new UncertaintyUtilityWithoutTime();

    @Override
    public double utility(Game game) {
        UncertaintyUtilityWithoutTime.humanQueryCost = 0.01;
        return utility.apply(game);
    }
}
