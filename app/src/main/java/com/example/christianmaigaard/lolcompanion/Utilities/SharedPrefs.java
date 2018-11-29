package com.example.christianmaigaard.lolcompanion.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.SHARED_PREFERENCES;
import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.SUMMONER_ID;
import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.SUMMONER_NAME;

public class SharedPrefs {

    private static String LOG = "SharedPrefs";

    // Code source - https://www.journaldev.com/9412/android-shared-preferences-example-tutorial
    public static void storeSummonerNameInSharedPreferences(Context c, String summonerName) {

        SharedPreferences pref = c.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(SUMMONER_NAME, summonerName); // Storing string
        editor.apply(); // commit changes
    }

    public static void storeSummonerIdInSharedPreferences(Context c, long summonerId) {

        SharedPreferences pref = c.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        editor.putLong(SUMMONER_ID, summonerId);
        editor.apply(); // commit changes
    }

    public static String retrieveSummonorNameFromSharedPreferences(Context c) {
        SharedPreferences pref = c.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, 0); // 0 - for private mode
        return pref.getString(SUMMONER_NAME, null);
    }

    public static long retrieveSummonorIdFromSharedPreferences(Context c) {
        SharedPreferences pref = c.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, 0); // 0 - for private mode
        return pref.getLong(SUMMONER_ID,0);
    }

    public static void deleteSummonerName(Context c) {
        SharedPreferences preferences = c.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, 0);
        preferences.edit().remove(SUMMONER_NAME).apply();
    }
}
