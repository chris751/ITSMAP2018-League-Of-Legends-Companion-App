package com.example.christianmaigaard.lolcompanion.Model;

import java.io.Serializable;

public class Participant implements Serializable {

    long summonerId;
    String summonerName;
    long championId;

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
}
