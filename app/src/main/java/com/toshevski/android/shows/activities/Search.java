package com.toshevski.android.shows.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.toshevski.android.shows.adapters.SearchAdapter;
import com.toshevski.android.shows.databases.MyData;
import com.toshevski.android.shows.pojos.Keys;
import com.toshevski.android.shows.pojos.Series;
import com.toshevski.android.shows.animations.ProgressBarAnimation;
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
import java.util.ArrayList;

public class Search extends AppCompatActivity {

    private ArrayList<Series> series;
    private ListView listview;
    private SearchAdapter adapter;
    private TextView nothingFoundText;
    private ProgressBar pb;
    private ProgressBarAnimation progressAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search_layout);
        series = new ArrayList<>();

        nothingFoundText = (TextView) findViewById(R.id.nothingFound);
        listview = (ListView) findViewById(R.id.searchList);
        adapter = new SearchAdapter(getApplicationContext(), series);

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Series s = series.get(position);
                s.setSelected(!s.isSelected());
                adapter.notifyDataSetChanged();
            }
        });

        handleIntent(getIntent());

        pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.custom_progressbar));

    }

    private void reDraw() {
        if (adapter.getCount() == 0) {
            listview.setVisibility(View.INVISIBLE);
            nothingFoundText.setVisibility(View.VISIBLE);
        } else {
            listview.setVisibility(View.VISIBLE);
            nothingFoundText.setVisibility(View.GONE);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        return true;
    }

    private void fixSeries(Series s) {
        s.selected = false;

        try {
            FileOutputStream fos = new FileOutputStream(new File(getFilesDir(), s.getImdb() + ".jpg"));
            Bitmap bm = ((BitmapDrawable) s.getImage()).getBitmap();
            bm.compress(Bitmap.CompressFormat.JPEG, this.getResources().getInteger(R.integer.image_quality), fos);
            fos.close();
            s.setStatus(Series.ImageStatus.DOWN);
            s.setImage(null);
        } catch (Exception e) {
            Log.i("MainActivity.fixSeries:", "Greshki");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent(getApplicationContext(), MySeries.class);
        switch (item.getItemId()) {
            case R.id.addSeries:
                for (Series s : series)
                    if (s.isSelected()) {
                        fixSeries(s);
                        MyData.add(s);
                    }
                MyData.isDirty = true;
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //i.putExtra("series", as);
                setResult(Activity.RESULT_OK, i);
                Log.i("Search.addSeries:", "TREBA DA SE VRATI NA POCHETNA! " + MyData.howManySeries());
                startActivity(i);
                break;
            case android.R.id.home:
                //selected.clear();
                setResult(Activity.RESULT_CANCELED, i);
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            this.setTitle(query);
            if (this.getSupportActionBar() != null)
                this.getSupportActionBar().setSubtitle(R.string.search_query);
            Log.i("L3FT.srch.handleIntent:", query);
            String link2 = Keys.searchLinkBeforeName + query + Keys.searchLinkAfterName;
            new GetData().execute(link2.replace(" ", "%20"));
        }
    }

    // AsyncTask

    class GetData extends AsyncTask<String, Void, Void> {

        private String line;
        private int howMany = 0;
        private int howManyPassed = 0;
        private Series newToAdd;

        protected Void doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", Keys.contentType);
                connection.setRequestProperty("trakt-api-version", Keys.apiVersion);
                connection.setRequestProperty("trakt-api-key", Keys.apiKey);
                connection.setDoInput(true);
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                line = rd.readLine();
                JsonToJava(line);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.notifyDataSetChanged();
            pb.setVisibility(View.INVISIBLE);
            reDraw();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            progressAnimation = new ProgressBarAnimation(pb,
                    pb.getProgress(), (int)(100 * (double) howManyPassed / howMany));
            progressAnimation.setDuration(500);

            pb.startAnimation(progressAnimation);

            series.add(newToAdd);
            adapter.notifyDataSetChanged();
        }


        // Inner Methods

        private void JsonToJava(String line) {
            try {
                JSONArray array = new JSONArray(line);
                howMany = array.length();
                for (int i = 0; i < array.length(); ++i) {
                    howManyPassed = i + 1;
                    JSONObject object = array.getJSONObject(i);
                    JSONObject show = object.getJSONObject("show");

                    double rating = object.getDouble("score");

                    String title = show.getString("title");

                    int year = -1;
                    if (show.has("year") && !show.isNull("year"))
                        year = show.getInt("year");
                    if (year == -1)
                        continue;

                    JSONObject poster = show.getJSONObject("images").getJSONObject("poster");

                    String imageName = "x";
                    if (poster.has("thumb") && !poster.isNull("thumb"))
                        imageName = poster.getString("thumb");
                    if (imageName.equals("x"))
                        continue;

                    JSONObject ids = show.getJSONObject("ids");

                    String imdb;
                    if (!ids.isNull("imdb"))
                        imdb = ids.getString("imdb");
                    else continue;
                    if (imdb == null || imdb.length() < 3 || imdb.equals("null"))
                        continue;

                    String overview = "";
                    if (show.has("overview"))
                        overview = show.getString("overview");

                    Drawable drw = null;
                    if (imageName != null)
                        drw = getImage(imageName);

                    newToAdd = new Series(title, year, imdb, overview, rating, "", imageName, Series.ImageStatus.ONLINE);
                    newToAdd.setFilesDir(getFilesDir());

                    if (drw != null)
                        newToAdd.setImage(drw);
                    else newToAdd.setStatus(Series.ImageStatus.NO);

                    Log.i("JSON:", title);

                    publishProgress();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        private Drawable getImage(String line) {
            try {
                Log.i("L3FT.search.getImage:", line);
                URL url = new URL(line);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                InputStream is = connection.getInputStream();
                return Drawable.createFromStream(is, null);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

}
