package com.example.christianmaigaard.lolcompanion;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.christianmaigaard.lolcompanion.Utilities.Constants;
import com.example.christianmaigaard.lolcompanion.Utilities.SharedPrefs;

import static com.example.christianmaigaard.lolcompanion.EnterSummonerNameActivity.SUMMONER_NAME;
import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.SUMMONER_ID;
import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.SUMMONER_LEVEL;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String LOG = "MainActivity";

    // UI
    private TextView name;
    private TextView profileIcon;
    private TextView level;
    private TextView bestChamp;
    private ImageView champImage;
    private Button changeName;
    private Button getInfo;
    private Button liveGame;
    // Variables
    private String summonerName;
    private long summonerLevel;
    private long summonerID;
    // Services
    private CommunicationService mService;
    private boolean mBound = false;
    // Broadcasts
    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUiComponents();
        getDataFromSummonerNameActivity();

        // Save summoner name in sharedpreferences
        SharedPrefs.storeSummonerNameInSharedPreferences(this, summonerName);
        SharedPrefs.storeSummonerIdInSharedPreferences(this, summonerID);

//        long summId = SharedPrefs.retrieveSummonorIdFromSharedPreferences(this);
        //      String summName = SharedPrefs.retrieveSummonorNameFromSharedPreferences(this);


        // Start services
        startService(new Intent(this, CommunicationService.class));
        setButtonOnClickListeners();
        startBroadcastReceiver();
        registerIntentFilter();
        updateUI();
    }

    private void getDataFromSummonerNameActivity() {
        // Code inspired heavily from "intentClassExample"
        // Fetch data from "EnterSummonerNameActivity"
        Bundle extras = getIntent().getExtras();
        summonerName = extras.getString(SUMMONER_NAME);
        summonerLevel = extras.getLong(SUMMONER_LEVEL,0);
        summonerID = extras.getLong(SUMMONER_ID,0);
    }

    private void registerIntentFilter() {
        mFilter = new IntentFilter();
        mFilter.addAction(Constants.BROADCAST_BEST_CHAMPION_ACTION);
        mFilter.addAction(Constants.BROADCAST_SUMMONER_INFO_ACTION);
        registerReceiver(mReceiver, mFilter);
    }

    private void startBroadcastReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Constants.BROADCAST_BEST_CHAMPION_ACTION)){
                    String bestChampName = intent.getStringExtra(Constants.BEST_CHAMPION_EXTRA);
                    bestChamp.setText(bestChampName);
                    champImage.setImageDrawable(loadChampImageFromAssets(bestChampName));
                }
                if(intent.getAction().equals(Constants.BROADCAST_SUMMONER_INFO_ACTION)){
                    long summonerLvl = intent.getLongExtra(Constants.SUMMONER_INFO_LEVEL_EXTRA,0);
                    level.setText(summonerLvl+"");
                }
            }
        };
    }

    private void setButtonOnClickListeners() {
        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPrefs.deleteSummonerName(MainActivity.this);
                finish();
            }
        });

        getInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                    mService.getBestChamp();
                }
            }
        });
        liveGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LiveGameActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupUiComponents() {
        name = findViewById(R.id.nameView);
        profileIcon = findViewById(R.id.profileIconView);
        level = findViewById(R.id.levelView);
        bestChamp = findViewById(R.id.bestChampView);
        getInfo = findViewById(R.id.getInfoButton);
        changeName = findViewById(R.id.main_activity_change_name_button);
        champImage = findViewById(R.id.champIcon);
        liveGame = findViewById(R.id.goToLive);
    }

    // Calls all relevant services to gather information about current summoner
    private void callServices() {
        mService.getBestChamp();
    }

    private void updateUI() {
        name.setText(summonerName);
        level.setText(String.valueOf(summonerLevel));
    }


    // Source: https://xjaphx.wordpress.com/2011/10/02/store-and-use-files-in-assets/
    private Drawable loadChampImageFromAssets(String champName) {
        // load image
        try {
            // get input stream
            InputStream ims = getAssets().open("champion/" + champName + ".png");
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            return d;
        }
        catch(IOException ex) {
            return null;
        }
    }

    /*
        System callbacks
    */
    @Override
    protected void onStart(){
        super.onStart();
        Intent intent = new Intent(this, CommunicationService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop(){
        super.onStop();
        unbindService(connection);
        mBound = false;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private ServiceConnection connection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            CommunicationService.LocalBinder binder = (CommunicationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            callServices();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }

    };
}
