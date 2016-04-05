package com.toshevski.android.shows.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by l3ft on 12/11/15.
 */
public class NotificationPublisherForService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("NotifPubliForSer:", "Startuvan e servisot.");
        context.startService(new Intent(context, NewEpisodesService.class));
    }
}
