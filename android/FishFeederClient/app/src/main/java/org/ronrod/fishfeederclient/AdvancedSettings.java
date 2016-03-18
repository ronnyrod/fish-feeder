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

        btUploadSettings.setOnClickListener(this);
        btUploadSettings.setEnabled(false);

        //Initial values and listeners
        setupSeekBarValueFromIntent(sbNightThreshold, Constants.keys.NIGHT_THRESHOLD, R.integer.min_night_threshold);
        setupSeekBarValueFromIntent(sbLightSensorPin, Constants.keys.LIGHT_SENSOR_PIN, R.integer.min_light_sensor_pin);
        setupSeekBarValueFromIntent(sbStartPos, Constants.keys.STARTING_POS, R.integer.min_position);
        setupSeekBarValueFromIntent(sbMidPos, Constants.keys.MID_POSITION, R.integer.min_position);
        setupSeekBarValueFromIntent(sbEndPos, Constants.keys.END_POSITION, R.integer.min_position);
        setupSeekBarValueFromIntent(sbLongDelay, Constants.keys.LONG_DELAY, R.integer.min_long_delay);
        setupSeekBarValueFromIntent(sbShortDelay, Constants.keys.SHORT_DELAY, R.integer.min_short_delay);
        setupSeekBarValueFromIntent(sbServoPin, Constants.keys.SERVO_PIN, R.integer.min_servo_pin);


        //Default value
        setResult(RESULT_CANCELED);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int id = seekBar.getId();
        switch (id) {
            case R.id.sb_night_threshold:
                int value = progress+getResources().getInteger(R.integer.min_night_threshold);
                tvNightThreshold.setText(String.format(getString(R.string.night_threshold),value));
                break;
            case R.id.sb_light_sensor_pin:
                value = progress+getResources().getInteger(R.integer.min_light_sensor_pin);
                tvLightSensorPin.setText(String.format(getString(R.string.light_sensor_pin),value));
                break;
            case R.id.sb_start_postition:
                value = progress+getResources().getInteger(R.integer.min_position);
                tvStartPos.setText(String.format(getString(R.string.start_position),value));
                break;
            case R.id.sb_mid_postition:
                value = progress+getResources().getInteger(R.integer.min_position);
                tvMidPos.setText(String.format(getString(R.string.mid_position),value));
                break;
            case R.id.sb_end_postition:
                value = progress+getResources().getInteger(R.integer.min_position);
                tvEndPos.setText(String.format(getString(R.string.end_position),value));
                break;
            case R.id.sb_long_delay:
                value = progress+getResources().getInteger(R.integer.min_long_delay);
                tvLongDelay.setText(String.format(getString(R.string.long_delay),value));
                break;
            case R.id.sb_short_delay:
                value = progress+getResources().getInteger(R.integer.min_short_delay);
                tvShortDelay.setText(String.format(getString(R.string.short_delay),value));
                break;
            case R.id.sb_servo_pin:
                value = progress+getResources().getInteger(R.integer.min_servo_pin);
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
            addValueToIntent(intent,sbNightThreshold, Constants.keys.NIGHT_THRESHOLD, R.integer.min_night_threshold);
            addValueToIntent(intent,sbLightSensorPin, Constants.keys.LIGHT_SENSOR_PIN, R.integer.min_light_sensor_pin);
            addValueToIntent(intent,sbStartPos, Constants.keys.STARTING_POS, R.integer.min_position);
            addValueToIntent(intent,sbMidPos, Constants.keys.MID_POSITION, R.integer.min_position);
            addValueToIntent(intent,sbEndPos, Constants.keys.END_POSITION, R.integer.min_position);
            addValueToIntent(intent,sbLongDelay, Constants.keys.LONG_DELAY, R.integer.min_long_delay);
            addValueToIntent(intent,sbShortDelay, Constants.keys.SHORT_DELAY, R.integer.min_short_delay);
            addValueToIntent(intent,sbServoPin, Constants.keys.SERVO_PIN, R.integer.min_servo_pin);
            setResult(RESULT_OK,intent);
            finish();
        }
    }

    /**
     *
     * @param seekBar
     * @param intentKey
     * @param minDimenId
     */
    private void setupSeekBarValueFromIntent(final SeekBar seekBar, String intentKey, int minDimenId) {
        seekBar.setProgress(getIntent().getIntExtra(intentKey,0)-getResources().getInteger(minDimenId));
        onProgressChanged(seekBar, seekBar.getProgress(), false);
        seekBar.setOnSeekBarChangeListener(this);
    }

    /**
     *
     * @param intent
     * @param seekBar
     * @param intentKey
     * @param minDimenId
     */
    private void addValueToIntent(final Intent intent, final SeekBar seekBar, String intentKey, int minDimenId) {
        intent.putExtra(intentKey,seekBar.getProgress()+getResources().getInteger(minDimenId));
    }

}
