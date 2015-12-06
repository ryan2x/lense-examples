package com.github.keenon.lense.examples.sentiment.runnables;

import com.github.keenon.lense.examples.sentiment.SentimentStaticBatch;
import com.github.keenon.lense.gameplay.players.GamePlayer;
import com.github.keenon.lense.gameplay.players.GamePlayerThreshold;
import com.github.keenon.lense.human_source.HumanSource;
import com.github.keenon.lense.human_source.ModelTagsHumanSource;

import java.io.IOException;

/**
 * Created by keenon on 10/28/15.
 */
public class SentimentThresholdGameplayer extends SentimentStaticBatch {

    static String sourceFolder = "src/main/resources/sentiment/batches";
    static String destFolder = "src/main/resources/sentiment/runs";

    public static void main(String[] args) throws IOException {
        if (args.length > 0) sourceFolder = args[0];
        if (args.length > 1) destFolder = args[1];
        new SentimentThresholdGameplayer().run();
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
        return sourceFolder+"/sentiment.ser";
    }

    @Override
    public String getModelDumpFileLocation() {
        return sourceFolder+"/sentiment-dump.txt";
    }

    @Override
    public String getPerformanceReportFolder() {
        return destFolder+"/threshold-gameplayer-balanced";
    }
}
