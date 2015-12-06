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
    static String sourceFolder = "src/main/resources/person_recognition/batches";
    static String destFolder = "src/main/resources/person_recognition/runs";

    public static void main(String[] args) throws IOException {
        if (args.length > 0) sourceFolder = args[0];
        if (args.length > 1) destFolder = args[1];
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
        return sourceFolder + "/person.ser";
    }

    @Override
    public String getModelDumpFileLocation() {
        return sourceFolder + "/person-dump.txt";
    }

    @Override
    public String getPerformanceReportFolder() {
        return destFolder + "/mcts-gameplayer";
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
