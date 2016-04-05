package com.toshevski.android.shows.pojos;

public class Keys {
    public static String contentType = "aplication/json";
    public static String apiVersion = "2";
    public static String apiKey = "44852dfd06e564559ffda22f37bc74bff05c76019096ac56278df9af1967ee07";
    public static String searchLinkBeforeName = "https://api-v2launch.trakt.tv/search?query=";
    public static String searchLinkAfterName = "&type=show&extended=full";
    public static String refreshShowEpisodesBeforeName = "https://api-v2launch.trakt.tv/shows/";
    public static String refreshShowEpisodesAfterName = "/seasons?extended=episodes,full,images";
    public static String refreshShowBeforeName = "https://api-v2launch.trakt.tv/shows/";
    public static String refreshShowAfterName = "?extended=full,images";
    public static String trendingShows = "https://api-v2launch.trakt.tv/shows/trending?extended=full,images";
    public static String popularShows = "https://api-v2launch.trakt.tv/shows/popular?extended=full,images";
}
