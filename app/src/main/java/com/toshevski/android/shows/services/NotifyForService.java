package com.toshevski.android.shows.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by l3ft on 12/11/15.
 */
public class NotifyForService {
    private Context ctx;

    public NotifyForService(Context ctx) {
        Log.i(this.getClass().getName(), "Kreiranje na nova notifikacija.");
        this.ctx = ctx;

        scheduleNotification();
    }

    private void scheduleNotification() {

        Random r = new Random();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 20);
        cal.set(Calendar.MINUTE, 0);

        if (cal.getTimeInMillis() < System.currentTimeMillis()) {
            cal.set(Calendar.WEEK_OF_MONTH, cal.get(Calendar.WEEK_OF_MONTH) + 1);
        }

        Intent notificationIntent = new Intent(ctx, NotificationPublisherForService.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, r.nextInt(999), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(),
                1000 * 60 * 60 * 24, pendingIntent);
        Log.i(this.getClass().getName(), "Postaven alarm za service: " + cal.getTime());

    }
}
