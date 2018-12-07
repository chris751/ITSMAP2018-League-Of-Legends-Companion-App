package com.example.christianmaigaard.lolcompanion;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.christianmaigaard.lolcompanion.Model.MasteryLevel;
import com.example.christianmaigaard.lolcompanion.Model.Participant;
import com.example.christianmaigaard.lolcompanion.Model.ParticipantsWrapper;
import com.example.christianmaigaard.lolcompanion.Utilities.AssetHelper;
import com.example.christianmaigaard.lolcompanion.Utilities.Constants;

import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.CHAMPMION_POINTS;

public class LiveSummonerInfoActivity extends AppCompatActivity {

    Button backBtn;
    ImageView champIcon;
    TextView summonerName;
    TextView championName;
    TextView masteryInfo;
    ProgressBar summonerSkillBarometer;
    ProgressBar summonerInfoSpinner;

    private CommunicationService mService;
    private boolean mBound = false;

    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;

    Participant participant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_summoner_info);

        setupUI();
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        summonerInfoSpinner.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        participant = (Participant) intent.getExtras().get(Constants.LIVE_SUMMONER_INFO_EXTRA);

        champIcon.setImageDrawable(AssetHelper.loadChampImageFromAssets(this, participant.getChampionAlias()));
        summonerName.setText(participant.getSummonerName());
        championName.setText(participant.getChampionName());

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Constants.BROADCAST_CURRENT_CHAMP_MASTERY_ACTION)) {
                    summonerInfoSpinner.setVisibility(View.INVISIBLE);
                    int championPoints = intent.getIntExtra(CHAMPMION_POINTS, 0);
                    MasteryLevel masteryLevel = determineMasterylevel(championPoints);
                    masteryInfo.setText(masteryLevel.getSummonerSkillLevel());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        summonerSkillBarometer.setProgress(masteryLevel.getSkillLevel(),true);
                    } else {
                        summonerSkillBarometer.setProgress(masteryLevel.getSkillLevel());
                    }
                }
            }
        };
        mFilter = new IntentFilter();
        mFilter.addAction(Constants.BROADCAST_CURRENT_CHAMP_MASTERY_ACTION);
        registerReceiver(mReceiver, mFilter);
    }

    private MasteryLevel determineMasterylevel (int championPoints) {
        MasteryLevel masteryLevel = new MasteryLevel();
        String summonerSkillInfo = "";
        int skillLevel = 1;
        if(championPoints == Constants.CHAMPION_LEVEL_0){
            summonerSkillInfo = getString(R.string.champion_level_0);
        } else if(Constants.CHAMPION_LEVEL_0 < championPoints && championPoints < Constants.CHAMPION_LEVEL_1){
            summonerSkillInfo = getString(R.string.champion_level_1);
            skillLevel = 11;
        } else if(Constants.CHAMPION_LEVEL_1 < championPoints && championPoints < Constants.CHAMPION_LEVEL_2){
            summonerSkillInfo = getString(R.string.champion_level_2);
            skillLevel = 22;
        } else if(Constants.CHAMPION_LEVEL_2 < championPoints && championPoints < Constants.CHAMPION_LEVEL_3){
            summonerSkillInfo = getString(R.string.champion_level_3);
            skillLevel = 33;
        } else if(Constants.CHAMPION_LEVEL_3 < championPoints && championPoints < Constants.CHAMPION_LEVEL_4){
            summonerSkillInfo = getString(R.string.champion_level_4);
            skillLevel = 44;
        } else if(Constants.CHAMPION_LEVEL_4 < championPoints && championPoints < Constants.CHAMPION_LEVEL_5){
            summonerSkillInfo = getString(R.string.champion_level_5);
            skillLevel = 55;
        } else if(Constants.CHAMPION_LEVEL_5 < championPoints && championPoints < Constants.CHAMPION_LEVEL_6){
            summonerSkillInfo = getString(R.string.champion_level_6);
            skillLevel = 66;
        } else if(Constants.CHAMPION_LEVEL_6 < championPoints && championPoints < Constants.CHAMPION_LEVEL_7){
            summonerSkillInfo = getString(R.string.champion_level_7);
            skillLevel = 77;
        } else if(Constants.CHAMPION_LEVEL_7 < championPoints && championPoints < Constants.CHAMPION_LEVEL_CRAZY){
            summonerSkillInfo = getString(R.string.champion_level_8);
            skillLevel = 88;
        } else if(championPoints > Constants.CHAMPION_LEVEL_CRAZY){
            summonerSkillInfo = getString(R.string.champion_level_9);
            skillLevel = 99;
        }
        masteryLevel.setSkillLevel(skillLevel);
        masteryLevel.setSummonerSkillLevel(summonerSkillInfo);
        return masteryLevel;
    }

    private void setupUI() {
        backBtn = findViewById(R.id.backBtn);
        champIcon = findViewById(R.id.champIconView);
        summonerName = findViewById(R.id.summonerNameView);
        championName = findViewById(R.id.champNameView);
        masteryInfo = findViewById(R.id.masteryInfoView);
        summonerSkillBarometer = findViewById(R.id.summonerSkillBarometer);
        summonerInfoSpinner = findViewById(R.id.summonerInfoSpinner);
        // https://stackoverflow.com/questions/13509989/how-to-set-the-android-progressbars-height/20360132
        summonerSkillBarometer.setScaleY(5f);
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
