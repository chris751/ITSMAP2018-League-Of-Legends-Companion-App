package com.example.christianmaigaard.lolcompanion.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class MatchWrapper implements Serializable {
    private ArrayList<Match> matchList;

    public MatchWrapper(ArrayList<Match> matchList){
        this.matchList = matchList;
    }

    public ArrayList<Match> getPlayerList(){
        return matchList;
    }

}
