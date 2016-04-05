package com.toshevski.android.shows.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.toshevski.android.shows.databases.MyData;
import com.toshevski.android.shows.activities.MySeries;
import com.toshevski.android.shows.pojos.Episode;
import com.toshevski.android.shows.R;

public class NotificationPublisher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(this.getClass().getName(), "Objavena e notifikacijata!");

        MyData myData = MyData.getInstance();
        myData.loadData(context.getFilesDir());
        if (myData.size() < 1) return;

        Object[] obj = myData.getRandomUnfinishedEpisode();
        Episode e = (Episode) obj[1];
        String link = (String) obj[0];


        Drawable draw = MyData.getImage(link, context);

        Bitmap image;
        if (draw != null)
            image = ((BitmapDrawable) draw).getBitmap();
        else return;

        Bitmap smallImage = Bitmap.createScaledBitmap(image, 102, 150, false);

        Intent myIntent = new Intent(context, MySeries.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                e.hashCode(), myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new NotificationCompat.Builder(context)
                .setTicker("New episodes: " + e.getTitle())
                .setContentTitle(e.getTitle())
                .setContentText(e.getOverview())
                .setSmallIcon(R.drawable.ic_movie_white_24dp)
                .setContentIntent(pendingIntent)
                .setLargeIcon(smallImage)
                .setAutoCancel(true)
                .build();

        notification.flags = Notification.DEFAULT_LIGHTS |
                Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(e.getTitle().hashCode(), notification);
    }
}