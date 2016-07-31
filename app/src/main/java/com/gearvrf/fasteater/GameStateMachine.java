package com.gearvrf.fasteater;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by b1.miller on 7/29/2016.
 */
public class GameStateMachine {

    public enum GameStatus {
        WELCOME_SCREEN, IN_PROCESS
    }

    private Player player;
    private List<FlyingItem> flyingItems;
    private Random random;
    private int currentLevel;
    private GameStatus status;

    public GameStateMachine() {

        random = new Random(System.currentTimeMillis());

        restartGame();
    }

    public void setCurrentLevel(int newLevel) {
        currentLevel = newLevel;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getScore() {
        return player.getCurrentScore();
    }

    public void setScore(int newScore) {
        player.setCurrentScore(newScore);
    }

    public void incrementScore(int valueIncrease) {
        player.incrementScore(valueIncrease);
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    /**
     * Restart a new game. Reset all status back to the beginning
     */
    public void restartGame() {
        player = new Player();

        currentLevel = 1;

        setStatus(GameStatus.WELCOME_SCREEN);

        flyingItems = new ArrayList<FlyingItem>();
        FlyingItem hamburger = new FlyingItem("hamburger", "hamburger.obj", 10);
        flyingItems.add(hamburger);
        FlyingItem hotdog = new FlyingItem("hotdog", "hotdog.obj", 5);
        flyingItems.add(hotdog);
        FlyingItem bomb = new FlyingItem("bomb", "bomb.obj", -1);
        flyingItems.add(bomb);
    }

    public void startGame() {

        setStatus(GameStatus.IN_PROCESS);
    }

    public void stopGame() {

        restartGame();
    }

    public List<FlyingItem> getCurrentFlyingItems() {
        List<FlyingItem> currentFlyingItems = new ArrayList<FlyingItem>();
        for (FlyingItem item : flyingItems) {
            if (item.isInMotion()) {
                currentFlyingItems.add(item);
            }
        }
        return currentFlyingItems;
    }

    // TODO: probably not run() / while, but callbacks to change state
    public void run() {

        while (!player.isDead()) {

            int itemIndex = random.nextInt(flyingItems.size());
            FlyingItem currentItem = flyingItems.get(itemIndex);

            // TODO: display item

            // TODO: determine if player catches the item, or avoids it

            boolean userCaughtItem = true; // TODO: set dynamically !

            if (currentItem.isBomb() && userCaughtItem) {
                player.loseALife();
            } else {
                player.incrementScore(currentItem.getPointValue());
            }

            // TODO: update display of score board
        }
    }
}
