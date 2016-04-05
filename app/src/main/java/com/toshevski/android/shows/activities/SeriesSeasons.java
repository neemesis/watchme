package com.toshevski.android.shows.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.toshevski.android.shows.adapters.SeasonsAdapter;
import com.toshevski.android.shows.databases.MyData;
import com.toshevski.android.shows.pojos.Episode;
import com.toshevski.android.shows.pojos.Season;
import com.toshevski.android.shows.pojos.Series;
import com.toshevski.android.shows.R;

public class SeriesSeasons extends AppCompatActivity {
    private Series series = null;
    private MyData myData;
    private int posOfSer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seasons_layout_with_expandable);


        myData = MyData.getInstance();
        posOfSer = getIntent().getIntExtra("position", -1);
        series = myData.get(posOfSer);

        Log.i("SeriesSeasons:", "onCreate");

        //if (series == null)
        //    series = (Series) getIntent().getSerializableExtra("series");
        Log.i("SeriesSeasons:", "Naslov: " + series.getTitle());
        this.setTitle(series.getTitle());
        if (this.getSupportActionBar() != null)
            this.getSupportActionBar().setSubtitle(R.string.seasons);

        final SeasonsAdapter sa = new SeasonsAdapter(this, myData.get(posOfSer));

        ExpandableListView elv = (ExpandableListView) findViewById(R.id.seasonsListExpandable);

        ViewGroup header = (ViewGroup) getLayoutInflater().inflate(R.layout.season_list_header, elv, false);

        ImageView headerImage = (ImageView) header.findViewById(R.id.seasonHeaderImage);
        TextView headerTitle = (TextView) header.findViewById(R.id.seasonHeaderTitle);
        TextView headerOverview = (TextView) header.findViewById(R.id.seasonHeaderOverview);

        headerImage.setImageDrawable(series.getImageFromFile());
        headerTitle.setText(series.getTitle());
        headerOverview.setText(series.getOverview());


        elv.addHeaderView(header);
        elv.setAdapter(sa);

        elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                Log.i("SeriesSeasons:", "Na koja pozicija e tatkoto: " + groupPosition);

                Episode e = series.getOneSeason(groupPosition).getOneEpisode(childPosition);
                Season s = series.getOneSeason(groupPosition);

                if (e.isFinished()) {
                    e.setIsFinished(false);
                    s.removeFinishedEpisode();
                } else {
                    e.setIsFinished(true);
                    s.addFinishedEpisode();
                }

                sa.notifyDataSetChanged();

                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        myData.saveData(getFilesDir());
    }
}
