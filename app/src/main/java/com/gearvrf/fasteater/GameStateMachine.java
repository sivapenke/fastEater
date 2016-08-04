package com.gearvrf.fasteater;

/**
 * Created by b1.miller on 7/29/2016.
 */
public class GameStateMachine {

    public enum GameStatus {
        STATE_GAME_IN_PROGRESS, //optimization
        STATE_GAME_LOAD,
        STATE_GAME_STARTED,
        STATE_GAME_END
    }

    private int currentLevel;
    private GameStatus status = GameStatus.STATE_GAME_END;

    public GameStateMachine() {
    }

    public void setCurrentLevel(int newLevel) {
        currentLevel = newLevel;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

}
