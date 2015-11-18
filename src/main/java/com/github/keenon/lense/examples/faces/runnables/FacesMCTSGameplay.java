package com.github.keenon.lense.examples.faces.runnables;

import com.github.keenon.lense.examples.faces.FacesStaticBatch;
import com.github.keenon.lense.gameplay.Game;
import com.github.keenon.lense.gameplay.players.GamePlayer;
import com.github.keenon.lense.gameplay.players.GamePlayerMCTS;
import com.github.keenon.lense.gameplay.utilities.UncertaintyUtilityWithoutTime;
import com.github.keenon.lense.human_source.HumanSource;
import com.github.keenon.lense.human_source.ModelTagsHumanSource;

import java.io.IOException;

/**
 * Created by keenon on 10/29/15.
 *
 * Runs a simple face classification task
 */
public class FacesMCTSGameplay extends FacesStaticBatch {
    public static void main(String[] args) throws IOException {
        new FacesMCTSGameplay().run();
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
        return "src/main/resources/person_recognition/batches/person.ser";
    }

    @Override
    public String getModelDumpFileLocation() {
        return "src/main/resources/person_recognition/batches/person-dump.txt";
    }

    @Override
    public String getPerformanceReportFolder() {
        return "src/main/resources/person_recognition/runs/mcts-gameplayer";
    }

    @Override
    public double getL2Regularization() {
        return 1.5;
    }

    UncertaintyUtilityWithoutTime utility = new UncertaintyUtilityWithoutTime();

    @Override
    public double utility(Game game) {
        UncertaintyUtilityWithoutTime.humanQueryCost = 0.01;
        return utility.apply(game);
    }
}
