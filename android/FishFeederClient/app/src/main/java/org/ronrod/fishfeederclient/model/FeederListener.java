package org.ronrod.fishfeederclient.model;

/**
 * Created by ronny on 13/01/2016.
 */
public interface FeederListener {
    void onStarting();
    void onStatus(Feeder feeder);
    void onFeedingProgress(float progress);
}
