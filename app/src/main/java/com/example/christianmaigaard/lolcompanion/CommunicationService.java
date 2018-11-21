package com.example.christianmaigaard.lolcompanion;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class CommunicationService extends Service {

    // TODO: når man logger ind, vil det være en god idé at gemme SummonerID da det bliver brugt til en del api kald

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

    public void createSummonerInfoRequest(String summonerName){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_SUMMONER_INFO_END_POINT + summonerName + Constants.API_KEY, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("requestResponse","Response: " + response.toString());
                        //TODO: broadcast svaret
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

    public void getBestChamp(){
        createChampionMastoryRequest(1111);
    }

    private void createChampionMastoryRequest(long summonerID){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_BEST_CHAMP_END_POINT + Constants.SUMMONER_ID + Constants.API_KEY, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try{
                            JSONObject firstObject = (JSONObject)response.get(0);
                            long firstObjectId = firstObject.getLong("championId");
                            findChampionById(firstObjectId);
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

    private void findChampionById(long championId) throws JSONException {
        String championListString = loadJSONFromAsset(getApplicationContext());
        JSONObject championListJson = new JSONObject(championListString);

        JSONArray onlyChampionList = (JSONArray) championListJson.getJSONArray("data");

//        JSONObject json = championListJson.getJSONObject("data");



        for(int i=0; i < onlyChampionList.length(); i++){
            JSONObject champion = (JSONObject) onlyChampionList.get(i);
            String key = champion.getString("key");
            Long keyAsLong = Long.parseLong(key);
            if(championId == keyAsLong){
                String championName = champion.getString("id");
                Log.d("requestResponse",  championName);
                // TODO: broadcast result
            } else {
                Log.d("requestResponse", "There was no champion with that key");
            }
        }
    }

    //Json reader source: https://stackoverflow.com/questions/13814503/reading-a-json-file-in-android
    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("champion.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
}
