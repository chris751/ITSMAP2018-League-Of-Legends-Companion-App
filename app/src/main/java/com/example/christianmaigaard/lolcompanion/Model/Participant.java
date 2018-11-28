package com.example.christianmaigaard.lolcompanion.Model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Participant implements Serializable {

    long summonerId;
    String summonerName;
    long championId;
    String championName;
    Drawable champIcon;


    public Participant(long summonerId, String summonerName, long championId){
        this.summonerId = summonerId;
        this.summonerName = summonerName;
        this.championId = championId;
    }

    public long getSummonerId(){
        return summonerId;
    }

    public String getSummonerName(){
        return summonerName;
    }

    public long getChampionId(){
        return championId;
    }

    public String getChampionName(){
        return championName;
    }

    public void setChampionName(String champName){
        this.championName = champName;
    }

    public Drawable getChampIcon(){
        return champIcon;
    }

    public void setChampIcon(Drawable champIcon){
        this.champIcon = champIcon;
    }

}
