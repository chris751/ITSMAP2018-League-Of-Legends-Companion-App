package com.example.christianmaigaard.lolcompanion.Model;

import java.io.Serializable;

public class Match implements Serializable {

    // first iteration
    private int order;
    private long gameId;
    private long championId;

    // second iteration
    private boolean win;
    private int kills;
    private int deaths;
    private int assists;

    // third iteration
    private String championAlias;


    public Match(int order, long gameId, long championId){
        this.order = order;
        this.gameId = gameId;
        this.championId = championId;
    }

    public int getOrder() {
        return order;
    }

    public long getGameId() {
        return gameId;
    }

    public void setWin(boolean didWin){
        this.win = didWin;
    }

    public boolean getWin() {
        return win;
    }

    public void setKills(int kills){
        this.kills = kills;
    }

    public int getKills() {
        return kills;
    }

    public void setDeaths(int deaths){
        this.deaths = deaths;
    }

    public int getDeaths() {
        return deaths;
    }

    public long getChampionId() {
        return championId;
    }

    public int getAssists() {
        return assists;
    }

    public void setAssists(int assists) {
        this.assists = assists;
    }

    public String getChampionAlias() {
        return championAlias;
    }

    public void setChampionAlias(String championAlias) {
        this.championAlias = championAlias;
    }
}
