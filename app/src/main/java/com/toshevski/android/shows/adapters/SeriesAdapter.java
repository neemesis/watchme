package com.toshevski.android.shows.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.toshevski.android.shows.databases.MyData;
import com.toshevski.android.shows.pojos.Series;
import com.toshevski.android.shows.R;

import java.util.ArrayList;

public class SeriesAdapter extends BaseAdapter {

    private ArrayList<Series> items;
    private LayoutInflater inflater;
    private Context ctx;

    public SeriesAdapter(Context ctx, ArrayList<Series> items) {
        this.items = items;
        this.ctx = ctx;
        this.inflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Series getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        final Series ser = items.get(position);
        final ViewHolder vh;

        if (convertView == null) {
            vh = new ViewHolder();

            //convertView = inflater.inflate(R.layout.series_layout, parent, false);
            convertView = inflater.inflate(R.layout.series_layout_v2, parent, false);

            vh.showImage = (ImageView) convertView.findViewById(R.id.sImage);
            vh.showName = (TextView) convertView.findViewById(R.id.sTitle);
            vh.showHeader = (TextView) convertView.findViewById(R.id.sHeader);
            vh.showSubHeader = (TextView) convertView.findViewById(R.id.sSubHeader);
            vh.showRating = (TextView) convertView.findViewById(R.id.sFooter);
            vh.showStatus = (TextView) convertView.findViewById(R.id.sStatus);
            vh.showProgress = (ProgressBar) convertView.findViewById(R.id.sUnfinishedProgress);

            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        vh.showImage.setImageDrawable(MyData.getImage(ser.getImageLink()));
        vh.showName.setText(ser.getTitle());
        vh.showProgress.getIndeterminateDrawable().setColorFilter(Color.WHITE,
                PorterDuff.Mode.DST_IN);

        if (ser.isRefresh()) {
            vh.showStatus.setVisibility(View.INVISIBLE);
            vh.showSubHeader.setVisibility(View.INVISIBLE);
            vh.showRating.setVisibility(View.INVISIBLE);
            vh.showProgress.setIndeterminate(true);
            vh.showHeader.setText(ctx.getString(R.string.refreshing));
        } else {

            vh.showProgress.setIndeterminate(false);
            vh.showStatus.setVisibility(View.VISIBLE);
            vh.showRating.setVisibility(View.VISIBLE);
            vh.showSubHeader.setVisibility(View.VISIBLE);

            int unf = ser.getUnfinished();

            vh.showHeader.setText(ser.getOverview());
            vh.showSubHeader.setText(String.format("%s %d",
                    ctx.getString(R.string.total_episodes), ser.getTotalEpisodes()));
            vh.showRating.setText(String.format("%s %.2f",
                    ctx.getString(R.string.rating), ser.getRating()));

            int progress = (int) (100f / ser.getTotalEpisodes() *
                    (ser.getTotalEpisodes() - unf));

            vh.showProgress.setProgress(progress);

            if (unf == 0) {
                vh.showStatus.setText(ctx.getString(R.string.done_series));
            } else if (unf == 1) {
                vh.showStatus.setText(ctx.getString(R.string.one_more_episode));
            } else {
                vh.showStatus.setText(String.format("%d %s", unf, ctx.getString(R.string.left_episodes)));
            }
        }

        return convertView;
    }

    // Methods
/*
    private Drawable getImage(String name) {
        Log.i("SA_v2:getImage", name);
        try {
            FileInputStream fis = new FileInputStream(new File(ctx.getFilesDir(), name));
            Bitmap b = BitmapFactory.decodeStream(fis);
            return new BitmapDrawable(Resources.getSystem(), b);
        } catch (Exception e) {
            Log.i("SA_v2.getImage:", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
*/
    // Inner class

    static private class ViewHolder {
        public ImageView showImage;
        public TextView showName;
        public TextView showHeader;
        public TextView showSubHeader;
        public TextView showRating;
        public ProgressBar showProgress;
        public TextView showStatus;
    }

}
