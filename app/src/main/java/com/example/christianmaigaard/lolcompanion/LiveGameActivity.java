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

import com.example.christianmaigaard.lolcompanion.Model.Participant;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class LiveGameActivity extends AppCompatActivity {


    Button getParticipants;
    ImageView blueTopImg;
    ImageView blueJungleImg;
    ImageView blueMidImg;
    ImageView blueBotImg;
    ImageView blueSupportImg;

    ImageView redTopImg;
    ImageView redJungleImg;
    ImageView redMidImg;
    ImageView redBotImg;
    ImageView redSupportImg;

    private CommunicationService mService;
    private boolean mBound = false;

    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_game);

        blueTopImg = findViewById(R.id.blueTopImg);
        blueJungleImg = findViewById(R.id.blueJungleImg);
        blueMidImg = findViewById(R.id.blueMidImg);
        blueBotImg = findViewById(R.id.blueBotImg);
        blueSupportImg = findViewById(R.id.blueSupportImg);

        redTopImg = findViewById(R.id.redTopImg);
        redJungleImg = findViewById(R.id.redJungleImg);
        redMidImg = findViewById(R.id.redMidImg);
        redBotImg = findViewById(R.id.redBotImg);
        redSupportImg = findViewById(R.id.redSupportImg);



        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Constants.BROADCAST_GAME_PARTICIPANTS_ACTION)){
                    ArrayList<Participant> playerList = (ArrayList<Participant>) intent.getSerializableExtra(Constants.GAME_PARTICIPANTS_EXTRA);

                    playerList.get(0).getChampionId();

                }
            }
        };
        mFilter = new IntentFilter();
        mFilter.addAction(Constants.BROADCAST_GAME_PARTICIPANTS_ACTION);
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
