package com.example.christianmaigaard.lolcompanion.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.christianmaigaard.lolcompanion.Utilities.AssetHelper;
import com.example.christianmaigaard.lolcompanion.Model.Participant;
import com.example.christianmaigaard.lolcompanion.R;

import java.util.ArrayList;

//source: lecture slides L4
public class BlueTeamListAdapter extends BaseAdapter {

    Context context;
    ArrayList<Participant> playerList;
    Participant participant;

    public BlueTeamListAdapter(Context context, ArrayList<Participant> playerList){
        this.context = context;
        this.playerList = playerList;
    }

    @Override
    public int getCount() {
        if(playerList!=null){
            return playerList.size();
        } else{
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if(playerList!=null){
            return playerList.get(position);
        } else{
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
            LayoutInflater participantInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = participantInflater.inflate(R.layout.blue_team_table_layout,null);
        }

        participant = playerList.get(position);
        if(participant!=null){
            TextView champName = (TextView) convertView.findViewById(R.id.champName);
            champName.setText(participant.getChampionName());

            TextView summonerName = (TextView) convertView.findViewById(R.id.summonerName);
            summonerName.setText(participant.getSummonerName());


            ImageView champIcon = (ImageView) convertView.findViewById(R.id.champIcon);
            champIcon.setImageDrawable(AssetHelper.loadChampImageFromAssets(context,participant.getChampionAlias()));
        }
        return convertView;
    }
}
