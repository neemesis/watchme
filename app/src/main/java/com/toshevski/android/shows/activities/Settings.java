package com.toshevski.android.shows.activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.widget.Toast;

import com.toshevski.android.shows.databases.MyData;
import com.toshevski.android.shows.R;
import com.toshevski.android.shows.services.Notify;

import java.util.Calendar;

public class Settings extends AppCompatActivity {

    private MyData myData;
    private TimePicker timePicker;
    private Notify[] notifyAlarm;

    private CheckBox mon;
    private CheckBox tue;
    private CheckBox wed;
    private CheckBox thu;
    private CheckBox fri;
    private CheckBox sat;
    private CheckBox sun;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        mon = (CheckBox) findViewById(R.id.monday);
        tue = (CheckBox) findViewById(R.id.tuesday);
        wed = (CheckBox) findViewById(R.id.wednesday);
        thu = (CheckBox) findViewById(R.id.thursday);
        fri = (CheckBox) findViewById(R.id.friday);
        sat = (CheckBox) findViewById(R.id.saturday);
        sun = (CheckBox) findViewById(R.id.sunday);

        SharedPreferences sp = this.getSharedPreferences("com.toshevski.android.shows.settings",
                Context.MODE_PRIVATE);

        mon.setChecked(sp.getBoolean("mon", false));
        tue.setChecked(sp.getBoolean("tue", false));
        wed.setChecked(sp.getBoolean("wed", false));
        thu.setChecked(sp.getBoolean("thu", false));
        fri.setChecked(sp.getBoolean("fri", false));
        sat.setChecked(sp.getBoolean("sat", false));
        sun.setChecked(sp.getBoolean("sun", false));

        notifyAlarm = new Notify[7];
        myData = MyData.getInstance();
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion > Build.VERSION_CODES.LOLLIPOP_MR1){
            timePicker.setHour(sp.getInt("hour", 0));
            timePicker.setHour(sp.getInt("min", 0));
        } else {
            timePicker.setCurrentHour(sp.getInt("hour", 0));
            timePicker.setCurrentMinute(sp.getInt("min", 0));
        }
    }


    public void activateAlarm(View v) {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        int hour = 20;
        int min = 0;

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion > Build.VERSION_CODES.LOLLIPOP_MR1){
            hour = timePicker.getHour();
            min = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            min = timePicker.getCurrentMinute();
        }

        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);

        Toast.makeText(this.getApplicationContext(), "Postaven Alarm: " + cal.getTime(), Toast.LENGTH_SHORT).show();

        int currentWeek = cal.get(Calendar.WEEK_OF_MONTH);

        if (!myData.isEmpty()) {

            if (mon.isChecked()) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                cal.set(Calendar.WEEK_OF_MONTH, currentWeek);
                if (cal.getTimeInMillis() < System.currentTimeMillis()) {
                    cal.set(Calendar.WEEK_OF_MONTH, currentWeek + 1);
                }
                notifyAlarm[0] = new Notify(this, cal);
            } else {
                cancelAlarm(0);
            }

            if (tue.isChecked()) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                cal.set(Calendar.WEEK_OF_MONTH, currentWeek);
                if (cal.getTimeInMillis() < System.currentTimeMillis())
                    cal.set(Calendar.WEEK_OF_MONTH, currentWeek + 1);
                notifyAlarm[1] = new Notify(this, cal);
            } else {
                cancelAlarm(1);
            }

            if (wed.isChecked()) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                cal.set(Calendar.WEEK_OF_MONTH, currentWeek);
                if (cal.getTimeInMillis() < System.currentTimeMillis()) {
                    cal.set(Calendar.WEEK_OF_MONTH, currentWeek + 1);
                }
                notifyAlarm[2] = new Notify(this, cal);
            } else {
                cancelAlarm(2);
            }
            if (thu.isChecked()) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                cal.set(Calendar.WEEK_OF_MONTH, currentWeek);
                if (cal.getTimeInMillis() < System.currentTimeMillis()) {
                    cal.set(Calendar.WEEK_OF_MONTH, currentWeek + 1);
                }
                notifyAlarm[3] = new Notify(this, cal);
            } else {
                cancelAlarm(3);
            }
            if (fri.isChecked()) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                cal.set(Calendar.WEEK_OF_MONTH, currentWeek);
                if (cal.getTimeInMillis() < System.currentTimeMillis()) {
                    cal.set(Calendar.WEEK_OF_MONTH, currentWeek + 1);
                }
                notifyAlarm[4] = new Notify(this, cal);
            } else {
                cancelAlarm(4);
            }
            if (sat.isChecked()) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                cal.set(Calendar.WEEK_OF_MONTH, currentWeek);
                if (cal.getTimeInMillis() < System.currentTimeMillis()) {
                    cal.set(Calendar.WEEK_OF_MONTH, currentWeek + 1);
                }
                notifyAlarm[5] = new Notify(this, cal);
            } else {
                cancelAlarm(5);
            }
            if (sun.isChecked()) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                cal.set(Calendar.WEEK_OF_MONTH, currentWeek);
                if (cal.getTimeInMillis() < System.currentTimeMillis()) {
                    cal.set(Calendar.WEEK_OF_MONTH, currentWeek + 1);
                }
                notifyAlarm[6] = new Notify(this, cal);
            } else {
                cancelAlarm(6);
            }
        }

        SharedPreferences sp = this.getSharedPreferences("com.toshevski.android.shows.settings",
                Context.MODE_PRIVATE);

        sp.edit().putBoolean("mon", mon.isChecked()).apply();
        sp.edit().putBoolean("tue", tue.isChecked()).apply();
        sp.edit().putBoolean("wed", wed.isChecked()).apply();
        sp.edit().putBoolean("thu", thu.isChecked()).apply();
        sp.edit().putBoolean("fri", fri.isChecked()).apply();
        sp.edit().putBoolean("sat", sat.isChecked()).apply();
        sp.edit().putBoolean("sun", sun.isChecked()).apply();

        sp.edit().putInt("hour", hour).apply();
        sp.edit().putInt("min", min).apply();
    }

    public void cancelAlarm(int i) {
        if (notifyAlarm[i] != null) {
            notifyAlarm[i].cancelAlarm();
            notifyAlarm[i] = null;
        }
    }

}