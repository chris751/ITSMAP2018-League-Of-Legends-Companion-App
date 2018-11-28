package com.example.christianmaigaard.lolcompanion;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EnterSummonerNameActivity extends AppCompatActivity {


    private static final String LOG = "SummonorNameActivity";
    public static final String SUMMONER_NAME = "summonerName";

    Button findSummonerName;
    EditText enterSummonerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_summoner_name);
        // Button
        findSummonerName = findViewById(R.id.enter_summoner_name_find_button);
        findSummonerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findNameButtonClicked();
            }
        });

        // EditText
        enterSummonerName = findViewById(R.id.enter_summoner_name_edit_text);



    }

    private void findNameButtonClicked() {
        String summonerName = enterSummonerName.getText().toString();
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(SUMMONER_NAME, summonerName);
        startActivity(i);
    }

}
