package com.gearvrf.fasteater;

/**
 * Created by b1.miller on 7/29/2016.
 */
public class FlyingItem {

    private static int DEFAULT_SPEED = 25;


    public enum ItemStatus {
        HIDDEN, IN_MOTION, ARRIVED_AT_CAMERA
    }

    private String name;
    private String assetFilename;
    private int pointValue;
//    private int speed;
    private ItemStatus currentStatus;

    public FlyingItem(String name, String assetFilename, int pointValue) {
        this.name = name;
        this.assetFilename = assetFilename;
        this.pointValue = pointValue;
  //      this.speed = DEFAULT_SPEED;
        this.currentStatus = ItemStatus.HIDDEN;
    }

    /*
    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPointValue() {
        return pointValue;
    }

    public void setPointValue(int pointValue) {
        this.pointValue = pointValue;
    }

    public boolean isBomb() {
        return (pointValue < 0);
    }

    public ItemStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(ItemStatus newStatus) {
        currentStatus = newStatus;
    }

    public boolean isHidden() {
        return (currentStatus == ItemStatus.HIDDEN);
    }

    public boolean isInMotion() {
        return (currentStatus == ItemStatus.IN_MOTION);
    }

    public boolean isArrived() {
        return (currentStatus == ItemStatus.ARRIVED_AT_CAMERA);
    }
}
