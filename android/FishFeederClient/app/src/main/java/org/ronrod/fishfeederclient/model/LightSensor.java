package org.ronrod.fishfeederclient.model;

/**
 * Created by ronny on 6/03/16.
 */
public class LightSensor {
    private int pin;
    private int threshold;

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
