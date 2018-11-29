package com.example.christianmaigaard.lolcompanion.Utilities;

public final class Constants {
    // Api key: Has to be updated every 24 hours atm.
    public static final String API_KEY = "?api_key="+ "RGAPI-2c370ee3-61dc-41b6-b850-5a2e16c70503";

    // end points and url for riot api
    // Building api call example: RIOT_API_BASE_URL + <END_POINT> + API_KEY
    public static final String RIOT_API_BASE_URL = "https://euw1.api.riotgames.com";
    public static final String RIOT_API_SUMMONER_INFO_END_POINT = "/lol/summoner/v3/summoners/by-name/";
    public static final String RIOT_API_BEST_CHAMP_END_POINT = "/lol/champion-mastery/v3/champion-masteries/by-summoner/";
    public static final String RIOT_API_SPECTATOR_END_POINT = "/lol/spectator/v3/active-games/by-summoner/";
    //public static final String SUMMONER_ID = "20129544";


    //Get champions end point
    // Building url: COMMUNITY_DRAGON_CHAMPION_URL + <champkey/id> + .json
    public static final String COMMUNITY_DRAGON_CHAMPION_URL = "http://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/champions/";

    //Broadcast actions
    public static final String BROADCAST_BEST_CHAMPION_ACTION = "BEST_CHAMPION";
    public static final String BROADCAST_SUMMONER_INFO_ACTION = "SUMMONER_INFO";
    public static final String BROADCAST_GAME_PARTICIPANTS_ACTION = "GAME_PARTICIPANTS";

    // EXTRAS
    public static final String BEST_CHAMPION_EXTRA = "BEST_CHAMPION_EXTRA";
    public static final String SUMMONER_INFO_LEVEL_EXTRA = "SUMMONER_INFO_LEVEL_EXTRA";
    public static final String GAME_PARTICIPANTS_EXTRA = "GAME_PARTICIPANTS_EXTRA";
    public static final String LIVE_SUMMONER_INFO = "LIVE_SUMMONER_INFO";

    // Preferences
    public static final String SHARED_PREFERENCES = "UserPreferences";

    // Intent identifiers
    public static final String SUMMONER_NAME = "SummonorName";
    public static final String SUMMONER_LEVEL = "SummonerLevel";
    public static final String SUMMONER_ID = "SummonerID";
    public static final String ERROR = "Error";

    // Volley erros
    public static final String VOLLEY_AUTH_ERROR = "com.android.volley.AuthFailureError";
    public static final String VOLLEY_CONNECTION_ERROR = "com.android.volley.NoConnectionError: java.net.UnknownHostException: Unable to resolve host \"euw1.api.riotgames.com\": No address associated with hostname";



}
