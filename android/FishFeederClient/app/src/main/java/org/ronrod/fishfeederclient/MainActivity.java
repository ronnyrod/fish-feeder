package org.ronrod.fishfeederclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.ronrod.fishfeederclient.bluetooth.BluetoothDeviceHandler;
import org.ronrod.fishfeederclient.model.Constants;
import org.ronrod.fishfeederclient.model.Feeder;
import org.ronrod.fishfeederclient.model.FeederListener;
import org.ronrod.fishfeederclient.model.FeederManager;
import org.ronrod.fishfeederclient.ui.DonutProgress;

public class MainActivity extends BluetoothConnectionActivity implements
        View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,
        FeederListener
{

    private static final int REQUEST_ADVANCED_SETTINGS = 2;


    private Handler mTimer;

    private Runnable refreshStatusTask = new Runnable() {
        @Override
        public void run() {
            send(feederManager.status());
        }
    };

   //UI
    Button btFeed;
    Button btChangeInterval;
    Button btChangeTimes;
    Button btFeedAtNight;

    DonutProgress donutProgress;
    SeekBar sbInterval;
    SeekBar sbTimes;
    TextView tvInterval;
    TextView tvTimes;
    CheckBox cbFeedAtNight;
    TextView tvFirmwareVersion;

    private FeederManager feederManager;

    private Handler mConnectingHandler;
    private Runnable connectingTask = new Runnable() {
        @Override
        public void run() {
            if(!isConnected) {
                int dProgress = donutProgress.getProgress();
                if(dProgress<donutProgress.getMax()) {
                    dProgress+=(donutProgress.getMax()/20);
                } else {
                    dProgress = 0;
                }
                donutProgress.setProgress(dProgress);
                mConnectingHandler.postDelayed(this,250);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connecting);

        donutProgress = (DonutProgress)findViewById(R.id.dp_feeding_cycle);
        donutProgress.setProgress(0);
        donutProgress.setText(getString(R.string.waiting_bluetooth));
        donutProgress.setInnerBottomText("");

        //Timer task
        mTimer = new Handler();

        //Feeder
        feederManager = new FeederManager(
                getResources().getInteger(R.integer.min_interval),
                getResources().getInteger(R.integer.max_interval),
                getResources().getInteger(R.integer.min_times),
                getResources().getInteger(R.integer.max_times),
                getResources().getInteger(R.integer.min_night_threshold),
                getResources().getInteger(R.integer.max_night_threshold)
        );

        feederManager.setListener(this);

        //Show connection progress
        mConnectingHandler = new Handler();
        mConnectingHandler.post(connectingTask);

        //Start bluetooth connection
        initBluetooth();

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(isConnected) {
            int id = item.getItemId();
            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                Intent intent = new Intent(this,AdvancedSettings.class);
                intent.putExtra(Constants.keys.NIGHT_THRESHOLD,feederManager.getFeeder().getLightSensor().getThreshold());
                intent.putExtra(Constants.keys.LIGHT_SENSOR_PIN,feederManager.getFeeder().getLightSensor().getPin());
                intent.putExtra(Constants.keys.STARTING_POS,feederManager.getFeeder().getServoVars().getStartPosition());
                intent.putExtra(Constants.keys.MID_POSITION,feederManager.getFeeder().getServoVars().getMidPosition());
                intent.putExtra(Constants.keys.END_POSITION,feederManager.getFeeder().getServoVars().getEndPosition());
                intent.putExtra(Constants.keys.LONG_DELAY,feederManager.getFeeder().getServoVars().getLongDelay());
                intent.putExtra(Constants.keys.SHORT_DELAY,feederManager.getFeeder().getServoVars().getShortDelay());
                intent.putExtra(Constants.keys.SERVO_PIN,feederManager.getFeeder().getServoVars().getPin());
                startActivityForResult(intent, REQUEST_ADVANCED_SETTINGS);
                return true;
            } else if (id == R.id.action_reset) {
                onMenuReset();
            } else if (id == R.id.action_save) {
                onSaveData();
            }
        } else {
            Toast.makeText(this,getString(R.string.feeder_not_connected),Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == REQUEST_ADVANCED_SETTINGS) {
            if(isConnected) {
                if(resultCode == RESULT_OK) {
                    String currentDonutText = donutProgress.getText();
                    donutProgress.setText(getString(R.string.uploading_servo_vars));
                    send(feederManager.changeNightThreshold(data.getIntExtra(Constants.keys.NIGHT_THRESHOLD, feederManager.getFeeder().getLightSensor().getThreshold())));
                    send(feederManager.changeLightSensorPin(data.getIntExtra(Constants.keys.LIGHT_SENSOR_PIN, feederManager.getFeeder().getLightSensor().getPin())));
                    send(feederManager.changeStartPosition(data.getIntExtra(Constants.keys.STARTING_POS, feederManager.getFeeder().getServoVars().getStartPosition())));
                    send(feederManager.changeMidPosition(data.getIntExtra(Constants.keys.MID_POSITION, feederManager.getFeeder().getServoVars().getMidPosition())));
                    send(feederManager.changeEndPosition(data.getIntExtra(Constants.keys.END_POSITION, feederManager.getFeeder().getServoVars().getEndPosition())));
                    send(feederManager.changeLongDelay(data.getIntExtra(Constants.keys.LONG_DELAY, feederManager.getFeeder().getServoVars().getLongDelay())));
                    send(feederManager.changeShortDelay(data.getIntExtra(Constants.keys.SHORT_DELAY, feederManager.getFeeder().getServoVars().getShortDelay())));
                    send(feederManager.changeServoPin(data.getIntExtra(Constants.keys.SERVO_PIN, feederManager.getFeeder().getServoVars().getPin())));
                    donutProgress.setText(currentDonutText);
                    //Requesting status to sync data
                    send(feederManager.status());
                }
            } else {
                donutProgress.setText(getString(R.string.feeder_not_connected));
            }
        }
    }

    @Override
    public void onDeviceConnected() {
        super.onDeviceConnected();
        send(feederManager.status());

        //load connected layout
        loadUIComponents();

    }


    /**
     *
     */
    private void onMenuReset() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.warning)
        .setMessage(R.string.reset_warning_message)
        .setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                send(feederManager.reset());
            }
        })
        .setNegativeButton(R.string.cancel,null)
        .setCancelable(true).create().show();
    }

    /**
     *
     */
    private void onSaveData() {
        send(feederManager.save());
        send(feederManager.status());
    }

    /**
     *
     */
    private void loadUIComponents() {
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

        btFeedAtNight = Button.class.cast(findViewById(R.id.bt_feed_at_night));
        btFeedAtNight.setOnClickListener(this);

        sbInterval = (SeekBar)findViewById(R.id.sb_interval);
        sbInterval.setOnSeekBarChangeListener(this);
        tvInterval = (TextView)findViewById(R.id.tv_feed_interval);
        sbTimes = (SeekBar)findViewById(R.id.sb_times);
        sbTimes.setOnSeekBarChangeListener(this);
        tvTimes = (TextView)findViewById(R.id.tv_feed_times);
        cbFeedAtNight = (CheckBox)findViewById(R.id.cb_feed_at_night);
        tvFirmwareVersion = (TextView)findViewById(R.id.tv_firmware_version);
    }

    @Override
    public void onError(int code) {

        if(code == BluetoothDeviceHandler.INITIAL_CONNECTION_FAILED
                ||code == BluetoothDeviceHandler.READING_WRITING_FAILURE) {
            isConnected = false;
            donutProgress.setProgress(0);
            donutProgress.setText(getString(R.string.connection_not_availble));
            donutProgress.setInnerBottomText(getString(R.string.click_to_try_again));
            mConnectingHandler.removeCallbacks(connectingTask);
            donutProgress.setOnClickListener(this);
        }  else {
            Toast.makeText(this,String.format("ERROR %d",code),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPacket(String packet) {
        feederManager.process(packet);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.bt_feed_now) {
            send(feederManager.forceFeed());
        } else if(id == R.id.bt_change_interval) {
            send(feederManager.changeFeedInterval(getResources().getInteger(R.integer.min_interval) + sbInterval.getProgress()));
            mTimer.postDelayed(refreshStatusTask, 1000);
        } else if(id == R.id.bt_change_times) {
            send(feederManager.changeFeedTimes(getResources().getInteger(R.integer.min_times) + sbTimes.getProgress()));
            mTimer.postDelayed(refreshStatusTask, 1000);
        } else if(id == R.id.bt_feed_at_night) {
            send(feederManager.changeFeedAtNightFlag(cbFeedAtNight.isChecked()));
            mTimer.postDelayed(refreshStatusTask, 1000);
        } else if(id == R.id.dp_feeding_cycle) {
            donutProgress.setOnClickListener(null);
            donutProgress.setText(getString(R.string.waiting_bluetooth));
            donutProgress.setInnerBottomText("");
            //Show connection progress
            mConnectingHandler = new Handler();
            mConnectingHandler.post(connectingTask);
            initBluetooth();
        }
    }



    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int id = seekBar.getId();
        if(id == R.id.sb_interval) {
            int interval = getResources().getInteger(R.integer.min_interval)+progress;
            if(interval != feederManager.getFeeder().getInterval()) {
                btChangeInterval.setEnabled(true);
            }
            tvInterval.setText(String.format(getString(R.string.feed_each_x_minutes), interval));
        } else if(id == R.id.sb_times) {
            int times = getResources().getInteger(R.integer.min_times)+progress;
            if(times != feederManager.getFeeder().getTimes()) {
                btChangeTimes.setEnabled(true);
            }
            tvTimes.setText(String.format(getString(R.string.feed_turns_per_feed_cycle), times));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStarting() {
        donutProgress.setText(getString(R.string.starting));
    }

    @Override
    public void onStatus(Feeder feeder) {
        donutProgress.setMax(feederManager.getFeeder().getInterval());
        donutProgress.setProgress(feederManager.getFeeder().getNextFeeding());
        donutProgress.setText(getString(R.string.next_feeding_in));
        int nextFeedingSeconds = (feederManager.getFeeder().getInterval() - feederManager.getFeeder().getNextFeeding());

        if(nextFeedingSeconds>=60) {
            int minutes = nextFeedingSeconds / 60;
            int seconds = (int)(60*((float)((float)nextFeedingSeconds / 60.0)-minutes)) ;
            if(seconds == 0) {
                donutProgress.setInnerBottomText(String.format(getString(R.string.in_minutes),nextFeedingSeconds/60));
            } else {
                StringBuilder sb = new StringBuilder()
                        .append(String.format(getString(R.string.in_minutes),minutes))
                        .append(" ")
                        .append(String.format(getString(R.string.in_seconds), seconds));
                donutProgress.setInnerBottomText(sb.toString());
            }

        } else if(nextFeedingSeconds<=0 &&
                !feederManager.getFeeder().isFeedAtNight() &&
                feederManager.getFeeder().getLastLightValue()>=feederManager.getFeeder().getLightSensor().getThreshold()) {
            donutProgress.setInnerBottomText(getString(R.string.waiting_for_light));
        } else {
            donutProgress.setInnerBottomText(String.format(getString(R.string.in_seconds), nextFeedingSeconds));
        }

        sbInterval.setProgress((feederManager.getFeeder().getInterval() - getResources().getInteger(R.integer.min_interval) * 60) / 60);
        onProgressChanged(sbInterval, sbInterval.getProgress(), false);
        sbTimes.setProgress(feederManager.getFeeder().getTimes() - getResources().getInteger(R.integer.min_times));
        onProgressChanged(sbTimes, sbTimes.getProgress(), false);
        btFeed.setEnabled(true);
        btChangeInterval.setEnabled(false);
        btChangeTimes.setEnabled(false);
        cbFeedAtNight.setChecked(feeder.isFeedAtNight());
        tvFirmwareVersion.setText(String.format(getString(R.string.firmware_version),feeder.getVersion()));
        mTimer.removeCallbacks(refreshStatusTask);
        mTimer.postDelayed(refreshStatusTask, 60000);
    }

    @Override
    public void onFeedingProgress(float progress) {
        donutProgress.setMax(100);
        donutProgress.setProgress((int) (progress * 100));
        donutProgress.setText(getString(R.string.feeding));
        donutProgress.setInnerBottomText(String.format("%d %%", donutProgress.getProgress()));
        btFeed.setEnabled(false);
        btChangeInterval.setEnabled(false);
        btChangeTimes.setEnabled(false);
        mTimer.removeCallbacks(refreshStatusTask);
        mTimer.postDelayed(refreshStatusTask, 1000);
    }
}
