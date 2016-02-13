package org.ronrod.fishfeederclient;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;

import org.ronrod.fishfeederclient.bluetooth.BluetoothConnectionManager;
import org.ronrod.fishfeederclient.bluetooth.BluetoothDeviceHandler;
import org.ronrod.fishfeederclient.model.Constants;

import java.io.IOException;
import java.util.Set;

/**
 * Created by ronny on 13/02/16.
 */
public abstract class BluetoothConnectionActivity extends ActionBarActivity
        implements BluetoothDeviceHandler,DialogInterface.OnClickListener {
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothConnectionManager btConnectionManager;
    String deviceMAC = null;
    String[] macList = null;

    boolean isConnected = false;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initial state
        isConnected = false;

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message message) {
                if(message.what == BluetoothConnectionManager.BT_CONNECTED) {
                    try {
                        Thread.currentThread().sleep(10);
                    } catch (InterruptedException e) {
                    }
                    BluetoothConnectionActivity.this.onDeviceConnected();
                } else if (message.what == BluetoothConnectionManager.BT_CLOSING) {
                    BluetoothConnectionActivity.this.onDeviceClosing();
                } else if (message.what == BluetoothConnectionManager.BT_CLOSE) {
                    BluetoothConnectionActivity.this.onDeviceClose();
                } else if (message.what == BluetoothConnectionManager.BT_PACKET_RECEIVED) {
                    BluetoothConnectionActivity.this.onPacket((String) message.obj);
                }

            }
        };

        //Load settings from file
        loadSettings();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Disconnect bluetooth
        disconnectBluetooth();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == RESULT_OK) {
                onDeviceReady();
            } else {
                isConnected = false;
                onError(BluetoothDeviceHandler.DEVICE_NOT_CONNECTED);
            }
        }
    }
    @Override
    public void onDeviceReady() {
        if(deviceMAC == null) {
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            macList = new String[bondedDevices.size()];
            int i = 0;
            for (BluetoothDevice dev:bondedDevices) {
                macList[i]= new StringBuilder().append(dev.getName()).append("|").append(dev.getAddress()).toString();
                i++;
            }
            createSelectDeviceDialog().show();

        } else {
            connectBluetooth();
        }
    }

    @Override
    public void onDeviceConnected() {
        isConnected = true;
    }

    @Override
    public void onDeviceClosing() {
        isConnected = false;
    }

    @Override
    public void onDeviceClose() {
        isConnected = false;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        String[] strings = macList[which].split("\\|");
        if(strings!=null && strings.length>=2) {
            deviceMAC = strings[1];
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.app.SETTINGS_FILE,MODE_PRIVATE);
            sharedPreferences.edit().putString("deviceMAC",deviceMAC).commit();
            connectBluetooth();
        } else {
            onError(BluetoothDeviceHandler.MAC_NOT_DEFINED);
        }

    }
    /**
     *
     * @param data
     * @return
     */
    protected boolean send(final String data) {
        boolean output = false;
        if(btConnectionManager!=null && btConnectionManager.isConnected()) {
            try {
                btConnectionManager.write(data);
                output = true;
            } catch (IOException e) {
                output = false;
            }
        }
        return output;
    }
    /**
     *
     */
    void connectBluetooth() {
        if(btConnectionManager != null && btConnectionManager.isConnected()) {
            onError(BluetoothDeviceHandler.ALREADY_CONNECTED);
        } else {
            if(BluetoothAdapter.checkBluetoothAddress(deviceMAC)) {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceMAC);
                try {
                    btConnectionManager = new BluetoothConnectionManager(device,mBluetoothAdapter,mHandler);
                    btConnectionManager.start();
                } catch (IOException e) {
                    onError(BluetoothDeviceHandler.CREATION_FAILED);
                }
            }
        }
    }
    /**
     *
     */
    protected void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            onError(BluetoothDeviceHandler.COMM_NOT_AVAILABLE);
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                onDeviceReady();
            }
        }
    }

    /**
     * Releases bluetooth connection and resources
     */
    void disconnectBluetooth() {
        if(btConnectionManager != null) {
            try {
                btConnectionManager.cancel();
            } catch (IOException e) {
                onError(BluetoothDeviceHandler.DISCONNECTION_FAILED);
            }
        }
    }

    /**
     *
     * @return
     */
    private AlertDialog createSelectDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_a_device);
        builder.setItems(macList, this);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isConnected = false;
                onError(BluetoothDeviceHandler.SELECT_DEVICE_CANCELED);
            }
        });
        return builder.create();
    }
    private void loadSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.app.SETTINGS_FILE,MODE_PRIVATE);
        deviceMAC = sharedPreferences.getString("deviceMAC",null);
    }
    /**
     *
     * @param code
     */
    abstract void onError(int code);


}
