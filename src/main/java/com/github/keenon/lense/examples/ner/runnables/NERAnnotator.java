package com.github.keenon.lense.examples.ner.runnables;

import com.github.keenon.lense.examples.ner.NERStaticBatch;
import com.github.keenon.lense.gameplay.players.GamePlayer;
import com.github.keenon.lense.gameplay.players.GamePlayerNVote;
import com.github.keenon.lense.human_source.HumanSource;
import com.github.keenon.lense.human_source.MTurkHumanSource;

import java.io.IOException;

/**
 * Created by keenon on 10/24/15.
 *
 * What to run if you want to annotate more NER tags
 */
public class NERAnnotator extends NERStaticBatch {
    public static void main(String[] args) throws IOException {
        new NERAnnotator().run();
    }

    @Override
    public GamePlayer getGamePlayer() {
        return new GamePlayerNVote(5, true);
    }

    @Override
    public HumanSource getHumanSource() {
        try {
            return new MTurkHumanSource("localhost", namespace, observedHumanDelays);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getBatchFileLocation() {
        return "src/main/resources/ner/batches/ner-batch.ser";
    }

    @Override
    public String getPerformanceReportFolder() {
        return "src/main/resources/ner/runs/human-annotator";
    }

    @Override
    public boolean parallelBatchIgnoreRetraining() {
        return true;
    }
}
