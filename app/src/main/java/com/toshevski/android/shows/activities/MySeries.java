package com.toshevski.android.shows.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.support.v7.widget.SearchView;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.toshevski.android.shows.adapters.SeriesAdapter;
import com.toshevski.android.shows.databases.DBManager;
import com.toshevski.android.shows.databases.MyData;
import com.toshevski.android.shows.pojos.Episode;
import com.toshevski.android.shows.pojos.Keys;
import com.toshevski.android.shows.pojos.Season;
import com.toshevski.android.shows.pojos.Series;
import com.toshevski.android.shows.R;
import com.toshevski.android.shows.services.NewEpisodesService;
import com.toshevski.android.shows.services.NotifyForService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MySeries extends AppCompatActivity {

    public static MyData myData;
    private boolean check = true;
    private SeriesAdapter myAdapter;
    public static File filesDir;
    public static Context ctx;
    private int imageQuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_series);

        this.setTitle(R.string.my_shows);
        this.ctx = getApplicationContext();
        this.imageQuality = this.getResources().getInteger(R.integer.image_quality);

        if (this.getSupportActionBar() != null)
            this.getSupportActionBar().setSubtitle(R.string.my_shows_sub_title);


        filesDir = this.getFilesDir();
        if (check) {
            new NotifyForService(this);
            myData = MyData.getInstance();
            check = false;
        }
        myData.loadData(getFilesDir());
        myAdapter = new SeriesAdapter(this, myData.getList());
    }

    private void refreshData() {
        for (int i = myData.size() - 1; i >= 0; --i) {
            new RefreshData().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, myData.get(i));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        if (MyData.isDirty) {
            refreshData();
            MyData.isDirty = false;
        }
    }

    private void loadData() {

        //Search.selected.clear();

        TextView tv = (TextView) findViewById(R.id.firstTimeText);
        ListView lv = (ListView) findViewById(R.id.seriesList);

        if (myData.isEmpty()) {
            tv.setText(R.string.first_time_use);
            lv.setVisibility(View.INVISIBLE);
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.INVISIBLE);
            lv.setVisibility(View.VISIBLE);
            //myAdapter = new SeriesAdapter(this, myData.getList());

            lv.setAdapter(myAdapter);
            registerForContextMenu(lv);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.i("MySeries.loadData", "Preminuvanje vo SeriesSeasons");
                    Intent i = new Intent(getApplicationContext(), SeriesSeasons.class);
                    i.putExtra("series", myData.get(position));
                    i.putExtra("position", position);
                    startActivity(i);
                }
            });
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.options);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.context_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.contextDelete) {
            int i = info.position;
            Log.i("onItemClick", "Delete: " + i);
            myData.remove(i, filesDir);
            loadData();
        } else if (item.getItemId() == R.id.moveUp) {
            myData.moveUp(info.position);
        } else if (item.getItemId() == R.id.moveDown) {
            myData.moveDown(info.position);

        } else if (item.getItemId() == R.id.setAllFinished) {
            myData.get(info.position).setAllFinished(true);
        }

        myAdapter.notifyDataSetChanged();

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        myData.saveData(getFilesDir());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.search:
                Log.i("MySeries.oOIS:", "Pretisnen e plus.");
            case R.id.sortRating:
                myData.sortByRating();
                break;
            case R.id.sortUnfinished:
                myData.sortByUnfinished();
                break;
            case R.id.sortYear:
                myData.sortByYear();
                break;
            case R.id.settings:
                Intent i = new Intent(getApplicationContext(), Settings.class);
                startActivity(i);
                break;
            case R.id.refresh:
                refreshData();
                break;
            case R.id.newEpisodeService:
                startService(new Intent(getBaseContext(), NewEpisodesService.class));
                break;
            case R.id.newEpisodeServiceStop:
                stopService(new Intent(getBaseContext(), NewEpisodesService.class));
            case R.id.writeToDB:
                writeToDB();
                break;
            case R.id.readFromDB:
                readFromDB();
                break;
            case R.id.deleteDB:
                new DBManager(this).delDB();
                break;
        }
        myAdapter.notifyDataSetChanged();
        return true;
    }

    public void writeToDB() {
        DBManager dbm = new DBManager(this);
        Log.i("MySer:", "Zapishuvanje vo baza!");
        for (int i = 0; i < myData.howManySeries(); ++i) {
            Series s = myData.get(i);
            for (int j = 0; j < s.getSeasons().size(); ++j) {
                Season se = s.getSeasons().get(j);
                for (int k = 0; k < se.getEpisodes().size(); ++k) {
                    Episode e = se.getOneEpisode(k);
                    dbm.addEpisode(e, se.getTraktID());
                }
                dbm.addSeason(se, s.getImdb());
            }
            dbm.addSeries(s, i);
        }
    }
    public void readFromDB() {
        DBManager dbm = new DBManager(this);
        Log.i("MySer:", "Chitanje od baza!");
        ArrayList<Series> ser = dbm.getAllSeries();
        for (Series a : ser) {
            Log.i("A:", a.toString());
            ArrayList<Season> sea = dbm.getSeasonsForOneSeries(a.getImdb());
            for (Season b : sea) {
                Log.i("B:", b.toString());
                ArrayList<Episode> ep = dbm.getEpisodesForOneSeason(b.getTraktID());
                for (Episode c : ep) {
                    Log.i("C:", c.toString());
                }
            }
        }
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
            Log.i("MySeries.doInBack:", "Uspeshno simnuvanje na: " + originalSer.getTitle());
            if (isChanged) {
                myAdapter.notifyDataSetChanged();
                myData.saveData(getFilesDir());
            }
            originalSer.setRefresh(false);
        }

        // Methods

        private void GetShowInfo(String line, String link) {
            try {
                Log.i("MainAct.GetShowInf:", "Zemanje na osnovni podatoci: " + originalSer.getTitle());
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
                    Log.i("MainAct.GetShowInf:", "Ima li novi epizodi: " + hasNewSeries);
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

                Log.i("MainAct.GetShowInf:", "isCh: " + isChanged + " _ hNS: " + hasNewSeries);
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
                Log.i("MySeries.doInBack:", "Proba za simnuvanje: " + originalSer.getTitle());
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
                Log.i("MySeries.doInBack:", "Neuspeshno simnuvanje: " + originalSer.getTitle());
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
                    newSeason.setFilesDir(filesDir);

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
                Log.i("MainActy.DownImage:", "StartDownloadOfSeasonImage");
                File f = new File(filesDir, name);
                if (f.exists())
                    return;

                FileOutputStream fos = new FileOutputStream(f);

                URL url = new URL(link);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                InputStream is = connection.getInputStream();
                Bitmap bm = ((BitmapDrawable) Drawable.createFromStream(is, null)).getBitmap();
                bm.compress(Bitmap.CompressFormat.JPEG, imageQuality, fos);
                fos.close();

            } catch (Exception e) {
                Log.i("MainActy.DownImage:", "Greshki vo simnuvanje na slika za sezona.");
                e.printStackTrace();
            }
        }
    }

}
