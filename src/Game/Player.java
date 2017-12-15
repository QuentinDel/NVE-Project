/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import com.jme3.scene.Node;

/**
 *
 * @author henpet-1
 */
public class Player extends Node{
    private int id;
    private String name;
    private int team;
    
    public Player(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Player(int id, String name, int team) {
        this.id = id;
        this.name = name;
        this.team = team;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }
}
