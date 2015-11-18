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
    public static void main(String[] args) throws IOException {
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
        return "src/main/resources/sentiment/batches/sentiment.ser";
    }

    @Override
    public String getModelDumpFileLocation() {
        return "src/main/resources/sentiment/batches/sentiment-dump.txt";
    }

    @Override
    public String getPerformanceReportFolder() {
        return "src/main/resources/sentiment/runs/threshold-gameplayer-balanced";
    }
}
