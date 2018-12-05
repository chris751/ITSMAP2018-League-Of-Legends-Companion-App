package com.example.christianmaigaard.lolcompanion.Utilities;

public final class Constants {
    // Api key: Has to be updated every 24 hours atm.
    //public static final String API_KEY = "?api_key="+ "RGAPI-2c370ee3-61dc-41b6-b850-5a2e16c70503";

    // end points and url for riot api
    // Building api call example: RIOT_API_BASE_URL + <END_POINT> + API_KEY
    public static final String RIOT_API_BASE_URL = "https://euw1.api.riotgames.com";
    public static final String RIOT_API_SUMMONER_INFO_END_POINT = "/lol/summoner/v3/summoners/by-name/";
    public static final String RIOT_API_BEST_CHAMP_END_POINT = "/lol/champion-mastery/v3/champion-masteries/by-summoner/";
    public static final String RIOT_API_SPECTATOR_END_POINT = "/lol/spectator/v3/active-games/by-summoner/";
    public static final String RIOT_API_MATCH_HISTORY_END_POINT = "/lol/match/v3/matchlists/by-account/";
    public static final String RIOT_API_MATCH_INFO_END_POINT = "/lol/match/v3/matches/";
    public static final String RIOT_API_RANK_END_POINT = "lol/league/v3/positions/by-summoner/";



    //Get champions end point
    // Building url: COMMUNITY_DRAGON_CHAMPION_URL + <champkey/id> + .json
    public static final String COMMUNITY_DRAGON_CHAMPION_URL = "http://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/champions/";

    //Broadcast actions
    public static final String BROADCAST_BEST_CHAMPION_ACTION = "BEST_CHAMPION";
    public static final String BROADCAST_SUMMONER_INFO_ACTION = "SUMMONER_INFO";
    public static final String BROADCAST_GAME_PARTICIPANTS_ACTION = "GAME_PARTICIPANTS";
    public static final String BROADCAST_CURRENT_CHAMP_MASTERY_ACTION = "CURRENT_CHAMP_MASTERY_ACTION";
    public static final String BROADCAST_IS_IN_GAME_ACTION = "BROADCAST_IS_IN_GAME_ACTION";
    public static final String BROADCAST_API_KEY = "API_KEY";
    public static final String BROADCAST_MATCH_HISTORY_ACTION = "BROADCAST_MATCH_HISTORY_ACTION";
    public static final String BROADCAST_RANK_ACTION = "BROADCAST_RANK_ACTION";

    // EXTRAS
    public static final String BEST_CHAMPION_NAME_EXTRA = "BEST_CHAMPION_NAME_EXTRA";
    public static final String BEST_CHAMPION_ALIAS_EXTRA = "BEST_CHAMPION_ALIAS_EXTRA";
    public static final String SUMMONER_INFO_LEVEL_EXTRA = "SUMMONER_INFO_LEVEL_EXTRA";
    public static final String GAME_PARTICIPANTS_EXTRA = "GAME_PARTICIPANTS_EXTRA";
    public static final String LIVE_SUMMONER_INFO_EXTRA = "LIVE_SUMMONER_INFO_EXTRA";
    public static final String CURRENT_CHAMP_MASTERY_EXTRA = "CURRENT_CHAMP_MASTERY_EXTRA";
    public static final String IS_IN_GAME_EXTRA = "IS_IN_GAME_EXTRA";
    public static final String API_KEY_EXTRA = "API_KEY_EXTRA";
    public static final String MATCH_HISTORY_EXTRA = "MATCH_HISTORY_EXTRA";
    public static final String RANK_EXTRA = "RANK_EXTRA";

    // Preferences
    public static final String SHARED_PREFERENCES = "UserPreferences";

    // Intent identifiers
    public static final String SUMMONER_NAME = "SummonorName";
    public static final String SUMMONER_LEVEL = "SummonerLevel";
    public static final String SUMMONER_ID = "SummonerID";
    public static final String ERROR = "Error";
    public static final String ACCOUNT_ID = "AccountID";

    // Volley erros
    public static final String VOLLEY_AUTH_ERROR = "com.android.volley.AuthFailureError";
    public static final String VOLLEY_CONNECTION_ERROR = "com.android.volley.NoConnectionError: java.net.UnknownHostException: Unable to resolve host \"euw1.api.riotgames.com\": No address associated with hostname";

    // Champion mastery ratings
    public static final int CHAMPION_LEVEL_0 = 0;
    public static final int CHAMPION_LEVEL_1 = 1800;
    public static final int CHAMPION_LEVEL_2 = 6000;
    public static final int CHAMPION_LEVEL_3 = 12600;
    public static final int CHAMPION_LEVEL_4 = 21600;
    public static final int CHAMPION_LEVEL_5 = 35000;
    public static final int CHAMPION_LEVEL_6 = 50000;
    public static final int CHAMPION_LEVEL_7 = 100000;
    public static final int CHAMPION_LEVEL_CRAZY = 1000000;

    //
}
