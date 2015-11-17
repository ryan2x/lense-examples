package com.github.keenon.lense.examples.faces.runnables;

import com.github.keenon.lense.examples.faces.FacesStaticBatch;
import com.github.keenon.lense.gameplay.Game;
import com.github.keenon.lense.gameplay.players.GamePlayer;
import com.github.keenon.lense.gameplay.players.GamePlayerMCTS;
import com.github.keenon.lense.gameplay.utilities.UncertaintyUtility;
import com.github.keenon.lense.human_source.HumanSource;
import com.github.keenon.lense.human_source.ModelTagsHumanSource;

import java.io.IOException;

/**
 * Created by keenon on 10/29/15.
 *
 * Runs a simple face classification task
 */
public class FacesMCTSTimePenaltyGameplay extends FacesStaticBatch {
    public static void main(String[] args) throws IOException {
        new FacesMCTSTimePenaltyGameplay().run();
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
        return "lense-examples/src/main/resources/person_recognition/batches/person.ser";
    }

    @Override
    public String getModelDumpFileLocation() {
        return "lense-examples/src/main/resources/person_recognition/batches/person-dump.txt";
    }

    @Override
    public String getPerformanceReportFolder() {
        return "lense-examples/src/main/resources/person_recognition/runs/threshold-gameplayer";
    }

    @Override
    public double getL2Regularization() {
        return 1.5;
    }

    UncertaintyUtility utility = new UncertaintyUtility();

    @Override
    public double utility(Game game) {
        UncertaintyUtility.humanQueryCost = 0.02;
        return utility.apply(game);
    }
}
