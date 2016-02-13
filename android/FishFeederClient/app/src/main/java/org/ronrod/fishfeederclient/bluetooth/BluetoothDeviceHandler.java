package org.ronrod.fishfeederclient.bluetooth;

/**
 * Created by ronny on 14/01/16.
 */
public interface BluetoothDeviceHandler {
    static final int DISCONNECTION_FAILED = -2;
    static final int COMM_NOT_AVAILABLE = -1;
    static final int CREATION_FAILED = -3;
    static final int MAC_NOT_DEFINED = -4;
    static final int ALREADY_CONNECTED = -5;
    static final int WRITING_FAILURE = -6;
    static final int SELECT_DEVICE_CANCELED = -7;
    static final int DEVICE_NOT_CONNECTED = -8;

    void onDeviceReady();
    void onDeviceConnected();
    void onDeviceClosing();
    void onDeviceClose();
    void onPacket(String packet);
}
