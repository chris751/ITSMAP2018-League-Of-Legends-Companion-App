package com.example.christianmaigaard.lolcompanion;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

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
                if(error.networkResponse.statusCode == 404){
                    // if call is failure with code 404 no game is active
                    broadcastInGameStatus(false);
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

                            String name = response.getString("name");
                            long summonerLvl = response.getLong("summonerLevel");
                            long summonerId = response.getLong("id");
                            //Log.d(LOG, summonerLvl+"");
                            intent.putExtra(Constants.SUMMONER_INFO_LEVEL_EXTRA, summonerLvl);
                            intent.putExtra(Constants.SUMMONER_NAME, name);
                            intent.putExtra(Constants.SUMMONER_ID, summonerId);
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
                            long firstObjectId = firstObject.getLong("championId");
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
                    intent.putExtra(Constants.BEST_CHAMPION_NAME_EXTRA, response.getString("name"));
                    intent.putExtra(Constants.BEST_CHAMPION_ALIAS_EXTRA, response.getString("alias"));
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
                    JSONArray participants = response.getJSONArray("participants");
                    //Save all the game participants in a list
                    int i = 0;
                    while(i <participants.length()){
                        JSONObject jsonParticipant = (JSONObject) participants.get(i);
                        Participant p = new Participant(jsonParticipant.getLong("summonerId"), jsonParticipant.getString("summonerName"), jsonParticipant.getLong("championId"));
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
                    String name = response.getString("name");
                    String alias = response.getString("alias");
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
            long fromArrayId = champion.getLong("championId");
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
        int championPoints = champion.getInt("championPoints");

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
                    JSONArray values = response.getJSONArray("values");
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
                    JSONArray jsonMatchList = response.getJSONArray("matches");
                    for(int i = 0; i < 10; i++){
                        JSONObject jsonMatch = (JSONObject) jsonMatchList.get(i);
                        Match match = new Match(i, jsonMatch.getLong("gameId"), jsonMatch.getLong("champion"));
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
                    JSONArray participantList = (JSONArray) response.getJSONArray("participants");
                    for(int i = 0; i < participantList.length(); i++){
                        JSONObject participant = participantList.getJSONObject(i);
                        if(championId == participant.getInt("championId")){
                            thisPlayer = participant;
                        }
                    }
                    if(thisPlayer!=null){
                        thisPlayerStats = (JSONObject) thisPlayer.getJSONObject("stats");
                    }
                    if(thisPlayerStats!=null){
                        kills = thisPlayerStats.getInt("kills");
                        deaths = thisPlayerStats.getInt("deaths");
                        assists = thisPlayerStats.getInt("assists");
                        win = thisPlayerStats.getBoolean("win");

                        match.setWin(win);
                        match.setDeaths(deaths);
                        match.setKills(kills);
                        match.setAssists(assists);

                        createInformedMatchHistory(match);
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

    public void createChampionRankRequest(){
        long summonerId = SharedPrefs.retrieveSummonorIdFromSharedPreferences(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_RANK_END_POINT + summonerId +"?api_key="+API_KEY, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for(int i = 0; i < response.length(); i++){
                    try {
                        JSONObject queueType = (JSONObject) response.get(i);
                        if(queueType.getString("queueType").equals("RANKED_SOLO_5x5")){
                            int wins = queueType.getInt("wins");
                            int losses = queueType.getInt("losses");
                            String tier = queueType.getString("tier");

                            Rank rank = new Rank(wins, losses, tier);


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
        //Intent intent = new
    }

}
