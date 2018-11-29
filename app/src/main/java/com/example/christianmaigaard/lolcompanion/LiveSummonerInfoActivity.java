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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.christianmaigaard.lolcompanion.Model.Participant;
import com.example.christianmaigaard.lolcompanion.Model.ParticipantsWrapper;
import com.example.christianmaigaard.lolcompanion.Utilities.AssetHelper;
import com.example.christianmaigaard.lolcompanion.Utilities.Constants;

public class LiveSummonerInfoActivity extends AppCompatActivity {

    Button backBtn;
    ImageView champIcon;
    TextView summonerName;
    TextView championName;
    TextView masteryInfo;

    private CommunicationService mService;
    private boolean mBound = false;

    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;

    Participant participant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_summoner_info);

        backBtn = findViewById(R.id.backBtn);
        champIcon = findViewById(R.id.champIconView);
        summonerName = findViewById(R.id.summonerNameView);
        championName = findViewById(R.id.champNameView);
        masteryInfo = findViewById(R.id.masteryInfoView);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        participant = (Participant) intent.getExtras().get(Constants.LIVE_SUMMONER_INFO_EXTRA);

        champIcon.setImageDrawable(AssetHelper.loadChampImageFromAssets(this, participant.getChampionAlias()));
        summonerName.setText(participant.getSummonerName());
        championName.setText(participant.getChampionName());

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Constants.BROADCAST_CURRENT_CHAMP_MASTERY_ACTION)){
                    masteryInfo.setText(intent.getStringExtra(Constants.CURRENT_CHAMP_MASTERY_EXTRA));
                }
            }
        };
        mFilter = new IntentFilter();
        mFilter.addAction(Constants.BROADCAST_CURRENT_CHAMP_MASTERY_ACTION);
        registerReceiver(mReceiver, mFilter);
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
            mService.createCurrentChampionMasteryRequest(participant.getSummonerId(), participant.getChampionId());

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
