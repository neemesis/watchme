package com.toshevski.android.shows.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.toshevski.android.shows.databases.MyData;
import com.toshevski.android.shows.pojos.Episode;
import com.toshevski.android.shows.pojos.Season;
import com.toshevski.android.shows.pojos.Series;
import com.toshevski.android.shows.animations.ProgressBarAnimation;
import com.toshevski.android.shows.R;

public class SeasonsAdapter extends BaseExpandableListAdapter {

    private Series originalSer;
    private Context ctx;
    private LayoutInflater inflater;
    private ProgressBar progress;

    public SeasonsAdapter(Context ctx, Series originalSer) {
        this.ctx = ctx;
        this.originalSer = originalSer;
        this.inflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getGroupCount() {
        return originalSer.getNumberOfSeasons();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return originalSer.getOneSeason(groupPosition).getEpisodesCount();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        final ViewHolderForGroup vh;
        final Season s = originalSer.getOneSeason(groupPosition);

        if (convertView == null) {
            vh = new ViewHolderForGroup();

            convertView = inflater.inflate(R.layout.show_linear_layout_v2, parent, false);

            vh.showImage = (ImageView) convertView.findViewById(R.id.showImage);
            vh.showName = (TextView) convertView.findViewById(R.id.showName);
            vh.showHeader = (TextView) convertView.findViewById(R.id.showHeader);
            vh.showSubHeader = (TextView) convertView.findViewById(R.id.showSubHeader);
            vh.showRating = (TextView) convertView.findViewById(R.id.showRating);
            //vh.showUnfinishedEpisodes = (TextView) convertView.findViewById(R.id.unfinishedEpisodes);
            vh.unfinishedProgres = (ProgressBar) convertView.findViewById(R.id.unfinishedProgress);
            vh.unfinishedProgres.setProgress((int) ((100 / (double) s.getEpisodesCount()) *
                    (s.getEpisodesCount() - s.getUnfinished())));
            //vh.showUnfinishedImage = (ImageView) convertView.findViewById(R.id.unfinishedImage);
            vh.isFinished = (CheckBox) convertView.findViewById(R.id.seasonCheckBox);

            convertView.setTag(vh);
        } else {
            vh = (ViewHolderForGroup) convertView.getTag();
        }

        Drawable draw = MyData.getImage(s.getImageLink());
        if (draw != null)
            vh.showImage.setImageDrawable(draw);
        else vh.showImage.setImageResource(R.drawable.nophoto);
        vh.showHeader.setText(String.format("%s %d", ctx.getString(R.string.total_episodes),
                s.getEpisodesCount()));

        vh.showName.setText(s.getTitle());
        vh.showRating.setText(String.format("%s %s", ctx.getString(R.string.rating), s.getRating()));
        vh.showSubHeader.setText(String.format("%s %d",
                ctx.getString(R.string.finished_episodes), s.getEpisodesCount() - s.getUnfinished()));
        //vh.showUnfinishedImage.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);

        int unf = s.getUnfinished();

        if (unf == 0) {
            vh.isFinished.setChecked(true);
        } else vh.isFinished.setChecked(false);

        int prog = (int) ((100 / (double) s.getEpisodesCount()) *
                (s.getEpisodesCount() - unf));
        int unfProg = vh.unfinishedProgres.getProgress();

        if (prog != s.getLastTimeProgress()) {
            ProgressBarAnimation pba = new ProgressBarAnimation(vh.unfinishedProgres,
                    unfProg, prog);

            pba.setDuration(800);
            s.setLastTimeProgress(prog);

            vh.unfinishedProgres.startAnimation(pba);
        } else vh.unfinishedProgres.setProgress(prog);

        //vh.unfinishedProgres.setProgress((int) ((100 /(double) s.getEpisodesCount()) * (s.getEpisodesCount() - unf)));

        /*if (s.getUnfinished() == 0)
            convertView.setBackgroundColor(Color.parseColor("#E1DBA0"));
        else
            convertView.setBackgroundColor(Color.parseColor("#00000000"));*/

        vh.isFinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                s.setAllFinished(cb.isChecked());
                SeasonsAdapter.this.notifyDataSetChanged();
            }
        });

        return convertView;
    }
/*
    private Drawable getImage(String name) {
        try {
            FileInputStream fis = new FileInputStream(new File(ctx.getFilesDir(), name));
            Bitmap b = BitmapFactory.decodeStream(fis);
            return new BitmapDrawable(Resources.getSystem(), b);
        } catch (Exception e) {
            Log.i("L3FT.getImage:", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
*/
    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final ViewHolderForChild vh;
        final Episode e = originalSer.getOneSeason(groupPosition).getOneEpisode(childPosition);


        if (convertView == null) {
            vh = new ViewHolderForChild();

            convertView = inflater.inflate(R.layout.episode_row_layout_v2, parent, false);

            vh.episodeName = (TextView) convertView.findViewById(R.id.episodeName);
            vh.episodeOverview = (TextView) convertView.findViewById(R.id.episodeOverview);
            vh.isFinished = (CheckBox) convertView.findViewById(R.id.isFinished);

            convertView.setTag(vh);
        } else {
            vh = (ViewHolderForChild) convertView.getTag();
        }

        vh.episodeOverview.setText(e.getOverview());
        vh.episodeName.setText(String.format("%d. %s", e.getEpisodeNo(), e.getTitle()));

        if (e.isFinished()) {
            vh.isFinished.setChecked(true);
            vh.episodeOverview.setVisibility(View.GONE);
            //convertView.setBackgroundColor(Color.parseColor("#E1DBA0"));
        } else {
            vh.isFinished.setChecked(false);
            vh.episodeOverview.setVisibility(View.VISIBLE);
            //convertView.setBackgroundColor(Color.parseColor("#00000000"));
        }

        vh.isFinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                e.setIsFinished(cb.isChecked());
                SeasonsAdapter.this.notifyDataSetChanged();
                Log.i("SeasonsAdap.cbOnClick:", "Clicked on: " + e.getTitle());
            }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    // Inner class

    static private class ViewHolderForGroup {
        public ImageView showImage;
        public TextView showName;
        public TextView showHeader;
        public TextView showSubHeader;
        public TextView showRating;
        //public TextView showUnfinishedEpisodes;
        //public ImageView showUnfinishedImage;
        public ProgressBar unfinishedProgres;
        public CheckBox isFinished;
    }

    static private class ViewHolderForChild {
        public TextView episodeName;
        public TextView episodeOverview;
        public CheckBox isFinished;
    }
}
