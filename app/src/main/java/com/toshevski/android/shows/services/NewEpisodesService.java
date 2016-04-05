package com.toshevski.android.shows.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.toshevski.android.shows.pojos.Keys;
import com.toshevski.android.shows.databases.MyData;
import com.toshevski.android.shows.activities.MySeries;
import com.toshevski.android.shows.pojos.Episode;
import com.toshevski.android.shows.pojos.Season;
import com.toshevski.android.shows.pojos.Series;
import com.toshevski.android.shows.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class NewEpisodesService extends Service {

    private MyData myData;

    @Override
    public void onCreate() {
        this.myData = MyData.getInstance();
        this.myData.loadData(getFilesDir());
        Log.i(this.getClass().getName(), "LoadData: " + myData.size());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        refreshData();
        Log.i(this.getClass().getName(), "onStartCommand + refreshData");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(this.getClass().getName(), "onDestroy");
    }

    private void refreshData() {
        if (myData.size() > 0)
            showNotif(myData.get(0));
        for (int i = myData.size() - 1; i >= 0; --i) {
            new RefreshData().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, myData.get(i));
        }
    }

    public void publishNotification(Series s) {
        Drawable draw = MyData.getImage(s.getImageLink(), this);

        Bitmap image;
        if (draw != null)
            image = ((BitmapDrawable) draw).getBitmap();
        else return;

        Bitmap smallImage = Bitmap.createScaledBitmap(image, 102, 150, false);

        Intent myIntent = new Intent(this, MySeries.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                s.hashCode(), myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setTicker("New episodes: " + s.getTitle())
                .setContentTitle(s.getTitle())
                .setContentText("!" + s.getOverview())
                .setSmallIcon(R.drawable.ic_movie_white_24dp)
                .setContentIntent(pendingIntent)
                .setLargeIcon(smallImage)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(s.hashCode(), notification);

    }

    private void showNotif(Series s) {

        Drawable draw = MyData.getImage(s.getImageLink(), this);

        Bitmap image;
        if (draw != null)
            image = ((BitmapDrawable) draw).getBitmap();
        else return;

        Bitmap smallImage = Bitmap.createScaledBitmap(image, 102, 150, false);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification2 = new NotificationCompat.Builder(this)
                .setTicker("Service start")
                .setContentTitle("Service start")
                .setContentText("on: " + new SimpleDateFormat("yyyy:MM:dd - HH:mm:ss").format(Calendar.getInstance().getTime()))
                .setSmallIcon(R.drawable.ic_movie_white_24dp)
                .setLargeIcon(smallImage)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(666, notification2);
    }

    class RefreshData extends AsyncTask<Series, Void, Void> {

        public Series originalSer;
        private boolean isChanged = false;
        private boolean hasNewSeries = false;

        @Override
        protected Void doInBackground(Series... params) {
            originalSer = params[0];
            try {
                Log.i("MySeries.doInBack:", "Proba za simnuvanje: " + originalSer.getTitle());
                URL url = new URL(Keys.refreshShowBeforeName + params[0].getImdb() + Keys.refreshShowAfterName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", Keys.contentType);
                connection.setRequestProperty("trakt-api-version", Keys.apiVersion);
                connection.setRequestProperty("trakt-api-key", Keys.apiKey);
                connection.setDoInput(true);
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                String line = rd.readLine();
                GetShowInfo(line, params[0].getImdb());

            } catch (Exception e) {
                Log.i("MySeries.doInBack:", "Neuspeshno simnuvanje: " + originalSer.getTitle());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(this.getClass().getName(), "Uspeshno simnuvanje na: " + originalSer.getTitle());
            if (hasNewSeries) {
                //myAdapter.notifyDataSetChanged();
                myData.saveData(getFilesDir());
                publishNotification(originalSer); // ne se znae shto e :D
            }
            originalSer.setRefresh(false);
        }

        // Methods

        private void GetShowInfo(String line, String link) {
            try {
                Log.i(this.getClass().getName(), "Zemanje na osnovni podatoci: " + originalSer.getTitle());
                JSONObject series = new JSONObject(line);

                double rating = -1;
                if (!series.isNull("rating")) {
                    rating = series.getDouble("rating");
                    if (rating != originalSer.getRating())
                        isChanged = true;
                }

                int totalEpisodes = -1;
                if (!series.isNull("aired_episodes")) {
                    totalEpisodes = series.getInt("aired_episodes");
                    if (totalEpisodes != originalSer.getTotalEpisodes() - originalSer.getTotalSpecialEpisodes()) {
                        isChanged = hasNewSeries = true;
                        originalSer.setTotalEpisodes(totalEpisodes);
                    }
                    Log.i(this.getClass().getName(), "Ima li novi epizodi: " + hasNewSeries);
                }

                String genre = "x";
                if (!series.isNull("genres")) {
                    if (series.getJSONArray("genres").length() > 0) {
                        genre = series.getJSONArray("genres").getString(0);
                        if (!genre.equals(originalSer.getGenre())) {
                            isChanged = true;
                        }
                    }
                }

                String overview = "x";
                if (!series.isNull("overview")) {
                    overview = series.getString("overview");
                    if (overview.equals(originalSer.getOverview())) {
                        isChanged = true;
                    }
                }

                Log.i(this.getClass().getName(), "isCh: " + isChanged + " _ hNS: " + hasNewSeries);
                if (isChanged) {
                    originalSer.setRating(rating);
                    originalSer.setGenre(genre);
                    originalSer.setTotalEpisodes(totalEpisodes);
                    originalSer.setOverview(overview);
                }

                if (hasNewSeries)
                    GetSeason(link);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void GetSeason(String link) {
            try {
                Log.i(this.getClass().getName(), "Proba za simnuvanje: " + originalSer.getTitle());
                URL url = new URL(Keys.refreshShowEpisodesBeforeName + link + Keys.refreshShowEpisodesAfterName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", Keys.contentType);
                connection.setRequestProperty("trakt-api-version", Keys.apiVersion);
                connection.setRequestProperty("trakt-api-key", Keys.apiKey);
                connection.setDoInput(true);
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                String line = rd.readLine();

                FillSeasons(line);

            } catch (Exception e) {
                Log.i(this.getClass().getName(), "Neuspeshno simnuvanje: " + originalSer.getTitle());
                e.printStackTrace();
            }
        }

        private void FillSeasons(String line) {
            try {

                JSONArray array = new JSONArray(line);

                for (int i = 0; i < array.length(); ++i) {
                    JSONObject season = array.getJSONObject(i);

                    int seasonNo = -1;
                    if (!season.isNull("number"))
                        seasonNo = season.getInt("number");
                    if (seasonNo == -1)
                        continue;

                    int traktID = -1;
                    if (!season.isNull("ids"))
                        traktID = season.getJSONObject("ids").getInt("trakt");

                    double rating = 0;
                    if (!season.isNull("rating"))
                        rating = season.getDouble("rating");

                    int episodesCount = 0;
                    if (!season.isNull("episode_count"))
                        episodesCount = season.getInt("episode_count");
                    if (episodesCount == 0)
                        continue;

                    int airedEpisodes = 0;
                    if (!season.isNull("aired_episodes"))
                        airedEpisodes = season.getInt("aired_episodes");
                    if (airedEpisodes == 0)
                        continue;

                    String overview = "x";
                    if (!season.isNull("overview"))
                        overview = season.getString("overview");

                    String imageLink = "x";
                    if (!season.isNull("images"))
                        if (!season.getJSONObject("images").isNull("poster"))
                            if (!season.getJSONObject("images").getJSONObject("poster").isNull("thumb"))
                                imageLink = season.getJSONObject("images").getJSONObject("poster").getString("thumb");

                    ArrayList<Episode> eps = new ArrayList<>();
                    if (!season.isNull("episodes")) {
                        JSONArray listaEps = season.getJSONArray("episodes");

                        for (int j = 0; j < listaEps.length(); ++j) {
                            JSONObject episode = listaEps.getJSONObject(j);

                            int episodeNo = -1;
                            if (!episode.isNull("number"))
                                episodeNo = episode.getInt("number");
                            if (episodeNo == -1)
                                continue;

                            String title = "x";
                            if (!episode.isNull("title"))
                                title = episode.getString("title");
                            if (title.equals("x"))
                                continue;

                            String imdb = "x";
                            if (!episode.isNull("ids"))
                                imdb = episode.getJSONObject("ids").getString("imdb");
                            if (imdb.equals("x"))
                                continue;

                            String overviewEps = "";
                            if (!episode.isNull("overview"))
                                overviewEps = episode.getString("overview");

                            double ratingEps = -1;
                            if (!episode.isNull("rating"))
                                ratingEps = episode.getDouble("rating");

                            Episode newEps = new Episode(seasonNo, episodeNo, title, imdb, overviewEps, ratingEps);
                            eps.add(newEps);
                        }
                    }

                    Season newSeason = originalSer.addSeason(new Season(seasonNo, traktID,
                            rating, episodesCount, airedEpisodes, overview));
                    newSeason.addEpisodes(eps);
                    newSeason.setFilesDir(getFilesDir());

                    if (seasonNo == 0)
                        originalSer.setTotalSpecialEpisodes(airedEpisodes);

                    if (!imageLink.equals("x")) {
                        Log.i("Download Season Image: ", newSeason.getImageLink());
                        DownloadImage(newSeason.getImageLink(), imageLink);
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void DownloadImage(String name, String link) {
            try {
                Log.i(this.getClass().getName(), "StartDownloadOfSeasonImage");
                File f = new File(getFilesDir(), name);
                if (f.exists())
                    return;

                FileOutputStream fos = new FileOutputStream(f);

                URL url = new URL(link);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                InputStream is = connection.getInputStream();
                Bitmap bm = ((BitmapDrawable) Drawable.createFromStream(is, null)).getBitmap();
                bm.compress(Bitmap.CompressFormat.JPEG,
                        getResources().getInteger(R.integer.image_quality), fos);
                fos.close();

            } catch (Exception e) {
                Log.i(this.getClass().getName(), "Greshki vo simnuvanje na slika za sezona.");
                e.printStackTrace();
            }
        }
    }
}
