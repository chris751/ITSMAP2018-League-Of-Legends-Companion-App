package com.example.christianmaigaard.lolcompanion.Model;

public class Rank {

    private int wins;
    private int losses;
    private String tier;

    public Rank(int wins, int losses, String tier){
        this.wins = wins;
        this.losses = losses;
        this.tier = tier;
    }

    public int getLosses() {
        return losses;
    }

    public int getWins() {
        return wins;
    }

    public String getTier() {
        return tier;
    }
}
