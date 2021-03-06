package com.example.christianmaigaard.lolcompanion;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.christianmaigaard.lolcompanion.Model.Match;
import com.example.christianmaigaard.lolcompanion.Model.MatchWrapper;
import com.example.christianmaigaard.lolcompanion.Model.Participant;
import com.example.christianmaigaard.lolcompanion.Model.ParticipantsWrapper;
import com.example.christianmaigaard.lolcompanion.Adapter.MatchHistoryListAdapter;
import com.example.christianmaigaard.lolcompanion.Model.Rank;
import com.example.christianmaigaard.lolcompanion.Utilities.AssetHelper;
import com.example.christianmaigaard.lolcompanion.Utilities.Constants;
import com.example.christianmaigaard.lolcompanion.Utilities.Dialog;
import com.example.christianmaigaard.lolcompanion.Utilities.SharedPrefs;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.BROADCAST_MATCH_HISTORY_ACTION;
import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.BROADCAST_RANK_ACTION;
import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.MATCH_HISTORY_EXTRA;
import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.ACCOUNT_ID;
import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.SUMMONER_ID;
import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.SUMMONER_LEVEL;
import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.SUMMONER_NAME;
import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.SUMMONER_PROFILE_ICON_ID;

public class MainActivity extends AppCompatActivity {

    private static final String LOG = "MainActivity";

    // UI
    private TextView name;
    private TextView level;
    private TextView bestChamp;
    private ImageView champImage;
    private ImageView summonerProfileImage;
    private Button changeName;
    private Button liveGame;
    private ListView matchHistoryView;
    private ImageView rankedTier;
    private TextView winLossView;
    // Variables
    private String summonerName;
    private long summonerLevel;
    private long summonerID;
    private long summonerProfileId;
    private boolean mIsInGame = false;
    private long accountID;
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
        summonerProfileId = extras.getLong(SUMMONER_PROFILE_ICON_ID,0);
        accountID = extras.getLong(ACCOUNT_ID,0);
    }

    private void setupList(ArrayList<Match> matchList){
        MatchHistoryListAdapter matchHistoryListAdapter = new MatchHistoryListAdapter(this, matchList);
        matchHistoryView.setAdapter(matchHistoryListAdapter);
    }

    private void registerIntentFilter() {
        mFilter = new IntentFilter();
        mFilter.addAction(Constants.BROADCAST_BEST_CHAMPION_ACTION);
        mFilter.addAction(Constants.BROADCAST_SUMMONER_INFO_ACTION);
        mFilter.addAction(Constants.BROADCAST_IS_IN_GAME_ACTION);
        mFilter.addAction((BROADCAST_MATCH_HISTORY_ACTION));
        mFilter.addAction(BROADCAST_RANK_ACTION);
        registerReceiver(mReceiver, mFilter);
    }

    private void startBroadcastReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Constants.BROADCAST_BEST_CHAMPION_ACTION)){
                    String bestChampName = intent.getStringExtra(Constants.BEST_CHAMPION_NAME_EXTRA);
                    String bestChampAlias = intent.getStringExtra(Constants.BEST_CHAMPION_ALIAS_EXTRA);
                    bestChamp.setText(bestChampName);
                    champImage.setImageDrawable(AssetHelper.loadChampImageFromAssets(MainActivity.this, bestChampAlias));
                }
                if(intent.getAction().equals(Constants.BROADCAST_SUMMONER_INFO_ACTION)){
                    long summonerLvl = intent.getLongExtra(Constants.SUMMONER_INFO_LEVEL_EXTRA,0);
                    long profileIconId = intent.getLongExtra(Constants.SUMMONER_PROFILE_ICON_ID,0);

                    level.setText(summonerLvl+"");
                }
                if(intent.getAction().equals(Constants.BROADCAST_IS_IN_GAME_ACTION)){
                    processInGameStatus(intent.getBooleanExtra(Constants.IS_IN_GAME_EXTRA,false));
                }
                if(intent.getAction().equals(Constants.BROADCAST_MATCH_HISTORY_ACTION)){
                    MatchWrapper wrapper = (MatchWrapper) intent.getSerializableExtra(Constants.MATCH_HISTORY_EXTRA);
                    ArrayList<Match> matchList = wrapper.getMatchList();
                    setupList(matchList);
                }
                if(intent.getAction().equals(Constants.BROADCAST_RANK_ACTION)){
                    Log.d("rankModtager", "jeg modtager stuff");
                    Rank rank = (Rank) intent.getSerializableExtra(Constants.RANK_EXTRA);
                    Drawable tier = AssetHelper.loadRankTierImageFromAssets(MainActivity.this,rank.getTier());
                    rankedTier.setImageDrawable(tier);
                    winLossView.setText("W"+rank.getWins() +" / L"+rank.getLosses());
                }
            }
        };
    }

    private void processInGameStatus(boolean isInGame){
        if(isInGame && !this.mIsInGame){
            liveGame.setVisibility(View.VISIBLE);
            mIsInGame = isInGame;
            goToLiveGameActivity();
        } else if(!isInGame && this.mIsInGame){
            liveGame.setVisibility(View.INVISIBLE);
            mIsInGame = isInGame;
        }
    }
    
    private void setButtonOnClickListeners() {
        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPrefs.deleteSummonerName(MainActivity.this);
                finish();
            }
        });
        liveGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsInGame){
                    goToLiveGameActivity();
                } else{
                    Dialog.showAlertDialog(MainActivity.this, getString(R.string.not_in_game_title), getString(R.string.not_in_game));
                }
            }
        });
    }
    
    private void goToLiveGameActivity(){
        Intent intent = new Intent(MainActivity.this, LiveGameActivity.class);
        startActivity(intent);
    }

    private void setupUiComponents() {
        name = findViewById(R.id.nameView);
        level = findViewById(R.id.levelView);
        bestChamp = findViewById(R.id.bestChampView);
        changeName = findViewById(R.id.main_activity_change_name_button);
        champImage = findViewById(R.id.champIcon);
        liveGame = findViewById(R.id.goToLive);
        matchHistoryView = findViewById(R.id.matchHistory);
        summonerProfileImage = findViewById(R.id.summonerProfileImage);
        rankedTier = findViewById(R.id.rankTierIconView);
        winLossView = findViewById(R.id.winLossView);
        liveGame.setVisibility(View.INVISIBLE);
    }

    // Calls all relevant services to gather information about current summoner
    private void callServices() {
        mService.getBestChamp();
        mService.createMatchHistoryRequest(accountID);
        mService.createSummonerRankRequest();
    }

    private void updateUI() {
        name.setText(summonerName);
        level.setText(String.valueOf(summonerLevel));
        summonerProfileImage.setImageDrawable(AssetHelper.loadIconImageFromAssets(MainActivity.this, String.valueOf(summonerProfileId)));
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

    private ServiceConnection connection = new ServiceConnection() {
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
