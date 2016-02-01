package org.ronrod.fishfeederclient;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.ronrod.fishfeederclient.bluetooth.BluetoothConnectionManager;
import org.ronrod.fishfeederclient.bluetooth.BluetoothDeviceHandler;
import org.ronrod.fishfeederclient.comm.FishFeederEventHandler;
import org.ronrod.fishfeederclient.model.Constants;
import org.ronrod.fishfeederclient.model.Feeder;
import org.ronrod.fishfeederclient.ui.DonutProgress;

import java.io.IOException;
import java.util.Set;

public class MainActivity extends ActionBarActivity implements
        BluetoothDeviceHandler,
        FishFeederEventHandler,
        DialogInterface.OnClickListener,
        View.OnClickListener,
        SeekBar.OnSeekBarChangeListener
{
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String LOGCAT = "MainActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;

    private Handler mTimer;

    private Runnable feedCycleProgressTask = new Runnable() {
        @Override
        public void run() {
            status();
        }
    };

    private BluetoothConnectionManager btConnectionManager;
    String deviceMAC = null;
    String[] macList = null;
    Feeder feeder = null;

    //UI
    Button btFeed;
    Button btChangeInterval;
    Button btChangeTimes;

    DonutProgress donutProgress;
    SeekBar sbInterval;
    SeekBar sbTimes;
    TextView tvInterval;
    TextView tvTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        donutProgress = (DonutProgress)findViewById(R.id.dp_feeding_cycle);
        donutProgress.setProgress(0);
        donutProgress.setText(getString(R.string.waiting_bluetooth));
        donutProgress.setInnerBottomText("");

        btFeed = Button.class.cast(findViewById(R.id.bt_feed_now));
        btFeed.setOnClickListener(this);

        btChangeInterval = Button.class.cast(findViewById(R.id.bt_change_interval));
        btChangeInterval.setOnClickListener(this);

        btChangeTimes = Button.class.cast(findViewById(R.id.bt_change_times));
        btChangeTimes.setOnClickListener(this);

        sbInterval = (SeekBar)findViewById(R.id.sb_interval);
        sbInterval.setOnSeekBarChangeListener(this);
        tvInterval = (TextView)findViewById(R.id.tv_feed_interval);
        sbTimes = (SeekBar)findViewById(R.id.sb_times);
        sbTimes.setOnSeekBarChangeListener(this);
        tvTimes = (TextView)findViewById(R.id.tv_feed_times);

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message message) {
                if(message.what == BluetoothConnectionManager.BT_CONNECTED) {
                    try {
                        Thread.currentThread().sleep(10);
                    } catch (InterruptedException e) {
                    }
                    MainActivity.this.onDeviceConnected();
                } else if (message.what == BluetoothConnectionManager.BT_CLOSING) {
                    MainActivity.this.onDeviceClosing();
                } else if (message.what == BluetoothConnectionManager.BT_CLOSE) {
                    MainActivity.this.onDeviceClose();
                } else if (message.what == BluetoothConnectionManager.BT_PACKET_RECEIVED) {
                    MainActivity.this.onEvent((String) message.obj);
                }

            }
        };

        //Timer task
        mTimer = new Handler();

        //Feeder
        feeder = new Feeder();

        //Load settings from file
        loadSettings();

        //Start bluetooth connection
        initBluetooth();



    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Disconnect bluetooth
        disconnectBluetooth();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == RESULT_OK) {
                onDeviceReady();
            } else {
                //TODO: can not continues
            }
        }
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
        return builder.create();
    }

    /**
     *
     * @return
     */
    private boolean forceFeed() {
        return send(Constants.commands.FEED);
    }

    /**
     *
     * @return
     */
    private boolean reset() {
        return send(Constants.commands.RESET_DATA);
    }

    /**
     *
     * @return
     */
    private boolean status() {
        return send(Constants.commands.STATUS);
    }

    /**
     *
     * @param interval
     * @return
     */
    private boolean changeFeedInterval(int interval) {
        boolean output = false;
        if(interval>=getResources().getInteger(R.integer.min_interval) && interval<=getResources().getInteger(R.integer.max_interval)) {
            output = send(new StringBuilder()
                    .append(Constants.commands.FEED_INTERVAL)
                    .append(String.format("%05d", interval * 60)).toString());
        }
        return output;
    }

    /**
     *
     * @param times
     * @return
     */
    private boolean changeFeedTimes(int times) {
        boolean output = false;
        if(times>=getResources().getInteger(R.integer.min_times) && times<=getResources().getInteger(R.integer.max_times)) {
            output = send(new StringBuilder()
                    .append(Constants.commands.FEED_TIMES)
                    .append(times).toString());
        }
        return output;
    }
    /**
     *
     * @param data
     * @return
     */
    private boolean send(final String data) {
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
    public void onError(int code) {
        Toast.makeText(this,String.format("ERROR %d",code),Toast.LENGTH_LONG).show();
    }
    private void loadSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.app.SETTINGS_FILE,MODE_PRIVATE);
        deviceMAC = sharedPreferences.getString("deviceMAC",null);
    }
    @Override
    public void onEvent(String message) {
        //TODO: implement
        Log.d(LOGCAT,message);
        if(message.startsWith(Constants.responses.STATUS)) {
            Log.d(LOGCAT,"Status event received");
            String[] strings = message.split(";");
            if(strings != null && strings.length>1) {
                String status = strings[0].replace(Constants.responses.STATUS,"");
                if(Constants.status.NORMAL.equals(status)) {
                    //Get parameters
                    Log.d(LOGCAT, "Status is NORMAL");
                    for(String var:strings) {
                        if(var.startsWith(Constants.responses.FEED_INTERVAL)) {
                            int interval = Integer.parseInt(var.replace(Constants.responses.FEED_INTERVAL, ""));
                            feeder.setInterval(interval);
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
                            String[] sServoVars = var.split("\\|");
                            if(sServoVars!=null && sServoVars.length>=6) {
                                feeder.getServoVars().setPin(Integer.parseInt(sServoVars[0]));
                                feeder.getServoVars().setStartPosition(Integer.parseInt(sServoVars[1]));
                                feeder.getServoVars().setMidPosition(Integer.parseInt(sServoVars[2]));
                                feeder.getServoVars().setEndPosition(Integer.parseInt(sServoVars[3]));
                                feeder.getServoVars().setLongDelay(Integer.parseInt(sServoVars[4]));
                                feeder.getServoVars().setShortDelay(Integer.parseInt(sServoVars[5]));
                            }
                        } else {

                        }
                    }
                    sbInterval.setProgress(feeder.getInterval()/60);
                    sbTimes.setProgress(feeder.getTimes());
                    donutProgress.setMax(feeder.getInterval());
                    donutProgress.setProgress(feeder.getInterval() - feeder.getNextFeeding());
                    donutProgress.setText(getString(R.string.next_feeding_in));
                    donutProgress.setInnerBottomText(String.format(getString(R.string.in_minutes), feeder.getNextFeeding()));
                    btFeed.setEnabled(true);
                    btChangeInterval.setEnabled(false);
                    btChangeTimes.setEnabled(false);
                } else if(Constants.status.FEEDING.equals(status)) {
                    //Get progress
                    Log.d(LOGCAT, "Status is FEEDING");
                    float progress = Float.parseFloat(strings[1]);
                    donutProgress.setMax(100);
                    donutProgress.setProgress((int) (progress * 100));
                    donutProgress.setText(getString(R.string.feeding));
                    donutProgress.setInnerBottomText(String.format("%d %%", donutProgress.getProgress()));
                    btFeed.setEnabled(false);
                    btChangeInterval.setEnabled(false);
                    btChangeTimes.setEnabled(false);
                    mTimer.postAtTime(feedCycleProgressTask, 1000);

                } else if(Constants.status.STARTING.equals(status)) {
                    //Inform, the feeder is starting
                    Log.d(LOGCAT,"Status is STARTING");
                    donutProgress.setText(getString(R.string.starting));
                } else {
                    //Unknown status, maybe not supported firmware version?
                    Log.d(LOGCAT,"Status is UNKNOWN");
                }
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
        status();
        //TODO: allow operations
    }

    @Override
    public void onDeviceClosing() {
        //TODO: implement
    }

    @Override
    public void onDeviceClose() {
        //TODO: implement
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.bt_feed_now) {
            forceFeed();
        } else if(id == R.id.bt_change_interval) {
            changeFeedInterval(feeder.getInterval());
            mTimer.postAtTime(feedCycleProgressTask, 1000);
        } else if(id == R.id.bt_change_times) {
            changeFeedTimes(feeder.getTimes());
            mTimer.postAtTime(feedCycleProgressTask, 1000);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int id = seekBar.getId();
        if(id == R.id.sb_interval) {
            int interval = progress==0?getResources().getInteger(R.integer.min_interval):progress;
            if(interval != feeder.getInterval()) {
                btChangeInterval.setEnabled(true);
            }
            feeder.setInterval(interval);
            tvInterval.setText(String.format(getString(R.string.feed_each_x_minutes), feeder.getInterval()));
        } else if(id == R.id.sb_times) {
            int times = progress==0?getResources().getInteger(R.integer.min_times):progress;
            if(times != feeder.getTimes()) {
                btChangeTimes.setEnabled(true);
            }
            feeder.setTimes(times);
            tvTimes.setText(String.format(getString(R.string.feed_turns_per_feed_cycle), feeder.getTimes()));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
