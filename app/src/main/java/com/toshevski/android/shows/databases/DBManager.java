package com.toshevski.android.shows.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.toshevski.android.shows.pojos.Episode;
import com.toshevski.android.shows.pojos.Season;
import com.toshevski.android.shows.pojos.Series;

import java.util.ArrayList;

public class DBManager extends SQLiteOpenHelper {

    private Context ctx;

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "WatchMeDB";

    // All tables..
    private static final String TBL_SERIES = "Series";
    private static final String TBL_SEASONS = "Seasons";
    private static final String TBL_EPISODES =  "Episodes";
    private static final String TBL_POPULAR = "Popular";
    private static final String TBL_TRENDING = "Trending";

    // Series attributes...
    private static final String SERIES_position = "position"; // place in list = id
    private static final String SERIES_imdb = "imdb";
    private static final String SERIES_title = "title";
    private static final String SERIES_year = "year";
    private static final String SERIES_overview = "overview";
    private static final String SERIES_rating = "rating";
    private static final String SERIES_genre = "genre";
    private static final String SERIES_totalEp = "totalEp";

    // Seasons attributes...
    private static final String SEASON_traktID = "traktID";
    private static final String SEASON_seasonNo = "seasonNo";
    private static final String SEASON_rating = "rating";
    private static final String SEASON_totalEp = "totalEp";
    private static final String SEASON_overview = "overview";
    private static final String SEASON_owner = "owner";

    // Episode attributes...
    private static final String EPISODE_title = "title";
    private static final String EPISODE_imdb = "imdb";
    private static final String EPISODE_overview = "overview";
    private static final String EPISODE_rating = "rating";
    private static final String EPISODE_episodeNo = "episodeNo";
    private static final String EPISODE_owner = "owner";
    private static final String EPISODE_finished = "finished";


    public DBManager(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
        this.ctx = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SERIES_TBL = "CREATE TABLE " + TBL_SERIES + "(" +
                SERIES_imdb + " TEXT PRIMARY KEY, " + SERIES_position + " INTEGER, " +
                SERIES_genre + " TEXT, " + SERIES_overview + " TEXT, " + SERIES_rating + " REAL, " +
                SERIES_title + " TEXT, " + SERIES_totalEp + " INTEGER, " + SERIES_year + " INTEGER)";
        String CREATE_SEASONS_TBL = "CREATE TABLE " + TBL_SEASONS + "(" + SEASON_traktID + " " +
                "INTEGER PRIMARY KEY, " + SEASON_rating + " REAL, " + SEASON_seasonNo + " INTEGER, " +
                SEASON_totalEp + " INTEGER, " + SEASON_overview + " TEXT, " + SEASON_owner + " TEXT)";
        String CREATE_EPISODES_TBL = "CREATE TABLE " + TBL_EPISODES + "(" +
                EPISODE_imdb + " TEXT PRIMARY KEY, " + EPISODE_episodeNo + " INTEGER, " +
                EPISODE_overview + " TEXT, " + EPISODE_rating + " REAL, " + EPISODE_title + " TEXT, " +
                EPISODE_owner + " INTEGER, " + EPISODE_finished + " INTEGER)";
        String CREATE_POPULAR_TBL = "CREATE TABLE " + TBL_POPULAR + "(" +
                SERIES_imdb + " TEXT PRIMARY KEY, " + SERIES_position + " INTEGER, " +
                SERIES_genre + " TEXT, " + SERIES_overview + " TEXT, " + SERIES_rating + " REAL, " +
                SERIES_title + " TEXT, " + SERIES_totalEp + " INTEGER, " + SERIES_year + " INTEGER)";
        String CREATE_TRENDIND_TBL = "CREATE TABLE " + TBL_TRENDING + "(" +
                SERIES_imdb + " TEXT PRIMARY KEY, " + SERIES_position + " INTEGER, " +
                SERIES_genre + " TEXT, " + SERIES_overview + " TEXT, " + SERIES_rating + " REAL, " +
                SERIES_title + " TEXT, " + SERIES_totalEp + " INTEGER, " + SERIES_year + " INTEGER)";

        db.execSQL(CREATE_SERIES_TBL);
        db.execSQL(CREATE_SEASONS_TBL);
        db.execSQL(CREATE_EPISODES_TBL);
        db.execSQL(CREATE_POPULAR_TBL);
        db.execSQL(CREATE_TRENDIND_TBL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO
    }

    public void addSeries(Series s, int position) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(SERIES_position, position);
        cv.put(SERIES_imdb, s.getImdb());
        cv.put(SERIES_title, s.getTitle());
        cv.put(SERIES_year, s.getYear());
        cv.put(SERIES_overview, s.getOverview());
        cv.put(SERIES_rating, s.getRating());
        cv.put(SERIES_genre, s.getGenre());
        cv.put(SERIES_totalEp, s.getTotalEpisodes());

        db.insert(TBL_SERIES, null, cv);
        db.close();
    }

