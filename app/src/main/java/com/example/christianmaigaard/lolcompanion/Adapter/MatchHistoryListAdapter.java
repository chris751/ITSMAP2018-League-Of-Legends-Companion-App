package com.example.christianmaigaard.lolcompanion.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.christianmaigaard.lolcompanion.Model.Match;
import com.example.christianmaigaard.lolcompanion.R;
import com.example.christianmaigaard.lolcompanion.Utilities.AssetHelper;

import java.util.ArrayList;

public class MatchHistoryListAdapter extends BaseAdapter {

    Context context;
    ArrayList<Match> matchList;
    Match match;

    public MatchHistoryListAdapter(Context context, ArrayList<Match> matchList){
        this.context = context;
        this.matchList = matchList;
    }

    @Override
    public int getCount() {
        if(matchList!=null){
            return matchList.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if(matchList!=null) {
            return matchList.get(position);
        } else {
            return null;
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView ==null){
            LayoutInflater matchInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = matchInflater.inflate(R.layout.match_history_table_layout,null);
        }

        match = matchList.get(position);
        if(match!=null){

            ImageView champIcon = (ImageView) convertView.findViewById(R.id.champIcon);
            TextView matchResult = (TextView) convertView.findViewById(R.id.isWin);
            TextView kda = (TextView) convertView.findViewById(R.id.kda);
            ConstraintLayout layout = (ConstraintLayout) convertView.findViewById(R.id.colorBackgroundConstraintLayout);

            Log.d("championAlias",match.getChampionAlias());

            champIcon.setImageDrawable(AssetHelper.loadChampImageFromAssets(context, match.getChampionAlias()));

            String matchResultText;
            if(match.getWin()){
                matchResultText = context.getString(R.string.victory);
                layout.setBackgroundColor(Color.parseColor("#00c853"));
            } else {
                matchResultText = context.getString(R.string.defeat);
                layout.setBackgroundColor(Color.parseColor("#d50000"));
            }
            matchResult.setText(matchResultText);

            int kills = match.getKills();
            int deaths = match.getDeaths();
            int assists = match.getAssists();

            kda.setText(kills + "/" + deaths + "/" + assists);
        }
        return convertView;
    }
}
