package com.toshevski.android.shows.pojos;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.toshevski.android.shows.activities.MySeries;
import com.toshevski.android.shows.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Season implements Serializable {
    private int seasonNo;
    private int traktID;
    private double rating;
    private int episodesCount;
    private int finishedEpisodes;
    private int airedEpisodes;
    private String overview;
    private ArrayList<Episode> episodes;
    private File filesDir;
    private int lastTimeProgress;

    public boolean hasDownloadedPicture;

    public Season(int seasonNo, int traktID, double rating, int episodesCount, int airedEpisodes, String overview) {
        this.seasonNo = seasonNo;
        this.traktID = traktID;
        this.rating = rating;
        this.episodesCount = episodesCount;
        this.airedEpisodes = airedEpisodes;
        this.overview = overview;
        this.episodes = new ArrayList<>();
        this.hasDownloadedPicture = false;
        this.finishedEpisodes = 0;
        this.lastTimeProgress = 0;
    }

    @Override
    public String toString() {
        return "\t" + seasonNo + ". " + getTitle();
    }

    public void addAllEpisodes(ArrayList<Episode> e) {
        this.episodes = e;
    }

    public String getTitle() {
        if (seasonNo == 0) return MySeries.ctx.getResources().getString(R.string.special_season);
        return MySeries.ctx.getResources().getString(R.string.season_name) + " " + seasonNo;
    }

    public int getLastTimeProgress() {
        return lastTimeProgress;
    }

    public void setLastTimeProgress(int lastTimeProgress) {
        this.lastTimeProgress = lastTimeProgress;
    }

    public void addEpisodes(ArrayList<Episode> eps) {

        for (Episode epI : episodes) {
            for (Episode epJ : eps) {
                if (epI.getImdb().equals(epJ.getImdb())) {
                    epI.setOverview(epJ.getOverview());
                    epI.setRating(epJ.getRating());
                    eps.remove(epJ);
                }
            }
        }

        for (Episode e : eps) {
            episodes.add(e);
        }
/*
        for (int i = 0; i < episodes.size(); ++i) {
            Episode epI = episodes.get(i);
            for (int j = 0; j < eps.size(); ++j) {
                Episode epJ = eps.get(i);
                if (epI.getImdb().equals(epJ.getImdb())) {
                    epI.setOverview(epJ.getOverview());
                    epI.setRating(epJ.getRating());
                }
            }
        }

        for (int i = episodes.size(); i < eps.size(); ++i) {
            episodes.add(eps.get(i));
        }
*/
    }

    public Episode getOneEpisode(int position) {
        return episodes.get(position);
    }

    public void setAllFinished(Boolean how) {
        for (Episode e : episodes) {
            e.setIsFinished(how);
        }
        finishedEpisodes = episodes.size();
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setEpisodesCount(int episodesCount) {
        this.episodesCount = episodesCount;
    }

    public void setAiredEpisodes(int airedEpisodes) {
        this.airedEpisodes = airedEpisodes;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setEpisodes(ArrayList<Episode> e) {
        episodes = e;
    }

    public String getImageLink() {
        return String.format(traktID + ".jpg");
    }

    public int getSeasonNo() {
        return seasonNo;
    }

    public int getTraktID() {
        return traktID;
    }

    public double getRating() {
        return rating;
    }

    public int getEpisodesCount() {
        return episodes.size();
    }

    public int getAiredEpisodes() {
        return airedEpisodes;
    }

    public String getOverview() {
        return overview;
    }

    public void addFinishedEpisode() {
        ++finishedEpisodes;
    }

    public void removeFinishedEpisode() {
        --finishedEpisodes;
    }

    public int getUnfinished() {
        int i = 0;
        for (Episode e : episodes)
            if (!e.isFinished())
                ++i;
        return i;
    }

    public String getFirstHeader() {
        return Integer.toString(episodesCount);
    }

    public int returnType() {
        return 1;
    }

    public void setFilesDir(File filesDir) {
        this.filesDir = filesDir;
    }

    public Drawable getImageFromFile() {
        try {
            FileInputStream fis = new FileInputStream(new File(filesDir, getImageLink()));
            Bitmap b = BitmapFactory.decodeStream(fis);
            return new BitmapDrawable(Resources.getSystem(), b);
        } catch (Exception e) {
            Log.i("Season.getImage:", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Episode> getEpisodes() {
        return episodes;
    }
}
