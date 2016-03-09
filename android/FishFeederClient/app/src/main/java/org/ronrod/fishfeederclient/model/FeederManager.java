package org.ronrod.fishfeederclient.model;

import android.util.Log;

/**
 * Created by ronny on 13/02/16.
 */
public class FeederManager {
    private static final String LOGCAT = "FeederManager";


    private Feeder feeder;
    private FeederListener listener;
    private int minInterval;
    private int maxInterval;
    private int minFeedTimes;
    private int maxFeedTimes;
    private int minNightThreshold;
    private int maxNightThreshold;


    public FeederManager(int minInterval,int maxInterval,int minFeedTimes,int maxFeedTimes,int minNightThreshold,int maxNightThreshold) {
        this.feeder = new Feeder();
        this.minInterval = minInterval;
        this.maxInterval = maxInterval;
        this.minFeedTimes = minFeedTimes;
        this.maxFeedTimes = maxFeedTimes;
        this.minNightThreshold = minNightThreshold;
        this.maxNightThreshold = maxNightThreshold;
    }

    /**
     *
     * @param packet
     */
    public void process(String packet) {
        Log.d(LOGCAT, packet);
        if(packet.startsWith(Constants.responses.STATUS)) {
            Log.d(LOGCAT, "Status event received");
            String[] strings = packet.split(";");
            if(strings != null && strings.length>1) {
                String status = strings[0].replace(Constants.responses.STATUS,"");
                if(Constants.status.NORMAL.equals(status)) {
                    //Get parameters
                    Log.d(LOGCAT, "Status is NORMAL");
                    for(String var:strings) {
                        if(var.startsWith(Constants.responses.FEED_INTERVAL)) {
                            int interval = Integer.parseInt(var.replace(Constants.responses.FEED_INTERVAL, ""));
                            feeder.setInterval(interval);
                        }else if(var.startsWith(Constants.responses.VERSION)) {
                            int version = Integer.parseInt(var.replace(Constants.responses.VERSION, ""));
                            feeder.setVersion(version);
                        } else if (var.startsWith(Constants.responses.FEED_AT_NIGHT)) {
                            int feedAtNight = Integer.parseInt(var.replace(Constants.responses.FEED_AT_NIGHT, ""));
                            feeder.setFeedAtNight(feedAtNight > 0);
                        } else if (var.startsWith(Constants.responses.LAST_LIGHT_VALUE)) {
                            int lastLightValue = Integer.parseInt(var.replace(Constants.responses.LAST_LIGHT_VALUE, ""));
                            feeder.setLastLightValue(lastLightValue);
                        } else if (var.startsWith(Constants.responses.FEED_TIMES)) {
                            int feedTimes = Integer.parseInt(var.replace(Constants.responses.FEED_TIMES, ""));
                            feeder.setTimes(feedTimes);
                        } else if (var.startsWith(Constants.responses.LAST_FEED_TIME)) {
                            int lastFeedTime = Integer.parseInt(var.replace(Constants.responses.LAST_FEED_TIME, ""));
                            feeder.setLastFeedTime(lastFeedTime);
                        }else if (var.startsWith(Constants.responses.NEXT_FEEDING)) {
                            int nextFeeding = Integer.parseInt(var.replace(Constants.responses.NEXT_FEEDING, ""));
                            feeder.setNextFeeding(nextFeeding);
                        } else if (var.startsWith(Constants.responses.SERVO_VARS)) {
                            String[] sServoVars = var.replace(Constants.responses.SERVO_VARS,"").replace("[","").replace("]","").split("\\|");
                            if(sServoVars!=null && sServoVars.length>=6) {
                                feeder.getServoVars().setPin(Integer.parseInt(sServoVars[0]));
                                feeder.getServoVars().setStartPosition(Integer.parseInt(sServoVars[1]));
                                feeder.getServoVars().setMidPosition(Integer.parseInt(sServoVars[2]));
                                feeder.getServoVars().setEndPosition(Integer.parseInt(sServoVars[3]));
                                feeder.getServoVars().setLongDelay(Integer.parseInt(sServoVars[4]));
                                feeder.getServoVars().setShortDelay(Integer.parseInt(sServoVars[5]));
                            }
                        } else if (var.startsWith(Constants.responses.LIGHT_SENSOR)) {
                            String[] sLightSensorVars = var.replace(Constants.responses.LIGHT_SENSOR,"").replace("[","").replace("]","").split("\\|");
                            if(sLightSensorVars!=null && sLightSensorVars.length>=2) {
                                feeder.getLightSensor().setPin(Integer.parseInt(sLightSensorVars[0]));
                                feeder.getLightSensor().setThreshold(Integer.parseInt(sLightSensorVars[1]));
                            }
                        } else {

                        }
                    }

                    if(listener!=null) {
                        listener.onStatus(feeder);
                    }

                } else if(Constants.status.FEEDING.equals(status)) {
                    //Get progress
                    Log.d(LOGCAT, "Status is FEEDING");
                    try {
                        float progress = Float.parseFloat(strings[1]);
                        if(listener!=null) {
                            listener.onFeedingProgress(progress);
                        }
                    }catch (NumberFormatException ex) {
                        Log.e(LOGCAT,"progress has a incorrect format");
                        listener.onFeedingProgress(0);
                    }

                } else if(Constants.status.STARTING.equals(status)) {
                    //Inform, the feeder is starting
                    Log.d(LOGCAT, "Status is STARTING");
                    if(listener!=null) {
                        listener.onStarting();
                    }

                } else {
                    //Unknown status, maybe not supported firmware version?
                    Log.d(LOGCAT,"Status is UNKNOWN");
                }
            }
        }
    }

