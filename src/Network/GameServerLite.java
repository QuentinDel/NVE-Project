/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network;

/**
    *
    * @author Quentin
    */
public class GameServerLite{

    private String address;
    private String idMap;
    private int nbPlayers;
    private int status;
    private int scoreBlue;
    private int scoreRed;

    public GameServerLite(){}

    public GameServerLite(String address, String idMap, int nbPlayers, int status, int scoreBlue, int scoreRed){
        this.address = address;
        this.idMap = idMap;
        this.nbPlayers = nbPlayers;
        this.status = status;
        this.scoreBlue = scoreBlue;
        this.scoreRed = scoreRed;
    }

    public String getAddress(){ return address; }

    public String getIdMap(){ return idMap; }

    public int getNbPlayers(){ return nbPlayers; }

    public int getStatus(){ return status; }

    public int getScoreBlue(){ return scoreBlue; }

    public int getScoreRed(){ return scoreRed; }

}
