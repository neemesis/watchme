package com.toshevski.android.shows.databases;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.toshevski.android.shows.activities.MySeries;
import com.toshevski.android.shows.pojos.Episode;
import com.toshevski.android.shows.pojos.Season;
import com.toshevski.android.shows.pojos.Series;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class MyData implements Serializable {

    private static ArrayList<Series> mySeries;
    public static ArrayList<Series> newSeries;
    public static boolean isDirty = false;

    private static MyData data = new MyData();

    public static MyData getInstance() {
        return data;
    }

    private MyData() {
        newSeries = new ArrayList<>();
        //loadData();
    }

    public static int howManySeries() {
        return mySeries.size();
    }

    public static void addNewSeries(Series s) {
        newSeries.add(s);
    }

    public void sortByRating() {
        Collections.sort(mySeries, Comparators.RATING);
    }

    public void sortByUnfinished() {
        Collections.sort(mySeries, Comparators.UNFINISHED);
    }

    public void sortByYear() {
        Collections.sort(mySeries, Comparators.YEAR);
    }

    public void moveUp(int pos) {
        if (pos == 0) return;

        Series up = mySeries.get(pos - 1);
        mySeries.set(pos - 1, mySeries.get(pos));
        mySeries.set(pos, up);
    }

    public void moveDown(int pos) {
        if (pos + 1 == mySeries.size()) return;

        Series up = mySeries.get(pos + 1);
        mySeries.set(pos + 1, mySeries.get(pos));
        mySeries.set(pos, up);
    }

    public ArrayList<Series> getList() {
        return mySeries;
    }

    public int size() {
        return mySeries.size();
    }

    public static void add(Series s) {
        if (!mySeries.contains(s)) {
            mySeries.add(s);
        }
    }

    public void remove(Series s) {
        mySeries.remove(s);
    }

    public void remove(int i, File filesDir) {
        mySeries.get(i).removePictures(filesDir);
        mySeries.remove(i);
    }

    public boolean isEmpty() {
        return mySeries.size() == 0;
    }

    public static boolean contains(Series s) {
        return mySeries.contains(s);
    }

    public Series get(int i) {
        return mySeries.get(i);
    }

    public Object[] getRandomUnfinishedEpisode() {

        Object[] obj = new Object[2];

        Random random = new Random();

        int i = mySeries.size();

        Series s = null;
        while (i >= -1) {
            s = mySeries.get(random.nextInt(mySeries.size()));
            if (s.getUnfinished() > 0) break;
            --i;
        }
        if (s != null) {
            Season seas = null;
            i = s.getNumberOfSeasons();
            while (i >= 0) {
                seas = s.getOneSeason(random.nextInt(s.getNumberOfSeasons()));
                if (seas.getUnfinished() > 0) {
                    obj[0] = seas.getImageLink();
                    break;
                }
                --i;
            }
            if (seas != null) {
                i = seas.getEpisodesCount();
                Episode e = null;
                while (i >= 0) {
                    e = seas.getOneEpisode(random.nextInt(seas.getEpisodesCount()));
                    if (!e.isFinished()) break;
                    --i;
                }
                if (e != null) {
                    obj[1] = e;
                    return obj;
                }
            }
        }
        return null;
    }

    public void loadData(File filesDir) {
        if (mySeries != null) return;
        try {
            FileInputStream fis = new FileInputStream(new File(filesDir, "data.bin"));
            ObjectInputStream ois = new ObjectInputStream(fis);
            mySeries = (ArrayList<Series>) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            mySeries = new ArrayList<>();
        }
    }

    public void saveData(File filesDir) {
        if (mySeries == null) return;
        try {
            FileOutputStream fos = new FileOutputStream(new File(filesDir, "data.bin"));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mySeries);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Drawable getImage(String name, Context ctx) {
        try {
            FileInputStream fis = new FileInputStream(new File(ctx.getFilesDir(), name));
            Bitmap b = BitmapFactory.decodeStream(fis);
            return new BitmapDrawable(Resources.getSystem(), b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Drawable getImage(String name) {
        try {
            FileInputStream fis = new FileInputStream(new File(MySeries.filesDir, name));
            Bitmap b = BitmapFactory.decodeStream(fis);
            return new BitmapDrawable(Resources.getSystem(), b);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class Comparators {
        public static Comparator<Series> RATING = new Comparator<Series>() {
            @Override
            public int compare(Series lhs, Series rhs) {
                if (lhs.getRating() > rhs.getRating())
                    return -1;
                else if (lhs.getRating() == rhs.getRating())
                    return 0;
                else return 1;
            }
        };

        public static Comparator<Series> YEAR = new Comparator<Series>() {
            @Override
            public int compare(Series lhs, Series rhs) {
                if (lhs.getYear() > rhs.getYear())
                    return -1;
                else if (lhs.getYear() == rhs.getYear())
                    return 0;
                else return 1;
            }
        };

        public static Comparator<Series> UNFINISHED = new Comparator<Series>() {
            @Override
            public int compare(Series lhs, Series rhs) {
                int lhsU = lhs.getUnfinished();
                int rhsU = rhs.getUnfinished();
                if (lhsU > rhsU)
                    return -1;
                else if (lhsU == rhsU)
                    return 0;
                else return 1;
            }
        };
    }
}
