package com.example.christianmaigaard.lolcompanion;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.christianmaigaard.lolcompanion.Model.Participant;
import com.example.christianmaigaard.lolcompanion.Utilities.AssetHelper;
import com.example.christianmaigaard.lolcompanion.Utilities.Constants;

public class LiveSummonerInfoActivity extends AppCompatActivity {

    Button backBtn;
    ImageView champIcon;
    TextView summonerName;
    TextView championName;
    TextView masteryInfo;

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
        Participant participant = (Participant) intent.getExtras().get(Constants.LIVE_SUMMONER_INFO);

        champIcon.setImageDrawable(AssetHelper.loadChampImageFromAssets(this, participant.getChampionAlias()));
        summonerName.setText(participant.getSummonerName());
        championName.setText(participant.getChampionName());


        // TODO: lav et get på personens champion mastery, og find den der matcher med nuværende champ ID.. og fortæl hans mastery score,, måske ikke direkte men, at han er så og så god eller dårlig
    }
}
