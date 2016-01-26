package org.ronrod.fishfeederclient.model;

/**
 * Created by ronny on 15/01/16.
 */
public class Feeder {
    private String status;
    private int interval;
    private int times;
    private int lastFeedTime;
    private int nextFeeding;
    private ServoVars servoVars;

    public Feeder() {
        servoVars = new ServoVars();
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

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getLastFeedTime() {
        return lastFeedTime;
    }

    public void setLastFeedTime(int lastFeedTime) {
        this.lastFeedTime = lastFeedTime;
    }

    public int getNextFeeding() {
        return nextFeeding;
    }

    public void setNextFeeding(int nextFeeding) {
        this.nextFeeding = nextFeeding;
    }

    public ServoVars getServoVars() {
        return servoVars;
    }

    public void setServoVars(ServoVars servoVars) {
        this.servoVars = servoVars;
    }
}