    /**
     *
     * @return
     */
    public String forceFeed() {
        return Constants.commands.FEED;
    }

    /**
     *
     * @return
     */
    public String reset() {
        return Constants.commands.RESET_DATA;
    }
    /**
     *
     * @return
     */
    public String save() {
        return Constants.commands.SAVE_DATA;
    }
    /**
     *
     * @return
     */
    public String status() {
        return Constants.commands.STATUS;
    }

    /**
     *
     * @param pin
     * @return
     */
    public String changeServoPin(int pin) {
        return new StringBuilder()
                .append(Constants.commands.CHANGE_SERVO_PIN)
                .append(pin).toString();
    }

    /**
     *
     * @param position
     * @return
     */
    public String changeStartPosition(int position) {
        return new StringBuilder()
                .append(Constants.commands.CHANGE_STARTING_POS)
                .append(String.format("%03d", position)).toString();
    }
    /**
     *
     * @param position
     * @return
     */
    public String changeMidPosition(int position) {
        return new StringBuilder()
                .append(Constants.commands.CHANGE_MID_POSITION)
                .append(String.format("%03d", position)).toString();
    }
    /**
     *
     * @param position
     * @return
     */
    public String changeEndPosition(int position) {
        return new StringBuilder()
                .append(Constants.commands.CHANGE_END_POSITION)
                .append(String.format("%03d", position)).toString();
    }

    /**
     *
     * @param delay
     * @return
     */
    public String changeLongDelay(int delay) {
        return new StringBuilder()
                .append(Constants.commands.CHANGE_LONG_DELAY)
                .append(String.format("%05d", delay)).toString();
    }
    /**
     *
     * @param delay
     * @return
     */
    public String changeShortDelay(int delay) {
        return new StringBuilder()
                .append(Constants.commands.CHANGE_SHORT_DELAY)
                .append(String.format("%05d", delay)).toString();
    }
    /**
     *
     * @param interval
     * @return
     */
    public String changeFeedInterval(int interval) {
        String output = null;
        if(interval>=minInterval && interval<=maxInterval) {
            output = new StringBuilder()
                    .append(Constants.commands.FEED_INTERVAL)
                    .append(String.format("%05d", interval * 60)).toString();
        }
        return output;
    }

    /**
     *
     * @param times
     * @return
     */
    public String changeFeedTimes(int times) {
        String output = null;
        if(times>=minFeedTimes && times<=maxFeedTimes) {
            output = new StringBuilder()
                    .append(Constants.commands.FEED_TIMES)
                    .append(times).toString();
        }
        return output;
    }

    /**
     *
     * @param threshold
     * @return
     */
    public String changeNightThreshold(int threshold) {
        String output = null;
        if(threshold>=minNightThreshold && threshold<=maxNightThreshold) {
            output = new StringBuilder()
                    .append(Constants.commands.CHANGE_LIGHT_THRESHOLD)
                    .append(threshold).toString();
        }
        return output;
    }

    /**
     *
     * @param pin
     * @return
     */
    public String changeLightSensorPin(int pin) {
        return new StringBuilder()
                .append(Constants.commands.CHANGE_LIGHT_SENSOR_PIN)
                .append(pin).toString();
    }

    /**
     *
     * @param allowFeedAtNight
     */
    public String changeFeedAtNightFlag(boolean allowFeedAtNight) {
        String output = null;
        if(allowFeedAtNight) {
            output = new StringBuilder(Constants.commands.FEED_AT_NIGHT).append("1").toString();
        } else {
            output = new StringBuilder(Constants.commands.FEED_AT_NIGHT).append("0").toString();
        }
        return output;
    }

    /**
     *
     * @return
     */
    public Feeder getFeeder() {
        return feeder;
    }

    /**
     *
     * @param listener
     */
    public void setListener(FeederListener listener) {
        this.listener = listener;
    }


}
