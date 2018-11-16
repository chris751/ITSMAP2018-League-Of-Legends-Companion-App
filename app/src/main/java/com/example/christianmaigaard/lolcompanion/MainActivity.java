package com.example.christianmaigaard.lolcompanion;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView name;
    private TextView profileIcon;
    private TextView level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.nameView);
        profileIcon = findViewById(R.id.profileIconView);
        level = findViewById(R.id.levelView);

        //Intent intent = new Intent()
        startService(new Intent(this, CommunicationService.class));
    }
}
