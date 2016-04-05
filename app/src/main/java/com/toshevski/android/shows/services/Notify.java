package com.toshevski.android.shows.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Random;

public class Notify {

    private Context ctx;

    public Notify(Context ctx, Calendar cal) {
        this.ctx = ctx;

        scheduleNotification(cal);
    }

    private void scheduleNotification(Calendar cal) {

        Log.i("Notify:", "Kreiranje na nova notifikacija: " + cal.getTime());

        Random r = new Random();

        Intent notificationIntent = new Intent(ctx, NotificationPublisher.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, r.nextInt(999), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                1000 * 60 * 60 * 24 * 7, pendingIntent);

    }

    public void cancelAlarm() {
        Intent notificationIntent = new Intent(ctx, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Toast.makeText(ctx, "Deaktiviran Alarm", Toast.LENGTH_SHORT).show();
    }
}
