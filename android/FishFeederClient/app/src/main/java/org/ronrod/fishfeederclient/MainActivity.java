package org.ronrod.fishfeederclient;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.ronrod.fishfeederclient.bluetooth.BluetoothConnectionManager;
import org.ronrod.fishfeederclient.comm.FishFeederEventHandler;

public class MainActivity extends ActionBarActivity implements FishFeederEventHandler {

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;

    private BluetoothConnectionManager btConnectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == BluetoothConnectionManager.BT_PACKET_RECEIVED) {
                    MainActivity.this.onEvent((String) msg.obj);
                }
            }
        };

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEvent(String message) {
        //TODO: implement
    }
}
