package com.example.christianmaigaard.lolcompanion;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.christianmaigaard.lolcompanion.Model.Match;
import com.example.christianmaigaard.lolcompanion.Model.MatchWrapper;
import com.example.christianmaigaard.lolcompanion.Model.Participant;
import com.example.christianmaigaard.lolcompanion.Model.Rank;
import com.example.christianmaigaard.lolcompanion.Utilities.Constants;
import com.example.christianmaigaard.lolcompanion.Model.ParticipantsWrapper;
import com.example.christianmaigaard.lolcompanion.Utilities.SharedPrefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;

import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.CHAMPMION_POINTS;

public class CommunicationService extends Service {

    private static String LOG = "CommunicationService";

    private boolean handlerStarted = false;
    private String API_KEY = "";

    private long summonerId;

    // Volley Source: https://developer.android.com/training/volley/simple#java
    RequestQueue queue;

    private ArrayList<Participant> playersInGame = new ArrayList<>();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        queue = Volley.newRequestQueue(getApplicationContext());

        if(!handlerStarted){
            handler.post(periodicUpdate);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        CommunicationService getService(){
            return CommunicationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //region periodic updates
    // Handler and periodic update inspired by: https://wangjingke.com/2016/09/23/Multiple-ways-to-schedule-repeated-tasks-in-android
    Handler handler = new Handler();
    private Runnable periodicUpdate = new Runnable() {
        @Override
        public void run() {
            handlerStarted = true;
            Log.d("ServiceResponse", "handler run");
            checkIfInGame();
            // Loops this method every 1 minutes
            handler.postDelayed(periodicUpdate, 1000*60);

        }
    };

    private void checkIfInGame(){
        long summonerId = SharedPrefs.retrieveSummonorIdFromSharedPreferences(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_SPECTATOR_END_POINT + summonerId +"?api_key="+API_KEY, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // If call is a success broadcast true
                broadcastInGameStatus(true);
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                // null check source: https://stackoverflow.com/questions/22948006/http-status-code-in-android-volley-when-error-networkresponse-is-null
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                    if(error.networkResponse.statusCode == 404){
                        // if call is failure with code 404 no game is active
                        broadcastInGameStatus(false);

                        Intent intent = new Intent(Constants.BROADCAST_END_GAME_ACTION);
                        sendBroadcast(intent);
                    }
                }
            }
        }
        );
        queue.add(jsonObjectRequest);
    }

    private void broadcastInGameStatus(boolean isInGame){
        Intent intent = new Intent(Constants.BROADCAST_IS_IN_GAME_ACTION);
        intent.putExtra(Constants.IS_IN_GAME_EXTRA, isInGame);
        sendBroadcast(intent);
    }


    //endregion

    //region summornerInfo methods
    public void createSummonerInfoRequest(String summonerName){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_SUMMONER_INFO_END_POINT + summonerName +"?api_key="+API_KEY, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(LOG,"Response: " + response.toString());
                        Intent intent = new Intent(Constants.BROADCAST_SUMMONER_INFO_ACTION);
                        try {

                            String name = response.getString(Constants.SUMMONER_NAME_KEY);
                            long summonerLvl = response.getLong(Constants.SUMMONER_LEVEL_KEY);
                            long summonerId = response.getLong(Constants.SUMMONER_ID_KEY);
                            long profileIconId = response.getLong(Constants.SUMMONER_PROFILE_ICON_ID_KEY);
                            long accountId = response.getLong(Constants.SUMMONER_ACCOUNT_ID_KEY);
                            intent.putExtra(Constants.SUMMONER_PROFILE_ICON_ID, profileIconId);
                            intent.putExtra(Constants.SUMMONER_INFO_LEVEL_EXTRA, summonerLvl);
                            intent.putExtra(Constants.SUMMONER_NAME, name);
                            intent.putExtra(Constants.SUMMONER_ID, summonerId);
                            intent.putExtra(Constants.ACCOUNT_ID, accountId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sendBroadcast(intent);
                    }
                }, new ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG, "SummonerInfoRequest error");
                        Log.d(LOG, error.toString());
                        // Broadcast error to receivers
                        Intent intent = new Intent(Constants.BROADCAST_SUMMONER_INFO_ACTION);
                        intent.putExtra(Constants.ERROR, error.toString());
                        sendBroadcast(intent);
                    }
                });
        queue.add(jsonObjectRequest);
    }
    //endregion

    //region bestChampionRequests methods
    public void getBestChamp(){
        long summonerID = SharedPrefs.retrieveSummonorIdFromSharedPreferences(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_BEST_CHAMP_END_POINT + summonerID +"?api_key="+API_KEY, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject firstObject = (JSONObject) response.get(0);
                            long firstObjectId = firstObject.getLong(Constants.CHAMPION_ID_KEY);
                            createChampionNameByIdRequest(firstObjectId);
                        } catch (JSONException e) {
                            Log.d(LOG, e.toString());
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.d(LOG, error.toString());
                    }
                }
        );
        queue.add(jsonArrayRequest);
    }

    public void createChampionNameByIdRequest(long championId){
        String url = Constants.COMMUNITY_DRAGON_CHAMPION_URL;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url + championId + ".json", null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("requestResponse","Response: " + response.toString());
                Intent intent = new Intent(Constants.BROADCAST_BEST_CHAMPION_ACTION);
                try {
                    intent.putExtra(Constants.BEST_CHAMPION_NAME_EXTRA, response.getString(Constants.CHAMPION_NAME_KEY));
                    intent.putExtra(Constants.BEST_CHAMPION_ALIAS_EXTRA, response.getString(Constants.CHAMPION_ALIAS_KEY));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendBroadcast(intent);
            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG, "Der skete en fejl");
                Log.d(LOG, error.toString());

                // TODO: Handle error

            }
        });
        queue.add(request);
    }
    //endregion

    //region activeGameParticipantsRequests methods
    public void createActiveGameRequest(long summonerId){
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_SPECTATOR_END_POINT + summonerId +"?api_key="+API_KEY, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                playersInGame.clear();

                try {
                    JSONArray participants = response.getJSONArray(Constants.GAME_PARTICIPANTS_KEY);
                    //Save all the game participants in a list
                    int i = 0;
                    while(i <participants.length()){
                        JSONObject jsonParticipant = (JSONObject) participants.get(i);
                        Participant p = new Participant(jsonParticipant.getLong(Constants.GAME_PARTICIPANT_ID_KEY), jsonParticipant.getString(Constants.GAME_PARTICIPANT_NAME_KEY), jsonParticipant.getLong(Constants.CHAMPION_ID_KEY));
                        playersInGame.add(p);
                        i++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                matchChampionIdWithNames();
                Log.d("requestResponse", playersInGame.toString());
            }
        }, new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.d("requestResponse", error.toString());
                    }
        }
        );
        queue.add(jsonObjectRequest);
    }

    private void matchChampionIdWithNames(){

        for(int i =0; i<playersInGame.size(); i++){
            createChampIdToNameRequest(playersInGame.get(i).getChampionId(), i);
        }

    }

    private void createChampIdToNameRequest(long champId, final int index){
        String url = Constants.COMMUNITY_DRAGON_CHAMPION_URL;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url + champId + ".json", null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("requestResponse","Response: " + response.toString());
                try {
                    String name = response.getString(Constants.CHAMPION_NAME_KEY);
                    String alias = response.getString(Constants.CHAMPION_ALIAS_KEY);
                    playersInGame.get(index).setChampionName(name);
                    playersInGame.get(index).setChampionAlias(alias);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(index == playersInGame.size()-1){
                    ParticipantsWrapper wrapper = new ParticipantsWrapper(playersInGame);
                    Log.d("playerList", wrapper.toString());
                    Intent intent = new Intent(Constants.BROADCAST_GAME_PARTICIPANTS_ACTION);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.GAME_PARTICIPANTS_EXTRA, wrapper);
                    intent.putExtras(bundle);

                    sendBroadcast(intent);
                }

            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("requestResponse", "Der skete en fejl");
                Log.d("requestResponse", error.toString());

                // TODO: Handle error

            }
        });
        queue.add(request);
    }
    //endregion

    //region CurrentChampMasteryRequest methods
    public void createCurrentChampionMasteryRequest(long summonerId, final long championId){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_BEST_CHAMP_END_POINT + summonerId +"?api_key="+API_KEY, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    Log.d("yoyoyo", "array stedet");
                    findCurrentChamp(response,championId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.d(LOG, error.toString());
                    }
                }
        );
        queue.add(jsonArrayRequest);
    }

    private void findCurrentChamp(JSONArray champArray,long championId) throws JSONException {
        for(int i = 0; i < champArray.length(); i++){
            JSONObject champion = (JSONObject) champArray.get(i);
            long fromArrayId = champion.getLong(Constants.CHAMPION_ID_KEY);
            if(fromArrayId == championId) {
                Log.d("yoyoyo", "fundet en champ stedet");
                processCurrentChampInfo(champion);
                return;
            } else { // champion is not in the players pool, and thus must be 0
                Intent intent = new Intent(Constants.BROADCAST_CURRENT_CHAMP_MASTERY_ACTION);
                intent.putExtra(CHAMPMION_POINTS, 0); // therefore we simply broadcast 0
                sendBroadcast(intent);
            }
        }
    }

    private void processCurrentChampInfo(JSONObject champion) throws JSONException {
        int championPoints = champion.getInt(Constants.CHAMPION_POINTS_KEY);

        Intent intent = new Intent(Constants.BROADCAST_CURRENT_CHAMP_MASTERY_ACTION);
        intent.putExtra(CHAMPMION_POINTS, championPoints);
        Log.d(LOG, "sender broadcast stedet");

        sendBroadcast(intent);
    }
    //endregion
    public void fetchRiotGamesApiKey() {
        String url = "https://sheets.googleapis.com/v4/spreadsheets/11R2RtgPup8uc5N6ePR9KT8eNxKRSoOqVBRmS1H1WZOQ/values/A1?key=AIzaSyB11t2pRGIqX1oR75znhGYyoXYGcCxCJZE";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("requestResponse","Response: " + response.toString());
                Intent intent = new Intent(Constants.BROADCAST_API_KEY);
                try { ;
                    JSONArray values = response.getJSONArray(Constants.GOOGLE_VALUES_KEY);
                    Object apiKey = values.get(0);
                    String api = apiKey.toString();
                    api = api.substring(2, api.length()-2);
                    intent.putExtra(Constants.API_KEY_EXTRA, api);
                    Log.d(LOG, "API: " + api);
                    API_KEY = api;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendBroadcast(intent);
            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG, "Google sheets error");
                Log.d(LOG, error.toString());

                // TODO: Handle error

            }
        });
        queue.add(request);
    }

    //region Match History Methods
    public void createMatchHistoryRequest(long accountId){
        informedMatchHistory.clear();
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_MATCH_HISTORY_END_POINT + accountId+ "?api_key="+API_KEY, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("LISTEN JEG VIL SE", "første kald igennem");
                    ArrayList<Match> matchList = new ArrayList<Match>();
                    JSONArray jsonMatchList = response.getJSONArray(Constants.MATCH_LIST_MATCHES_KEY);
                    for(int i = 0; i < 10; i++){
                        JSONObject jsonMatch = (JSONObject) jsonMatchList.get(i);
                        Match match = new Match(i, jsonMatch.getLong(Constants.MATCH_LIST_GAME_ID_KEY), jsonMatch.getLong(Constants.MATCH_LIST_CHAMPION_IDENTIFIER_KEY));
                        matchList.add(match);
                    }
                    processMatchList(matchList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG, "Der skete en fejl");
                Log.d(LOG, error.toString());

                // TODO: Handle error

            }
        });
        queue.add(request);
    }

    private void processMatchList(ArrayList<Match> matchList){
        for(int i = 0; i < matchList.size(); i++){
            Match match = matchList.get(i);
            processSingleMatch(match, match.getGameId(), match.getChampionId());
        }
    }

    private void processSingleMatch(final Match match, long gameId, final long championId){
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_MATCH_INFO_END_POINT + gameId + "?api_key="+API_KEY, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(LOG, "andet kald igennem");
                try {
                    int kills;
                    int deaths;
                    int assists;
                    boolean win;
                    JSONObject thisPlayer = null;
                    JSONObject thisPlayerStats = null;
                    JSONArray participantList = (JSONArray) response.getJSONArray(Constants.GAME_PARTICIPANTS_KEY);
                    for(int i = 0; i < participantList.length(); i++){
                        JSONObject participant = participantList.getJSONObject(i);
                        if(championId == participant.getInt(Constants.CHAMPION_ID_KEY)){
                            thisPlayer = participant;
                        }
                    }
                    if(thisPlayer!=null){
                        thisPlayerStats = (JSONObject) thisPlayer.getJSONObject(Constants.MATCH_LIST_PARTICIPANT_STATS_KEY);
                    }
                    if(thisPlayerStats!=null){
                        kills = thisPlayerStats.getInt(Constants.MATCH_LIST_PARTICIPANT_STATS_KILLS_KEY);
                        deaths = thisPlayerStats.getInt(Constants.MATCH_LIST_PARTICIPANT_STATS_DEATHS_KEY);
                        assists = thisPlayerStats.getInt(Constants.MATCH_LIST_PARTICIPANT_STATS_ASSISTS_KEY);
                        win = thisPlayerStats.getBoolean(Constants.MATCH_LIST_PARTICIPANT_STATS_WIN_KEY);

                        match.setWin(win);
                        match.setDeaths(deaths);
                        match.setKills(kills);
                        match.setAssists(assists);

                        getChampionAlias(match, championId);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG, "Der skete en fejl");
                Log.d(LOG, error.toString());

                // TODO: Handle error

            }
        });
        queue.add(request);
    }

    private ArrayList<Match> informedMatchHistory = new ArrayList<Match>();

    private void getChampionAlias(final Match match, long championId){
        String url = Constants.COMMUNITY_DRAGON_CHAMPION_URL;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url + championId + ".json", null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("requestResponse","Response: " + response.toString());
                try {
                    String alias = response.getString(Constants.CHAMPION_ALIAS_KEY);
                    match.setChampionAlias(alias);
                    createInformedMatchHistory(match);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("requestResponse", "Der skete en fejl");
                Log.d("requestResponse", error.toString());

                // TODO: Handle error

            }
        });
        queue.add(request);
    }


    private void createInformedMatchHistory(Match match){
        informedMatchHistory.add(match);

        //source: https://stackoverflow.com/questions/16751540/sorting-an-object-arraylist-by-an-attribute-value-in-java
        Collections.sort(informedMatchHistory, new Comparator<Match>() {
            @Override
            public int compare(Match o1, Match o2) {
                if(o1.getOrder() > o2.getOrder()){
                    return 1;
                }
                if(o1.getOrder() < o2.getOrder()){
                    return -1;
                }
                return 0;
            }
        });
        MatchWrapper matchWrapper = new MatchWrapper(informedMatchHistory);
        Intent intent = new Intent(Constants.BROADCAST_MATCH_HISTORY_ACTION);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.MATCH_HISTORY_EXTRA, matchWrapper);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

    //endregion

    //region Ranked stats methods
    public void createSummonerRankRequest(){
        long summonerId = SharedPrefs.retrieveSummonorIdFromSharedPreferences(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_RANK_END_POINT + summonerId +"?api_key="+API_KEY, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for(int i = 0; i < response.length(); i++){
                    try {
                        JSONObject queueType = (JSONObject) response.get(i);
                        if(queueType.getString(Constants.RANK_QUEUE_TYPE_KEY).equals(Constants.RANK_QUEUE_TYPE_RANKED_5V5_KEY)){
                            int wins = queueType.getInt(Constants.RANK_WINS_KEY);
                            int losses = queueType.getInt(Constants.RANK_LOSSES_KEY);
                            String tier = queueType.getString(Constants.RANK_TIER_KEY);

                            Rank rank = new Rank(wins, losses, tier);
                            broadcastRank(rank);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.d(LOG, error.toString());
                    }
                }
        );
        queue.add(jsonArrayRequest);
    }

    private void broadcastRank(Rank rank){
        Intent intent = new Intent(Constants.BROADCAST_RANK_ACTION);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.RANK_EXTRA,rank);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }
    //endregion
}
