package org.ronrod.fishfeederclient.model;

/**
 * Created by ronny on 15/01/16.
 */
public class Feeder {
    private String status;
    private int interval;
    private boolean feedAtNight;
    private int lastLightValue;
    private int times;
    private long lastFeedTime;
    private int nextFeeding;
    private int version;
    private ServoVars servoVars;
    private LightSensor lightSensor;

    public Feeder() {
        servoVars = new ServoVars();
        lightSensor = new LightSensor();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public boolean isFeedAtNight() {
        return feedAtNight;
    }

    public void setFeedAtNight(boolean feedAtNight) {
        this.feedAtNight = feedAtNight;
    }

    public int getLastLightValue() {
        return lastLightValue;
    }

    public void setLastLightValue(int lastLightValue) {
        this.lastLightValue = lastLightValue;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public long getLastFeedTime() {
        return lastFeedTime;
    }

    public void setLastFeedTime(long lastFeedTime) {
        this.lastFeedTime = lastFeedTime;
    }

    public int getNextFeeding() {
        return nextFeeding;
    }

    public void setNextFeeding(int nextFeeding) {
        this.nextFeeding = nextFeeding;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public ServoVars getServoVars() {
        return servoVars;
    }

    public void setServoVars(ServoVars servoVars) {
        this.servoVars = servoVars;
    }

    public LightSensor getLightSensor() {
        return lightSensor;
    }

    public void setLightSensor(LightSensor lightSensor) {
        this.lightSensor = lightSensor;
    }
}
