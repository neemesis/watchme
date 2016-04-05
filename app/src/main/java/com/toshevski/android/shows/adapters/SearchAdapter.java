package com.toshevski.android.shows.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.toshevski.android.shows.databases.MyData;
import com.toshevski.android.shows.pojos.Series;
import com.toshevski.android.shows.R;

import java.util.ArrayList;

/**
 * Created by l3ft on 12/6/15.
 */
public class SearchAdapter extends BaseAdapter {

    private ArrayList<Series> items;
    private LayoutInflater inflater;
    private Context ctx;

    public SearchAdapter(Context ctx, ArrayList<Series> items) {
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
    public View getView(int position, View convertView, ViewGroup parent) {

        final Series ser = items.get(position);
        final ViewHolder vh;

        if (convertView == null) {
            vh = new ViewHolder();

            convertView = inflater.inflate(R.layout.search_series_layout, parent, false);

            vh.showImage = (ImageView) convertView.findViewById(R.id.sImage);
            vh.showName = (TextView) convertView.findViewById(R.id.sTitle);
            vh.showHeader = (TextView) convertView.findViewById(R.id.sHeader);
            vh.showCheckBox = (CheckBox) convertView.findViewById(R.id.sCheckBox);

            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        if (ser.getStatus() == Series.ImageStatus.DOWN) {
            Drawable draw = MyData.getImage(ser.getImdb() + ".jpg");
            if (draw != null) {
                vh.showImage.setImageDrawable(draw);
            }
        } else if (ser.getStatus() == Series.ImageStatus.ONLINE) {
            vh.showImage.setImageDrawable(ser.getImage());
        } else {
            vh.showImage.setImageResource(R.drawable.nophoto);
        }

        vh.showName.setText(ser.getTitle());
        vh.showHeader.setText(ser.getOverview());
        vh.showCheckBox.setChecked(ser.isSelected());

        vh.showCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                ser.setSelected(cb.isChecked());
                Log.i("SearchAdp:", "Klikneno na: " + ser.getTitle());
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
    // Inner class

    static private class ViewHolder {
        public ImageView showImage;
        public TextView showName;
        public TextView showHeader;
        public CheckBox showCheckBox;
    }
}
