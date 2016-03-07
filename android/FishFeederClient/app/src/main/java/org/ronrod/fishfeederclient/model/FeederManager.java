package org.ronrod.fishfeederclient.model;

import android.util.Log;

/**
 * Created by ronny on 13/02/16.
 */
public class FeederManager {
    private static final String LOGCAT = "FeederManager";


    private Feeder feeder;
    private FeederListener listener;


    public FeederManager() {
        this.feeder = new Feeder();
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
