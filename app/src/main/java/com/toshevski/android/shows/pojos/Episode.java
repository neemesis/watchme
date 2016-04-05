package com.toshevski.android.shows.pojos;

import java.io.Serializable;

public class Episode implements Serializable {
    private int seasonNo;
    private int episodeNo;
    private String title;
    private String imdb;
    private String overview;
    private double rating;
    private boolean isFinished;

    public Episode(int seasonNo, int episodeNo, String title, String imdb, String overview, double rating) {
        this.seasonNo = seasonNo;
        this.episodeNo = episodeNo;
        this.title = title;
        this.imdb = imdb;
        this.overview = overview;
        this.rating = rating;
        this.isFinished = false;
    }

    @Override
    public String toString() {
        return "\t\t" + episodeNo + ". " + title;
    }

    public void setIsFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getSeasonNo() {
        return seasonNo;
    }

    public int getEpisodeNo() {
        return episodeNo;
    }

    public String getTitle() {
        return title;
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
}
