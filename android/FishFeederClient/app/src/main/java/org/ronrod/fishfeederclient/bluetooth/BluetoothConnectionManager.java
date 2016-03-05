package org.ronrod.fishfeederclient.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by ronny on 12/01/16.
 */
public class BluetoothConnectionManager extends Thread {
    public static final UUID APP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String LOGCAT = "BTConnectionManager";

    public static final int BT_CONNECTED = 0;
    public static final int BT_CLOSING = 1;
    public static final int BT_PACKET_RECEIVED = 2;
    public static final int BT_CLOSE = 3;
    public static final int BT_CONNECTION_ERROR = 4;
    public static final int BT_INITIAL_CONN_ERROR = 5;

    private final BluetoothSocket mmSocket;
    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private boolean connected = false;

    public BluetoothConnectionManager(BluetoothDevice device, BluetoothAdapter mBluetoothAdapter, Handler mHandler) throws IOException {
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.mHandler = mHandler;
        this.mmSocket = device.createRfcommSocketToServiceRecord(APP_UUID);
    }

    @Override
    public void run() {
        // Cancel discovery because it will slow down the connection
        mBluetoothAdapter.cancelDiscovery();

        if(connect()) {
            try {


                mmInStream = mmSocket.getInputStream();
                mmOutStream = mmSocket.getOutputStream();

                connected = true;

                // Keep listening to the InputStream until an exception occurs
                byte[] buffer = new byte[1024];
                StringBuilder sb = new StringBuilder();
                //Connection event
                mHandler.obtainMessage(BT_CONNECTED).sendToTarget();

                while (connected) {

                    int nBytes = mmInStream.read(buffer);
                    if(nBytes>0) {
                        for(int i=0;i<nBytes;i++) {
                            sb.append((char)buffer[i]);
                        }
                        int lineSeparatorIndex = sb.indexOf("\r\n");
                        if(lineSeparatorIndex>=0) {
                            mHandler.obtainMessage(BT_PACKET_RECEIVED,sb.substring(0,lineSeparatorIndex)).sendToTarget();
                            sb.delete(0,lineSeparatorIndex+2);
                        }

                    } else {
                        connected = false;
                    }
                }

            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.e(LOGCAT, "Connection has failed", connectException);
                mHandler.obtainMessage(BT_CONNECTION_ERROR).sendToTarget();
            } finally {
                //Closing event
                mHandler.obtainMessage(BT_CLOSING).sendToTarget();
                try {
                    if(mmSocket != null) {
                        mmSocket.close();
                    }
                } catch (IOException closeException) {
                    Log.e(LOGCAT, "Close connection", closeException);
                }
            }
            //Closing event
            mHandler.obtainMessage(BT_CLOSE).sendToTarget();
        } else {
            //Impossible get a valid connection
            mHandler.obtainMessage(BT_INITIAL_CONN_ERROR).sendToTarget();
        }


    }

    /* Call this from the main activity to send data to the remote device */
    public void write(String data) throws IOException {
        if(connected && data!=null) {
            mmOutStream.write(new StringBuilder().append(data).append("\n").toString().getBytes());
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() throws IOException  {
        connected = false;
        if(mmSocket!=null) {
            mmSocket.close();
        }
    }
    /*

     */
    public boolean isConnected() {
        return this.connected;
    }

    /**
     *
     * @return
     */
    private boolean connect() {
        boolean output = false;
        // Connect the device through the socket. This will block
        // until it succeeds or throws an exception
        try {
            mmSocket.connect();
            output = true;
        } catch (IOException e) {
            Log.e(LOGCAT, "Connection has failed", e);
        }
        return output;
    }
}