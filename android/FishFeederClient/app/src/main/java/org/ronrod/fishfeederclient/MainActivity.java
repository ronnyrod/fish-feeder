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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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

    private static final int REQUEST_SERVO_SETTINGS = 2;


    private Handler mTimer;

    private Runnable refreshStatusTask = new Runnable() {
        @Override
        public void run() {
            status();
        }
    };

   //UI
    Button btFeed;
    Button btChangeInterval;
    Button btChangeTimes;

    DonutProgress donutProgress;
    SeekBar sbInterval;
    SeekBar sbTimes;
    TextView tvInterval;
    TextView tvTimes;
    private FeederManager feederManager;


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

        //Timer task
        mTimer = new Handler();

        //Feeder
        feederManager = new FeederManager();
        feederManager.setListener(this);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if(isConnected) {
                Intent intent = new Intent(this,ServoSettings.class);
                intent.putExtra(Constants.keys.STARTING_POS,feederManager.getFeeder().getServoVars().getStartPosition());
                intent.putExtra(Constants.keys.MID_POSITION,feederManager.getFeeder().getServoVars().getMidPosition());
                intent.putExtra(Constants.keys.END_POSITION,feederManager.getFeeder().getServoVars().getEndPosition());
                intent.putExtra(Constants.keys.LONG_DELAY,feederManager.getFeeder().getServoVars().getLongDelay());
                intent.putExtra(Constants.keys.SHORT_DELAY,feederManager.getFeeder().getServoVars().getShortDelay());
                intent.putExtra(Constants.keys.SERVO_PIN,feederManager.getFeeder().getServoVars().getPin());
                startActivityForResult(intent, REQUEST_SERVO_SETTINGS);
            } else {
                Toast.makeText(this,getString(R.string.feeder_not_connected),Toast.LENGTH_SHORT).show();

            }

            return true;
        } else if (id == R.id.action_reset) {
            onMenuReset();
        } else if (id == R.id.action_save) {
            onSaveData();
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == REQUEST_SERVO_SETTINGS) {
            if(isConnected) {
                if(resultCode == RESULT_OK) {
                    String currentDonutText = donutProgress.getText();
                    donutProgress.setText(getString(R.string.uploading_servo_vars));
                    changeStartPosition(data.getIntExtra(Constants.keys.STARTING_POS, feederManager.getFeeder().getServoVars().getStartPosition()));
                    changeMidPosition(data.getIntExtra(Constants.keys.MID_POSITION, feederManager.getFeeder().getServoVars().getMidPosition()));
                    changeEndPosition(data.getIntExtra(Constants.keys.END_POSITION, feederManager.getFeeder().getServoVars().getEndPosition()));
                    changeLongDelay(data.getIntExtra(Constants.keys.LONG_DELAY, feederManager.getFeeder().getServoVars().getLongDelay()));
                    changeShortDelay(data.getIntExtra(Constants.keys.SHORT_DELAY, feederManager.getFeeder().getServoVars().getShortDelay()));
                    changeServoPin(data.getIntExtra(Constants.keys.SERVO_PIN, feederManager.getFeeder().getServoVars().getPin()));
                    donutProgress.setText(currentDonutText);
                    //Requesting status to sync data
                    status();
                }
            } else {
                donutProgress.setText(getString(R.string.feeder_not_connected));
            }
        }
    }

    @Override
    public void onDeviceConnected() {
        super.onDeviceConnected();
        status();
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
    private boolean save() {
        return send(Constants.commands.SAVE_DATA);
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
     * @param pin
     * @return
     */
    private boolean changeServoPin(int pin) {
        return send(new StringBuilder()
                .append(Constants.commands.CHANGE_SERVO_PIN)
                .append(pin).toString());
    }

    /**
     *
     * @param position
     * @return
     */
    private boolean changeStartPosition(int position) {
        return send(new StringBuilder()
                .append(Constants.commands.CHANGE_STARTING_POS)
                .append(String.format("%03d", position)).toString());
    }
    /**
     *
     * @param position
     * @return
     */
    private boolean changeMidPosition(int position) {
        return send(new StringBuilder()
                .append(Constants.commands.CHANGE_MID_POSITION)
                .append(String.format("%03d", position)).toString());
    }
    /**
     *
     * @param position
     * @return
     */
    private boolean changeEndPosition(int position) {
        return send(new StringBuilder()
                .append(Constants.commands.CHANGE_END_POSITION)
                .append(String.format("%03d", position)).toString());
    }

    /**
     *
     * @param delay
     * @return
     */
    private boolean changeLongDelay(int delay) {
        return send(new StringBuilder()
                .append(Constants.commands.CHANGE_LONG_DELAY)
                .append(String.format("%05d", delay)).toString());
    }
    /**
     *
     * @param delay
     * @return
     */
    private boolean changeShortDelay(int delay) {
        return send(new StringBuilder()
                .append(Constants.commands.CHANGE_SHORT_DELAY)
                .append(String.format("%05d", delay)).toString());
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
     */
    private void onMenuReset() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.warning)
        .setMessage(R.string.reset_warning_message)
        .setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reset();
            }
        })
        .setNegativeButton(R.string.cancel,null)
        .setCancelable(true).create().show();
    }

    /**
     *
     */
    private void onSaveData() {
        save();
        status();
    }
    @Override
    public void onError(int code) {
        Toast.makeText(this,String.format("ERROR %d",code),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPacket(String packet) {
        feederManager.process(packet);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.bt_feed_now) {
            forceFeed();
        } else if(id == R.id.bt_change_interval) {
            changeFeedInterval(getResources().getInteger(R.integer.min_interval)+sbInterval.getProgress());
            mTimer.postDelayed(refreshStatusTask, 1000);
        } else if(id == R.id.bt_change_times) {
            changeFeedTimes(getResources().getInteger(R.integer.min_times)+sbTimes.getProgress());
            mTimer.postDelayed(refreshStatusTask, 1000);
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

        } else {
            donutProgress.setInnerBottomText(String.format(getString(R.string.in_seconds),nextFeedingSeconds));
        }

        sbInterval.setProgress((feederManager.getFeeder().getInterval() - getResources().getInteger(R.integer.min_interval) * 60) / 60);
        onProgressChanged(sbInterval, sbInterval.getProgress(), false);
        sbTimes.setProgress(feederManager.getFeeder().getTimes() - getResources().getInteger(R.integer.min_times));
        onProgressChanged(sbTimes, sbTimes.getProgress(), false);
        btFeed.setEnabled(true);
        btChangeInterval.setEnabled(false);
        btChangeTimes.setEnabled(false);
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