    public void delSeries(Series s) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TBL_SERIES, SERIES_imdb + " = ?", new String[] { s.getImdb() });
        db.close();
    }

    public ArrayList<Series> getAllSeries() {
        ArrayList<Series> s = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TBL_SERIES + " ORDER BY " +
                SERIES_position + " ASC;", null);

        if (cursor.moveToFirst()) {
            do {
                String imdb = cursor.getString(0);
                String genre = cursor.getString(2);
                String overview = cursor.getString(3);
                double rating = Double.parseDouble(cursor.getString(4));
                String title = cursor.getString(5);
                int totalEp = Integer.parseInt(cursor.getString(6));
                int year = Integer.parseInt(cursor.getString(7));

                Series ser = new Series(title, year, imdb, overview, rating, genre, "", Series.ImageStatus.DOWN);
                ser.setTotalEpisodes(totalEp);
                s.add(ser);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return s;
    }

    public void addSeason(Season s, String owner) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(SEASON_traktID, s.getTraktID());
        cv.put(SEASON_seasonNo, s.getSeasonNo());
        cv.put(SEASON_rating, s.getRating());
        cv.put(SEASON_totalEp, s.getEpisodesCount());
        cv.put(SEASON_overview, s.getOverview());
        cv.put(SEASON_owner, owner);

        db.insert(TBL_SEASONS, null, cv);
        db.close();
    }

    public void delSeason(Season s) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TBL_SEASONS, SEASON_traktID + " = ?", new String[] { String.valueOf(s.getTraktID()) });
        db.close();
    }

    public ArrayList<Season> getAllSeasons() {
        ArrayList<Season> seasons = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TBL_SEASONS + " ORDER BY " +
                SEASON_seasonNo + " ASC;", null);

        if (cursor.moveToFirst()) {
            do {
                int traktID = Integer.parseInt(cursor.getString(0));
                double rating = Double.parseDouble(cursor.getString(1));
                int seasonNo = Integer.parseInt(cursor.getString(2));
                int totalEp = Integer.parseInt(cursor.getString(3));
                String overview = cursor.getString(4);
                Season s = new Season(seasonNo, traktID, rating, totalEp, -1, overview);
                seasons.add(s);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return seasons;
    }

    public ArrayList<Series> getEverything() {
        ArrayList<Series> ser = getAllSeries();

        for (Series a : ser) {
            ArrayList<Season> sea = getSeasonsForOneSeries(a.getImdb());
            for (Season b : sea) {
                b.addAllEpisodes(getEpisodesForOneSeason(b.getTraktID()));
            }
            a.addAllSeasons(sea);
            ser.add(a);
        }
        return ser;
    }

    public void saveEverything(ArrayList<Series> series) {
        for (int i = 0; i < series.size(); ++i) {
            Series s = series.get(i);
            ArrayList<Season> sea = s.getSeasons();
            for (Season b : sea) {
                ArrayList<Episode> ep = b.getEpisodes();
                for (Episode c : ep) {
                    addEpisode(c, b.getTraktID());
                }
                addSeason(b, s.getImdb());
            }
            addSeries(s, i);
        }
    }

    public ArrayList<Season> getSeasonsForOneSeries(String imdb) {
        ArrayList<Season> seasons = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TBL_SEASONS + " WHERE " +
                SEASON_owner + " = '" + imdb + "' ORDER BY " +
                SEASON_seasonNo + " ASC;", null);

        if (cursor.moveToFirst()) {
            do {
                int traktID = Integer.parseInt(cursor.getString(0));
                double rating = Double.parseDouble(cursor.getString(1));
                int seasonNo = Integer.parseInt(cursor.getString(2));
                int totalEp = Integer.parseInt(cursor.getString(3));
                String overview = cursor.getString(4);
                Season s = new Season(seasonNo, traktID, rating, totalEp, -1, overview);
                seasons.add(s);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return seasons;
    }

    public void addEpisode(Episode e, int owner) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(EPISODE_title, e.getTitle());

        if (!e.getImdb().equals("null")) cv.put(EPISODE_imdb, e.getImdb());
        else cv.put(EPISODE_imdb, e.getEpisodeNo() + "." + e.getTitle());
        cv.put(EPISODE_overview, e.getOverview());
        cv.put(EPISODE_rating, e.getRating());
        cv.put(EPISODE_episodeNo, e.getEpisodeNo());
        cv.put(EPISODE_owner, owner);
        cv.put(EPISODE_finished, (e.isFinished() ? 1 : 0));

        db.insert(TBL_EPISODES, null, cv);
        db.close();
    }

    public void delEpisode(Episode e) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TBL_EPISODES, EPISODE_imdb + " = ?", new String[] { e.getImdb() });
        db.close();
    }

    public ArrayList<Episode> getAllEpisodes() {
        ArrayList<Episode> episodes = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TBL_EPISODES + " ORDER BY " +
                EPISODE_episodeNo + " ASC;", null);

        if (cursor.moveToFirst()) {
            do {
                String imdb = cursor.getString(0);
                int episodeNo = Integer.parseInt(cursor.getString(1));
                String overview = cursor.getString(2);
                double rating = Double.parseDouble(cursor.getString(3));
                String title = cursor.getString(4);
                int owner = Integer.parseInt(cursor.getString(5));
                boolean finished = Integer.parseInt(cursor.getString(6)) == 1;
                Episode e = new Episode(owner, episodeNo, title, imdb, overview, rating);
                e.setIsFinished(finished);
                episodes.add(e);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return episodes;
    }

    public ArrayList<Episode> getEpisodesForOneSeason(int traktID) {
        ArrayList<Episode> episodes = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TBL_EPISODES + " WHERE " +
                EPISODE_owner + " =? " + " ORDER BY " +
                EPISODE_episodeNo + " ASC;", new String[] { String.valueOf(traktID) }) ;

        if (cursor.moveToFirst()) {
            do {
                String imdb = cursor.getString(0);
                int episodeNo = Integer.parseInt(cursor.getString(1));
                String overview = cursor.getString(2);
                double rating = Double.parseDouble(cursor.getString(3));
                String title = cursor.getString(4);
                int owner = Integer.parseInt(cursor.getString(5));
                boolean finished = Integer.parseInt(cursor.getString(6)) == 1;
                Episode e = new Episode(owner, episodeNo, title, imdb, overview, rating);
                e.setIsFinished(finished);
                episodes.add(e);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return episodes;
    }

    public void addPopular(Series s) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(SERIES_position, 0);
        cv.put(SERIES_imdb, s.getImdb());
        cv.put(SERIES_title, s.getTitle());
        cv.put(SERIES_year, s.getYear());
        cv.put(SERIES_overview, s.getOverview());
        cv.put(SERIES_rating, s.getRating());
        cv.put(SERIES_genre, s.getGenre());
        cv.put(SERIES_totalEp, s.getTotalEpisodes());

        db.insert(TBL_POPULAR, null, cv);
        db.close();
    }

    public void delPopular(Series s) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TBL_POPULAR, SERIES_imdb + " = ?", new String[] { s.getImdb() });
        db.close();
    }

    public void addTrending(Series s) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(SERIES_position, 0);
        cv.put(SERIES_imdb, s.getImdb());
        cv.put(SERIES_title, s.getTitle());
        cv.put(SERIES_year, s.getYear());
        cv.put(SERIES_overview, s.getOverview());
        cv.put(SERIES_rating, s.getRating());
        cv.put(SERIES_genre, s.getGenre());
        cv.put(SERIES_totalEp, s.getTotalEpisodes());

        db.insert(TBL_TRENDING, null, cv);
        db.close();
    }

    public void delTrending(Series s) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TBL_TRENDING, SERIES_imdb + " = ?", new String[] { s.getImdb() });
        db.close();
    }

    public void delDB() {
        ctx.deleteDatabase(DB_NAME);
    }
}
