package com.example.christianmaigaard.lolcompanion;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.christianmaigaard.lolcompanion.Adapter.BlueTeamListAdapter;
import com.example.christianmaigaard.lolcompanion.Adapter.RedTeamListAdapter;
import com.example.christianmaigaard.lolcompanion.Model.Participant;
import com.example.christianmaigaard.lolcompanion.Model.ParticipantsWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

import com.example.christianmaigaard.lolcompanion.Utilities.Constants;
import com.example.christianmaigaard.lolcompanion.Utilities.SharedPrefs;

public class LiveGameActivity extends AppCompatActivity {

    ArrayList<Participant> playerList;

    Button getParticipants;

    BlueTeamListAdapter blueTeamListAdapter;
    ListView blueListView;
    RedTeamListAdapter redTeamListAdapter;
    ListView redListView;

    private CommunicationService mService;
    private boolean mBound = false;

    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_game);

        blueListView = findViewById(R.id.blueTeamList);
        redListView = findViewById(R.id.redTeamList);

        getParticipants = findViewById(R.id.hejsa);

        getParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long summonerId = SharedPrefs.retrieveSummonorIdFromSharedPreferences(LiveGameActivity.this);
                mService.createActiveGameRequest();
            }
        });

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Constants.BROADCAST_GAME_PARTICIPANTS_ACTION)){
                    ParticipantsWrapper wrappedList = (ParticipantsWrapper) intent.getExtras().get(Constants.GAME_PARTICIPANTS_EXTRA);
                    playerList = wrappedList.getPlayerList();

                    //Run through playerList and save the image for current champion
                    /*for(int i = 0; i < playerList.size(); i++){
                        playerList.get(i).setChampIcon(loadChampImageFromAssets(playerList.get(i).getChampionAlias()));
                    }*/
                    setupList();
                }

            }
        };
        mFilter = new IntentFilter();
        mFilter.addAction(Constants.BROADCAST_GAME_PARTICIPANTS_ACTION);
        registerReceiver(mReceiver, mFilter);
    }

    private void setupList(){
        // Setup up two lists, one for blue team one for red team
        final ArrayList<Participant> blueTeam = new ArrayList<Participant>();
        final ArrayList<Participant> redTeam = new ArrayList<Participant>();

        // First half is blue team, second hals is red
        for(int i = 0; i < playerList.size()/2; i++){
            blueTeam.add(playerList.get(i));
        }
        for (int i = 5; i< playerList.size(); i++){
            redTeam.add(playerList.get(i));
        }


        blueTeamListAdapter = new BlueTeamListAdapter(LiveGameActivity.this, blueTeam);
        blueListView.setAdapter(blueTeamListAdapter);
        blueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("PlayerList", "SUCK DICK");
                Participant participant = blueTeam.get(position);
                Intent intent = new Intent(LiveGameActivity.this, LiveSummonerInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.LIVE_SUMMONER_INFO, participant);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        redTeamListAdapter = new RedTeamListAdapter(LiveGameActivity.this, redTeam);
        redListView.setAdapter(redTeamListAdapter);
        redListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("PlayerList", "Suck red dick");
                Participant participant = redTeam.get(position);
                Intent intent = new Intent(LiveGameActivity.this, LiveSummonerInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.LIVE_SUMMONER_INFO, participant);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void goToLiveSummonerInfoActivity(){

    }


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

    private ServiceConnection connection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            CommunicationService.LocalBinder binder = (CommunicationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }

    };

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
