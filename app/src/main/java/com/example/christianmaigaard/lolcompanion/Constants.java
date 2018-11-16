package com.example.christianmaigaard.lolcompanion;

public final class Constants {
    // Api key: Has to be updated every 24 hours atm.
    public static final String API_KEY = "?api_key="+ "RGAPI-31293194-ea69-4897-a70b-f9b826cbe282";

    // end points and url for riot api
    // Building api call example: RIOT_API_BASE_URL + <END_POINT> + API_KEY
    public static final String RIOT_API_BASE_URL = "https://euw1.api.riotgames.com";
    public static final String RIOT_API_SUMMONER_INFO_END_POINT = "/lol/summoner/v3/summoners/by-name/";
    public static final String RIOT_API_BEST_CHAMP_END_POINT = "/lol/champion-mastery/v3/champion-masteries/by-summoner/";
    public static final String SUMMONER_ID = "20129544";


    // end points and url for data dragon api
    // Building api call example: DATA_DRAGON_BASE_URL + LOL_VERSION_NUMBER + <END_POINT>
    public static final String DATA_DRAGON_BASE_URL = "http://ddragon.leagueoflegends.com/cdn/";
    public static final String LOL_VERSION_NUMBER = "8.22.1"; // TODO: Kan måske opdateres igennem riot static api
    public static final String DD_ALL_CHAMPS_END_POINT = "/data/en_US/champion.json";
}
