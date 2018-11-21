package com.example.christianmaigaard.lolcompanion;

public final class Constants {
    // Api key: Has to be updated every 24 hours atm.
    public static final String API_KEY = "?api_key="+ "RGAPI-7c69aed3-6f13-4096-be28-2a0fd9d9ec9f";

    // end points and url for riot api
    // Building api call example: RIOT_API_BASE_URL + <END_POINT> + API_KEY
    public static final String RIOT_API_BASE_URL = "https://euw1.api.riotgames.com";
    public static final String RIOT_API_SUMMONER_INFO_END_POINT = "/lol/summoner/v3/summoners/by-name/";
    public static final String RIOT_API_BEST_CHAMP_END_POINT = "/lol/champion-mastery/v3/champion-masteries/by-summoner/";
    public static final String SUMMONER_ID = "20129544";


    public static final String DATA_DRAGON = "http://ddragon.leagueoflegends.com/cdn/8.23.1/data/en_US/champion.json";
}
