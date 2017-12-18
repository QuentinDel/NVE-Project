/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import Network.Util.PlayerLite;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author henpet-1
 */
public class Player extends Node{
    private int id;
    private String playerName;
    /**
     * team 0: no team/spectator
     * team 1: red
     * team 2: blue
     */
    private int team;
    
    public Player(int id, String name) {
        this.id = id;
        this.playerName = name;
        this.team = 0;
    }

    public Player(int id, String name, int team) {
        this.id = id;
        this.playerName = name;
        this.team = team;
    }

    public Player(PlayerLite playerData) {
        this.id = playerData.getId();
        this.playerName = playerData.getName();
        this.team = playerData.getTeam();
    }

    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return playerName;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public Vector3f getPosition() {
        if (this.getControl(BetterCharacterControl.class) == null) {
            return new Vector3f();
        }
        return this.getWorldTranslation();
    }

    public void setPosition(Vector3f position) {
        this.getControl(BetterCharacterControl.class).warp(position);
    }

    public Vector3f getDirection() {
        if (this.getControl(BetterCharacterControl.class) == null) {
            return new Vector3f();
        }
        return this.getControl(BetterCharacterControl.class).getViewDirection();
    }

    public void setDirection(Vector3f direction) {
        this.getControl(BetterCharacterControl.class).setViewDirection(direction);
    }

    public void updatePlayer(PlayerLite playerData) {
        this.id = playerData.getId();
        this.playerName = playerData.getName();
        this.team = playerData.getTeam();
        setDirection(playerData.getDirection());
        setPosition(playerData.getPosition());
    }
}
