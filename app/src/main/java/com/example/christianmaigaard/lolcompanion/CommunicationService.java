package com.example.christianmaigaard.lolcompanion;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class CommunicationService extends Service {

    // TODO: når man logger ind, vil det være en god idé at gemme SummonerID da det bliver brugt til en del api kald

    // Volley Source: https://developer.android.com/training/volley/simple#java
    RequestQueue queue;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        queue = Volley.newRequestQueue(getApplicationContext());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void createSummonerInfoRequest(String summonerName){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_SUMMONER_INFO_END_POINT + Constants.API_KEY, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("requestResponse","Response: " + response.toString());
                        //TODO: broadcast svaret
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("requestResponse", "Der skete en fejl");
                        Log.d("requestResponse", error.toString());

                        // TODO: Handle error

                    }
                });
        queue.add(jsonObjectRequest);
    }

    public void createAllChampionsRequest(){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, Constants.DATA_DRAGON_BASE_URL + Constants.LOL_VERSION_NUMBER + Constants.DD_ALL_CHAMPS_END_POINT, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("requestResponse","Response: " + response.toString());
                        // TODO: broadcast svaret
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("requestResponse", "Der skete en fejl");
                        Log.d("requestResponse", error.toString());

                        // TODO: Handle error

                    }
                });
        queue.add(jsonObjectRequest);
    }

}
