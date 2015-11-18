package com.github.keenon.lense.examples.faces.runnables;

import com.github.keenon.lense.examples.faces.FacesStaticBatch;
import com.github.keenon.lense.gameplay.Game;
import com.github.keenon.lense.gameplay.players.GamePlayer;
import com.github.keenon.lense.gameplay.players.GamePlayerThreshold;
import com.github.keenon.lense.human_source.HumanSource;
import com.github.keenon.lense.human_source.ModelTagsHumanSource;

import java.io.IOException;

/**
 * Created by keenon on 10/29/15.
 *
 * Runs a simple face classification task
 */
public class FacesThresholdGameplay extends FacesStaticBatch {
    public static void main(String[] args) throws IOException {
        new FacesThresholdGameplay().run();
    }

    @Override
    public GamePlayer getGamePlayer() {
        return new GamePlayerThreshold();
    }

    @Override
    public HumanSource getHumanSource() {
        return new ModelTagsHumanSource(namespace, observedHumanDelays);
    }

    @Override
    public String getBatchFileLocation() {
        return "src/main/resources/person_recognition/batches/person.ser";
    }

    @Override
    public String getModelDumpFileLocation() {
        return "src/main/resources/person_recognition/batches/person-dump.txt";
    }

    @Override
    public String getPerformanceReportFolder() {
        return "src/main/resources/person_recognition/runs/mcts-time-gameplayer";
    }

    @Override
    public double getL2Regularization() {
        return 1.5;
    }

    @Override
    public double utility(Game game) {
        return 0;
    }
}
