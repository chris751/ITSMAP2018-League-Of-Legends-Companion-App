package com.example.christianmaigaard.lolcompanion;

import android.app.Service;
import android.content.Intent;
import android.graphics.Region;
import android.os.Binder;
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
import com.example.christianmaigaard.lolcompanion.Model.Participant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CommunicationService extends Service {

    // TODO: når man logger ind, vil det være en god idé at gemme SummonerID da det bliver brugt til en del api kald

    private long summonerId;

    // Volley Source: https://developer.android.com/training/volley/simple#java
    RequestQueue queue;


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
                        Log.d("requestResponse","Response: " + response.toString());
                        Intent intent = new Intent(Constants.BROADCAST_SUMMONER_INFO_ACTION);
                        try {
                            saveId(response.getLong("id"));
                            long summonerLvl = response.getLong("summonerLevel");
                            Log.d("requestResponse", summonerLvl+"");
                            intent.putExtra(Constants.SUMMONER_INFO_LEVEL_EXTRA, summonerLvl);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sendBroadcast(intent);
                    }
                }, new ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("requestResponse", "Der skete en fejl");
                        Log.d("requestResponse", error.toString());

                        // TODO: Handle error

                    }
                });
        queue.add(jsonObjectRequest);
    }

    // Save id for future api calls
    private void saveId(long id){
        summonerId = id;
    }
    //endregion

    //region championRequests methods
    public void getBestChamp(){
        createChampionMastoryRequest(42817870);
    }

    private void createChampionMastoryRequest(long summonerID){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_BEST_CHAMP_END_POINT + summonerID + Constants.API_KEY, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try{
                            JSONObject firstObject = (JSONObject)response.get(0);
                            long firstObjectId = firstObject.getLong("championId");
                            createChampionNameByIdRequest(firstObjectId);
                        }catch (JSONException e){
                            Log.d("requestResponse", e.toString());
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.d("requestRespone", error.toString());
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
                Log.d("requestResponse", "Der skete en fejl");
                Log.d("requestResponse", error.toString());

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

                ArrayList<Participant> playersInGame = new ArrayList<Participant>();
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


}
