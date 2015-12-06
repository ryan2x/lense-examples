package com.github.keenon.lense.examples.sentiment.runnables;

import com.github.keenon.lense.examples.sentiment.SentimentStaticBatch;
import com.github.keenon.lense.gameplay.players.GamePlayer;
import com.github.keenon.lense.gameplay.players.GamePlayerMCTS;
import com.github.keenon.lense.human_source.HumanSource;
import com.github.keenon.lense.human_source.ModelTagsHumanSource;

import java.io.IOException;

/**
 * Created by keenon on 10/28/15.
 */
public class SentimentMCTSGameplayer extends SentimentStaticBatch {

    static String sourceFolder = "src/main/resources/sentiment/batches";
    static String destFolder = "src/main/resources/sentiment/runs";

    public static void main(String[] args) throws IOException {
        if (args.length > 0) sourceFolder = args[0];
        if (args.length > 1) destFolder = args[1];
        new SentimentMCTSGameplayer().run();
    }

    @Override
    public GamePlayer getGamePlayer() {
        return new GamePlayerMCTS();
    }

    @Override
    public HumanSource getHumanSource() {
        return new ModelTagsHumanSource(namespace, observedHumanDelays);
    }

    @Override
    public String getBatchFileLocation() {
        return sourceFolder+"/sentiment.ser";
    }

    @Override
    public String getModelDumpFileLocation() {
        return sourceFolder+"/sentiment-dump.txt";
    }

    @Override
    public String getPerformanceReportFolder() {
        return destFolder+"/mcts-gameplayer-balanced";
    }
}
