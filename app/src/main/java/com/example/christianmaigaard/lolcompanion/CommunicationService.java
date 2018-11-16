package com.example.christianmaigaard.lolcompanion;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.textclassifier.TextLinks;

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

    private void createAllChampionsRequest(){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, Constants.DATA_DRAGON_BASE_URL + Constants.LOL_VERSION_NUMBER + Constants.DD_ALL_CHAMPS_END_POINT + Constants.SUMMONER_ID, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("requestResponse","Response: " + response.toString());
                        // TODO: broadcast svaret
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

    public void createChampionMastoryRequest(long summonerID){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, Constants.RIOT_API_BASE_URL + Constants.RIOT_API_BEST_CHAMP_END_POINT + Constants.SUMMONER_ID + Constants.API_KEY, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try{
                            JSONObject hej = (JSONObject)response.get(1);
                            long hejsa = hej.getLong("championId");
                            Log.d("requestResponse", hejsa + "");
                            findSpecificChampionById(hejsa);
                            // TODO: det er måske nemmest at gemme alle champs plus ID i en lokal database. og så finde det på den måde
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

    private void findSpecificChampionById(long championId){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, Constants.DATA_DRAGON_BASE_URL + Constants.LOL_VERSION_NUMBER + Constants.DD_ALL_CHAMPS_END_POINT + Constants.SUMMONER_ID, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("requestResponse","Response: " + response.toString());
                        // TODO: broadcast svaret
                        try {
                            JSONArray championArray = response.getJSONArray("data");
                            Log.d("requestResponse", championArray.toString());
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
        queue.add(jsonObjectRequest);
    }

    private void iterateThroughChampionListFindId(long championId){

    }

}
