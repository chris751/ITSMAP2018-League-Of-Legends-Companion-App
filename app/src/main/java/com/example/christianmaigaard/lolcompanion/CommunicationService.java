package com.example.christianmaigaard.lolcompanion;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Telephony;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.christianmaigaard.lolcompanion.Model.Participant;
import com.example.christianmaigaard.lolcompanion.Utilities.Constants;
import com.example.christianmaigaard.lolcompanion.Model.ParticipantsWrapper;
import com.example.christianmaigaard.lolcompanion.Utilities.SharedPrefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class CommunicationService extends Service {

    // TODO: når man logger ind, vil det være en god idé at gemme SummonerID da det bliver brugt til en del api kald
    private static String LOG = "CommunicationService";

    private long summonerId;

    // Volley Source: https://developer.android.com/training/volley/simple#java
    RequestQueue queue;

    private ArrayList<Participant> playersInGame = new ArrayList<>();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        queue = Volley.newRequestQueue(getApplicationContext());

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

    //region summerInfo methods
    public void createSummonerInfoRequest(String summonerName){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_SUMMONER_INFO_END_POINT + summonerName + Constants.API_KEY, null, new Response.Listener<JSONObject>() {

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
                Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_BEST_CHAMP_END_POINT + summonerID + Constants.API_KEY, null, new Response.Listener<JSONArray>() {
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
                    intent.putExtra(Constants.BEST_CHAMPION_EXTRA, response.getString("name"));
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

    public void createActiveGameRequest(long summonerId){
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_SPECTATOR_END_POINT + summonerId + Constants.API_KEY, null, new Response.Listener<JSONObject>() {
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



    public void createCurrentChampionMasteryRequest(long summonerId, final long championId){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_BEST_CHAMP_END_POINT + summonerId + Constants.API_KEY, null, new Response.Listener<JSONArray>() {
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
            if(fromArrayId == championId){
                Log.d("yoyoyo", "fundet en champ stedet");
                processCurrentChampInfo(champion);
                return;
            }
        }
    }

    private void processCurrentChampInfo(JSONObject champion) throws JSONException {
        int championPoints = champion.getInt("championPoints");
        String summonerSkillInfo = "";

        if(championPoints == Constants.CHAMPION_LEVEL_0){
            summonerSkillInfo = getString(R.string.champion_level_0);
        } else if(Constants.CHAMPION_LEVEL_0 < championPoints && championPoints < Constants.CHAMPION_LEVEL_1){
            summonerSkillInfo = getString(R.string.champion_level_1);
        } else if(Constants.CHAMPION_LEVEL_1 < championPoints && championPoints < Constants.CHAMPION_LEVEL_2){
            summonerSkillInfo = getString(R.string.champion_level_2);
        } else if(Constants.CHAMPION_LEVEL_2 < championPoints && championPoints < Constants.CHAMPION_LEVEL_3){
            summonerSkillInfo = getString(R.string.champion_level_3);
        } else if(Constants.CHAMPION_LEVEL_3 < championPoints && championPoints < Constants.CHAMPION_LEVEL_4){
            summonerSkillInfo = getString(R.string.champion_level_4);
        } else if(Constants.CHAMPION_LEVEL_4 < championPoints && championPoints < Constants.CHAMPION_LEVEL_5){
            summonerSkillInfo = getString(R.string.champion_level_5);
        } else if(Constants.CHAMPION_LEVEL_5 < championPoints && championPoints < Constants.CHAMPION_LEVEL_6){
            summonerSkillInfo = getString(R.string.champion_level_6);
        } else if(Constants.CHAMPION_LEVEL_6 < championPoints && championPoints < Constants.CHAMPION_LEVEL_7){
            summonerSkillInfo = getString(R.string.champion_level_7);
        } else if(Constants.CHAMPION_LEVEL_7 < championPoints && championPoints < Constants.CHAMPION_LEVEL_CRAZY){
            summonerSkillInfo = getString(R.string.champion_level_8);
        }

        Intent intent = new Intent(Constants.BROADCAST_CURRENT_CHAMP_MASTERY_ACTION);
        intent.putExtra(Constants.CURRENT_CHAMP_MASTERY_EXTRA, summonerSkillInfo);

        Log.d("yoyoyo", "sender broadcast stedet");

        sendBroadcast(intent);
    }

}
