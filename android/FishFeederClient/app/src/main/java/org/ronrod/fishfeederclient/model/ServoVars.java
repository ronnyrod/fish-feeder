package org.ronrod.fishfeederclient.model;

/**
 * Created by ronny on 15/01/16.
 */
public class ServoVars {
    private int pin;
    private int startPosition;
    private int midPosition;
    private int endPosition;
    private int longDelay;
    private int shortDelay;

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public int getMidPosition() {
        return midPosition;
    }

    public void setMidPosition(int midPosition) {
        this.midPosition = midPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    public int getLongDelay() {
        return longDelay;
    }

    public void setLongDelay(int longDelay) {
        this.longDelay = longDelay;
    }

    public int getShortDelay() {
        return shortDelay;
    }

    public void setShortDelay(int shortDelay) {
        this.shortDelay = shortDelay;
    }
}
