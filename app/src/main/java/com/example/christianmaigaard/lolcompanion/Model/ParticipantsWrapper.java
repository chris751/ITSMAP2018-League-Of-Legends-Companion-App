package com.example.christianmaigaard.lolcompanion.Model;


import java.io.Serializable;
import java.util.ArrayList;

//source: https://stackoverflow.com/questions/15731029/array-list-intent-extra-in-java
public class ParticipantsWrapper implements Serializable {

    private ArrayList<Participant> playerList;

    public ParticipantsWrapper(ArrayList<Participant> playerList){
        this.playerList = playerList;
    }

    public ArrayList<Participant> getPlayerList(){
        return playerList;
    }

}
