package com.toshevski.android.shows.pojos;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Series implements Serializable {

    public enum ImageStatus {
        NO, DOWN, ONLINE
    }

    private String title;
    private int year;
    private String imdb;
    private String overview;
    private double rating;
    private String genre;
    private String imageLink;
    private ArrayList<Season> seasons;
    private Drawable image;
    private ImageStatus status;
    private int lastTimeUnfinished;
    private int totalEpisodes;
    private int totalSpecialEpisodes;
    public boolean selected;
    private boolean refresh;
    private File filesDir;

    public Series(String title, int year, String imdb, String overview,
                  double rating, String genre, String imageLink, ImageStatus status) {
        this.title = title;
        this.year = year;
        this.imdb = imdb;
        this.overview = overview;
        this.rating = rating;
        this.genre = genre;
        this.seasons = new ArrayList<>();
        this.imageLink = imageLink;
        this.status = status;
        this.selected = false;
        this.totalEpisodes = -1;
        this.lastTimeUnfinished = -1;
        this.refresh = true;
    }

    @Override
    public String toString() {
        return title;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public Season addSeason(Season s) {
        for (int i = 0; i < seasons.size(); ++i) {
            if (s.getTraktID() == seasons.get(i).getTraktID()) {
                Season old = seasons.get(i);
                old.setRating(s.getRating());
                old.setAiredEpisodes(s.getAiredEpisodes());
                old.setEpisodesCount(s.getEpisodesCount());
                old.setOverview(s.getOverview());
                return old;
            }
        }
        seasons.add(s);
        return s;
    }

    public void setAllFinished(Boolean how) {
        for (Season s : seasons) {
            s.setAllFinished(how);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getTotalSpecialEpisodes() {
        return totalSpecialEpisodes;
    }

    public void setTotalSpecialEpisodes(int totalSpecialEpisodes) {
        this.totalSpecialEpisodes = totalSpecialEpisodes;
    }

    public int getNumberOfSeasons() {
        return seasons.size();
    }

    public void removePictures(File filesDir) {
        for (Season s : seasons) {
            Log.i("Series.removePic:", "Brishenje: " + s.getTitle());
            File f = new File(filesDir, s.getImageLink());
            if (f.exists())
                f.delete();
        }
    }

    public Season getOneSeason(int position) {
        return seasons.get(position);
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

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setTotalEpisodes(int totalEpisodes) {
        this.totalEpisodes = totalEpisodes;
    }

    public int getTotalEpisodes() {
        return totalEpisodes + totalSpecialEpisodes;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public int getUnfinished() {
        int counter = 0;
        for (Season s : seasons) {
            counter += s.getUnfinished();
        }
        return counter;
    }

    public String getFirstHeader() {
        return Integer.toString(year);
    }

    public int returnType() {
        return 0;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public void setStatus(ImageStatus status) {
        this.status = status;
    }

    public Drawable getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public ImageStatus getStatus() {
        return status;
    }

    public int getYear() {
        return year;
    }

    public String getImdb() {
        return imdb;
    }

    public String getOverview() {
        return overview;
    }

    public double getRating() {
        return rating;
    }

    public String getImageLink() {
        return imdb + ".jpg";
    }

    public String getGenre() {
        return genre;
    }

    public ArrayList<Season> getSeasons() {
        return seasons;
    }

}
