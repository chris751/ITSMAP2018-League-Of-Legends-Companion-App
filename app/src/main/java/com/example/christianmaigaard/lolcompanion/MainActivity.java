package com.example.christianmaigaard.lolcompanion;

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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import static com.example.christianmaigaard.lolcompanion.EnterSummonerNameActivity.SUMMONER_NAME;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String LOG = "MainActivity";

    private TextView name;
    private TextView profileIcon;
    private TextView level;
    private TextView bestChamp;
    private Button getInfo;
    private String summonerName;
    private ImageView champImage;


    private CommunicationService mService;
    private boolean mBound = false;

    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Code inspired heavily from "intentClassExample"
        Intent dataFromSummonerNameActivity = getIntent();
        summonerName = dataFromSummonerNameActivity.getStringExtra(SUMMONER_NAME);
        // TODO Persist summoner name
        Log.d(LOG, summonerName);

        name = findViewById(R.id.nameView);
        profileIcon = findViewById(R.id.profileIconView);
        level = findViewById(R.id.levelView);
        bestChamp = findViewById(R.id.bestChampView);
        getInfo = findViewById(R.id.getInfoButton);
        champImage = findViewById(R.id.champIcon);

        //Intent intent = new Intent()
        startService(new Intent(this, CommunicationService.class));

        getInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                    mService.createSummonerInfoRequest("Nikkelazz");
                    mService.getBestChamp();
                }
            }
        });

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
        mFilter = new IntentFilter();
        mFilter.addAction(Constants.BROADCAST_BEST_CHAMPION_ACTION);
        mFilter.addAction(Constants.BROADCAST_SUMMONER_INFO_ACTION);
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
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }

    };

    // Source: https://xjaphx.wordpress.com/2011/10/02/store-and-use-files-in-assets/
    private Drawable loadChampImageFromAssets(String champName){
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
}
