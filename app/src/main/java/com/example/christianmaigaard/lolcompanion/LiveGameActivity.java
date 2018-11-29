package com.example.christianmaigaard.lolcompanion;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.christianmaigaard.lolcompanion.Adapter.BlueTeamListAdapter;
import com.example.christianmaigaard.lolcompanion.Adapter.RedTeamListAdapter;
import com.example.christianmaigaard.lolcompanion.Model.Participant;
import com.example.christianmaigaard.lolcompanion.Model.ParticipantsWrapper;

import java.util.ArrayList;

import com.example.christianmaigaard.lolcompanion.Utilities.Constants;
import com.example.christianmaigaard.lolcompanion.Utilities.SharedPrefs;

public class LiveGameActivity extends AppCompatActivity {

    Button getParticipants;
    Button backBtn;

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

        getParticipants = findViewById(R.id.getPlayers);
        backBtn = findViewById(R.id.backBtn);

        getParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                    long summonerId = SharedPrefs.retrieveSummonorIdFromSharedPreferences(LiveGameActivity.this);
                    mService.createActiveGameRequest(summonerId);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Constants.BROADCAST_GAME_PARTICIPANTS_ACTION)){
                    ParticipantsWrapper wrappedList = (ParticipantsWrapper) intent.getExtras().get(Constants.GAME_PARTICIPANTS_EXTRA);
                    ArrayList<Participant> playerList = wrappedList.getPlayerList();
                    setupList(playerList);
                }

            }
        };
        mFilter = new IntentFilter();
        mFilter.addAction(Constants.BROADCAST_GAME_PARTICIPANTS_ACTION);
        registerReceiver(mReceiver, mFilter);
    }

    private void setupList(ArrayList<Participant> playerList){
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

        // Create layout for blue team
        blueTeamListAdapter = new BlueTeamListAdapter(LiveGameActivity.this, blueTeam);
        blueListView.setAdapter(blueTeamListAdapter);
        blueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Participant participant = blueTeam.get(position);
                goToLiveSummonerInfoActivity(participant);
            }
        });
        // Create layout for red team
        redTeamListAdapter = new RedTeamListAdapter(LiveGameActivity.this, redTeam);
        redListView.setAdapter(redTeamListAdapter);
        redListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Participant participant = redTeam.get(position);
                goToLiveSummonerInfoActivity(participant);
            }
        });
    }

    private void goToLiveSummonerInfoActivity(Participant participant){
        Intent intent = new Intent(LiveGameActivity.this, LiveSummonerInfoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.LIVE_SUMMONER_INFO_EXTRA, participant);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    /*
        System callbacks + establishing service connection
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

    private ServiceConnection connection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            CommunicationService.LocalBinder binder = (CommunicationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            
            long summonerId = SharedPrefs.retrieveSummonorIdFromSharedPreferences(LiveGameActivity.this);
            mService.createActiveGameRequest(summonerId);
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
