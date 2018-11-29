package com.example.christianmaigaard.lolcompanion.Model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Participant implements Serializable {

    long summonerId;
    String summonerName;
    long championId;
    String championName;
    String championAlias;


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

    public String getChampionAlias(){
        return championAlias;
    }

    public void setChampionAlias(String championAlias){
        this.championAlias = championAlias;
    }

}
