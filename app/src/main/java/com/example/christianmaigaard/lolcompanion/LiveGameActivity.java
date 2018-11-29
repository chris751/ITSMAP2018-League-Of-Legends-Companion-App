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

import com.example.christianmaigaard.lolcompanion.Utilities.Constants;

public class LiveGameActivity extends AppCompatActivity {


    Button getParticipants;


    private CommunicationService mService;
    private boolean mBound = false;

    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_game);

        getParticipants = findViewById(R.id.getParticipants);

        getParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                    mService.createActiveGameRequest(226141);
                }
            }
        });

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
        mFilter = new IntentFilter();
        mFilter.addAction(Constants.BROADCAST_BEST_CHAMPION_ACTION);
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
}
