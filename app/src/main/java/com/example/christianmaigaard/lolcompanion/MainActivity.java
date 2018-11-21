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
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView name;
    private TextView profileIcon;
    private TextView level;
    private TextView bestChamp;
    private Button getInfo;

    private CommunicationService mService;
    private boolean mBound = false;

    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.nameView);
        profileIcon = findViewById(R.id.profileIconView);
        level = findViewById(R.id.levelView);
        bestChamp = findViewById(R.id.bestChampView);
        getInfo = findViewById(R.id.getInfoButton);

        //Intent intent = new Intent()
        startService(new Intent(this, CommunicationService.class));

        getInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound){
                    mService.getBestChamp();
                }
            }
        });

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("broadcastReceiver", intent.getStringExtra(Constants.BEST_CHAMPION_EXTRA));
                bestChamp.setText(intent.getStringExtra(Constants.BEST_CHAMPION_EXTRA));

                // TODO: gør noget mere her
            }
        };
        mFilter = new IntentFilter();
        mFilter.addAction(Constants.BROADCAST_BEST_CHAMPION);
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
}
