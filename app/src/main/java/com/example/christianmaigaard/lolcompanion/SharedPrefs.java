package com.example.christianmaigaard.lolcompanion;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import static com.example.christianmaigaard.lolcompanion.Constants.SHARED_PREFERENCES;
import static com.example.christianmaigaard.lolcompanion.Constants.SUMMONOR_NAME;

public class SharedPrefs {

    private static String LOG = "SharedPrefs";

    // Code source - https://www.journaldev.com/9412/android-shared-preferences-example-tutorial
    public static void storeSummonerNameInSharedPreferences(Context c, String summonerName) {
        Log.d(LOG, "Method called"); // getting String

        SharedPreferences pref = c.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(SUMMONOR_NAME, summonerName); // Storing string
        editor.commit(); // commit changes
    }

    public static String retrieveSummonorNameFromSharedPreferences(Context c) {
        SharedPreferences pref = c.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, 0); // 0 - for private mode
        String summonorName =pref.getString(SUMMONOR_NAME, null);
        return summonorName;
    }
}
