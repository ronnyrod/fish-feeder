package org.ronrod.fishfeederclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.ronrod.fishfeederclient.model.Constants;

public class AdvancedSettings extends ActionBarActivity  implements  SeekBar.OnSeekBarChangeListener, View.OnClickListener{

    //Seekbars
    SeekBar sbNightThreshold;
    SeekBar sbLightSensorPin;
    SeekBar sbStartPos;
    SeekBar sbMidPos;
    SeekBar sbEndPos;
    SeekBar sbLongDelay;
    SeekBar sbShortDelay;
    SeekBar sbServoPin;


    //Text views
    TextView tvNightThreshold;
    TextView tvLightSensorPin;
    TextView tvStartPos;
    TextView tvMidPos;
    TextView tvEndPos;
    TextView tvLongDelay;
    TextView tvShortDelay;
    TextView tvServoPin;


    //Button
    Button btUploadSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servo_settings);

        sbNightThreshold =  (SeekBar)findViewById(R.id.sb_night_threshold);
        sbLightSensorPin =  (SeekBar)findViewById(R.id.sb_light_sensor_pin);
        sbStartPos =  (SeekBar)findViewById(R.id.sb_start_postition);
        sbMidPos =  (SeekBar)findViewById(R.id.sb_mid_postition);
        sbEndPos =  (SeekBar)findViewById(R.id.sb_end_postition);
        sbLongDelay =  (SeekBar)findViewById(R.id.sb_long_delay);
        sbShortDelay =  (SeekBar)findViewById(R.id.sb_short_delay);
        sbServoPin = (SeekBar)findViewById(R.id.sb_servo_pin);

        tvNightThreshold = (TextView)findViewById(R.id.tv_night_threshold);
        tvLightSensorPin = (TextView)findViewById(R.id.tv_light_sensor_pin);
        tvStartPos = (TextView)findViewById(R.id.tv_start_position);
        tvMidPos = (TextView)findViewById(R.id.tv_mid_position);
        tvEndPos = (TextView)findViewById(R.id.tv_end_position);
        tvLongDelay = (TextView)findViewById(R.id.tv_long_delay);
        tvShortDelay = (TextView)findViewById(R.id.tv_short_delay);
        tvServoPin = (TextView)findViewById(R.id.tv_servo_pin);

        btUploadSettings = (Button)findViewById(R.id.bt_save_servo_settings);

        sbNightThreshold.setOnSeekBarChangeListener(this);
        sbLightSensorPin.setOnSeekBarChangeListener(this);
        sbStartPos.setOnSeekBarChangeListener(this);
        sbMidPos.setOnSeekBarChangeListener(this);
        sbEndPos.setOnSeekBarChangeListener(this);
        sbLongDelay.setOnSeekBarChangeListener(this);
        sbShortDelay.setOnSeekBarChangeListener(this);
        sbServoPin.setOnSeekBarChangeListener(this);

        btUploadSettings.setOnClickListener(this);
        btUploadSettings.setEnabled(false);

        //Initial values
        sbNightThreshold.setProgress(getIntent().getIntExtra(Constants.keys.NIGHT_THRESHOLD, 0));
        onProgressChanged(sbNightThreshold, sbNightThreshold.getProgress(), false);
        sbLightSensorPin.setProgress(getIntent().getIntExtra(Constants.keys.LIGHT_SENSOR_PIN, 0));
        onProgressChanged(sbLightSensorPin, sbLightSensorPin.getProgress(), false);

        sbStartPos.setProgress(getIntent().getIntExtra(Constants.keys.STARTING_POS, 0));
        onProgressChanged(sbStartPos, sbStartPos.getProgress(), false);
        sbMidPos.setProgress(getIntent().getIntExtra(Constants.keys.MID_POSITION, 0));
        onProgressChanged(sbMidPos, sbMidPos.getProgress(), false);
        sbEndPos.setProgress(getIntent().getIntExtra(Constants.keys.END_POSITION, 0));
        onProgressChanged(sbEndPos, sbEndPos.getProgress(), false);
        sbLongDelay.setProgress(getIntent().getIntExtra(Constants.keys.LONG_DELAY, 0));
        onProgressChanged(sbLongDelay, sbLongDelay.getProgress(), false);
        sbShortDelay.setProgress(getIntent().getIntExtra(Constants.keys.SHORT_DELAY, 0));
        onProgressChanged(sbShortDelay, sbShortDelay.getProgress(), false);
        sbServoPin.setProgress(getIntent().getIntExtra(Constants.keys.SERVO_PIN, 0));
        onProgressChanged(sbServoPin, sbServoPin.getProgress(), false);

        //Default value
        setResult(RESULT_CANCELED);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int id = seekBar.getId();
        switch (id) {
            case R.id.sb_night_threshold:
                int value = getResources().getInteger(R.integer.min_night_threshold)+progress;
                tvNightThreshold.setText(String.format(getString(R.string.night_threshold),value));
                break;
            case R.id.sb_light_sensor_pin:
                value = getResources().getInteger(R.integer.min_light_sensor_pin)+progress;
                tvLightSensorPin.setText(String.format(getString(R.string.light_sensor_pin),value));
                break;
            case R.id.sb_start_postition:
                value = getResources().getInteger(R.integer.min_position)+progress;
                tvStartPos.setText(String.format(getString(R.string.start_position),value));
                break;
            case R.id.sb_mid_postition:
                value = getResources().getInteger(R.integer.min_position)+progress;
                tvMidPos.setText(String.format(getString(R.string.mid_position),value));
                break;
            case R.id.sb_end_postition:
                value = getResources().getInteger(R.integer.min_position)+progress;
                tvEndPos.setText(String.format(getString(R.string.end_position),value));
                break;
            case R.id.sb_long_delay:
                value = getResources().getInteger(R.integer.min_long_delay)+progress;
                tvLongDelay.setText(String.format(getString(R.string.long_delay),value));
                break;
            case R.id.sb_short_delay:
                value = getResources().getInteger(R.integer.min_short_delay)+progress;
                tvShortDelay.setText(String.format(getString(R.string.short_delay),value));
                break;
            case R.id.sb_servo_pin:
                value = getResources().getInteger(R.integer.min_servo_pin)+progress;
                tvServoPin.setText(String.format(getString(R.string.servo_pin),value));
                break;
        }
        btUploadSettings.setEnabled(true);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.bt_save_servo_settings) {
            Intent intent = new Intent();
            intent.putExtra(Constants.keys.NIGHT_THRESHOLD,sbNightThreshold.getProgress());
            intent.putExtra(Constants.keys.LIGHT_SENSOR_PIN,sbLightSensorPin.getProgress());
            intent.putExtra(Constants.keys.STARTING_POS,sbStartPos.getProgress());
            intent.putExtra(Constants.keys.MID_POSITION,sbMidPos.getProgress());
            intent.putExtra(Constants.keys.END_POSITION,sbEndPos.getProgress());
            intent.putExtra(Constants.keys.LONG_DELAY,sbLongDelay.getProgress());
            intent.putExtra(Constants.keys.SHORT_DELAY, sbShortDelay.getProgress());
            intent.putExtra(Constants.keys.SERVO_PIN, sbServoPin.getProgress());
            setResult(RESULT_OK,intent);
            finish();
        }
    }
}
