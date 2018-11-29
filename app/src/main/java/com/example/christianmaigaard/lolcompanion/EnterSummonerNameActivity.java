package com.example.christianmaigaard.lolcompanion;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.christianmaigaard.lolcompanion.Utilities.Constants;
import com.example.christianmaigaard.lolcompanion.Utilities.Dialog;
import com.example.christianmaigaard.lolcompanion.Utilities.SharedPrefs;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static com.example.christianmaigaard.lolcompanion.Utilities.Constants.SUMMONER_LEVEL;

public class EnterSummonerNameActivity extends AppCompatActivity {


    private static final String LOG = "SummonorNameActivity";
    public static final String SUMMONER_NAME = "summonerName";

    Button findSummonerName;
    EditText enterSummonerName;

    private CommunicationService mService;
    private boolean mBound = false;

    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;
    private boolean userClickedBack;

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

        // Service
        startService(new Intent(this, CommunicationService.class));
        startBroadCastReceiver();

        mFilter = new IntentFilter();
        mFilter.addAction(Constants.BROADCAST_BEST_CHAMPION_ACTION);
        mFilter.addAction(Constants.BROADCAST_SUMMONER_INFO_ACTION);
        registerReceiver(mReceiver, mFilter);
    }

    private boolean summonerNameStored() {

        String summonerName = SharedPrefs.retrieveSummonorNameFromSharedPreferences(this);
        if(summonerName != null && !summonerName.isEmpty()) {
            return true;
        }
        return false;
    }


    private void startBroadCastReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.getAction().equals(Constants.BROADCAST_SUMMONER_INFO_ACTION)){
                    long summonerLevel = intent.getLongExtra(Constants.SUMMONER_INFO_LEVEL_EXTRA,0);
                    String name = intent.getStringExtra(Constants.SUMMONER_NAME);
                    String error = intent.getStringExtra(Constants.ERROR);
                    if(error != null && !error.isEmpty()) {
                        switch (error) {
                            case Constants.VOLLEY_AUTH_ERROR:
                                showInvalidAPIkeyDialog();
                                break;
                            case Constants.VOLLEY_CONNECTION_ERROR:
                                showFailedConnectionDialog();
                                break;
                            default:
                                showInvalidSummonerNameErrorDialog();
                                break;
                        }
                        return;
                    }
                    Intent i = new Intent(EnterSummonerNameActivity.this, MainActivity.class);
                    i.putExtra(SUMMONER_NAME, name);
                    i.putExtra(SUMMONER_LEVEL, summonerLevel);
                    startActivity(i);
                }
            }
        };
    }

    private void showInvalidSummonerNameErrorDialog() {
        Context c = EnterSummonerNameActivity.this;
        String title = getString(R.string.invalid_summoner_name);
        String message = getString(R.string.check_summoner_name_for_correctness);
        Dialog.showAlertDialog(c, title, message);
    }

    private void showInvalidAPIkeyDialog() {
        Context c = EnterSummonerNameActivity.this;
        String title = getString(R.string.invalid_api_key);
        String message = getString(R.string.please_contact_developers);
        Dialog.showAlertDialog(c, title, message);
    }

    private void showFailedConnectionDialog() {
        Context c = EnterSummonerNameActivity.this;
        String title = getString(R.string.connection_failed);
        String message = getString(R.string.please_check_connection);
        Dialog.showAlertDialog(c, title, message);
    }

    private void findNameButtonClicked() {

        String summonerName = enterSummonerName.getText().toString();
        if(mBound){
            mService.createSummonerInfoRequest(summonerName);
        }
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

            // if we already stored a summoner
            if (summonerNameStored() && !userClickedBack){
                if(mBound){
                    mService.createSummonerInfoRequest(SharedPrefs.retrieveSummonorNameFromSharedPreferences(EnterSummonerNameActivity.this));
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }

    };

}
