/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import Network.Util.PlayerLite;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

/**
 *
 * @author henpet-1
 */
public class Player extends Node{
    private int id;
    private String playerName;
    private BoxCollisionShape boxCollisionShape;
    private GhostControl zoneBallCatch;
    private Geometry catchZone;
    private Node toRotate;
    private boolean hasBall = false;

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
    
    public void initZoneBallCatch(AssetManager assetManager, Camera camera, AppSettings settings, float playerHeight){
        Box collisionShape = new Box(1f, 1f, 1f);
        catchZone = new Geometry("collis", collisionShape);
        Material matLine = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matLine.setColor("Color", ColorRGBA.White);
        catchZone.setMaterial(matLine);
        
        boxCollisionShape = new BoxCollisionShape(new Vector3f(1f, 1f, 1f));
        zoneBallCatch = new GhostControl(boxCollisionShape);
        zoneBallCatch.setSpatial(catchZone);
       
        //catchZone.setLocalTranslation(camera.getDirection().mult(3));
        
        //catchZone.move(this.getControl(BetterCharacterControl.class).getViewDirection());
        //System.out.println(cameraDirection);

        toRotate = new Node("toRotate");
        toRotate.setLocalTranslation(camera.getLocation());
        toRotate.attachChild(catchZone);
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

    public Vector3f getVelocity() {
        if (this.getControl(BetterCharacterControl.class) == null) {
            return new Vector3f();
        }
        return this.getControl(BetterCharacterControl.class).getWalkDirection();
    }

    public void setVelocity(Vector3f velocity) {
        this.getControl(BetterCharacterControl.class).setWalkDirection(velocity);
    }

    public void updatePlayer(PlayerLite playerData) {
        this.id = playerData.getId();
        this.playerName = playerData.getName();
        this.team = playerData.getTeam();
        setDirection(playerData.getDirection());
        setPosition(playerData.getPosition());
    }
    
    public GhostControl getGoshtControl(){
        return zoneBallCatch;
    }
            
    public Node getNodeCatchZone(){
        return toRotate;
    }

    public boolean hasBall() {
        return hasBall;
    }

    public void setHasBall(boolean value) {
        hasBall = value;
    }
}
