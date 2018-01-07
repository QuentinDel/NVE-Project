/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import Network.Util;
import Network.Util.PlayerLite;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

/**
 * This class represents a Player
 * 
 * @author henpet-1
 * Initial work, ball handling
 * 
 * @author Quentin
 * sound and animations, ballcatching
 */
public class Player extends Node{
    private int id;
    private String playerName;
    private BoxCollisionShape boxCollisionShape;
    private GhostControl zoneBallCatch;
    private Geometry catchZone;
    private Node toRotate;
    private boolean hasBall = false;
    private AudioNode audioJump;
    private AudioNode audioShot;
    
    AnimControl ninjaControl;
    AnimChannel channelWalk;
    AnimChannel channelJump;
    AnimChannel channelAttack;
    private boolean isWalking;
    private boolean isLocal;
    
    /**
     * team 0: no team/spectator
     * team 1: red
     * team 2: blue
     */
    private int team;
    
    
    public Player(int id, String name) {
        this.id = id;
        this.playerName = name;
        this.team = Util.SPECTATOR_TEAM_ID;
    }

    public Player(int id, String name, int team) {
        this.id = id;
        this.playerName = name;
        this.team = team;
    }

    public Player(PlayerLite playerData, boolean isLocal) {
        this.id = playerData.getId();
        this.playerName = playerData.getName();
        this.team = playerData.getTeam();
        this.isLocal = isLocal;
    }
    
    public void initSpatial(AssetManager assetManager){
        Spatial ninja = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
        ninja.scale(0.03f);
        ninja.rotate(0f, 180 * FastMath.DEG_TO_RAD, 0f);
        Material mat = new Material(assetManager,  // Create new material and...
             "Common/MatDefs/Light/Lighting.j3md"); // ... specify .j3md file to use (illuminated).
        mat.setBoolean("UseMaterialColors",true);  // Set some parameters, e.g. blue.
        if(team == Util.RED_TEAM_ID){
            mat.setColor("Ambient", ColorRGBA.Red);   // ... color of this object
            mat.setColor("Diffuse", ColorRGBA.Red);   // ... color of light being reflected
        }
        else{
            mat.setColor("Ambient", ColorRGBA.Blue);   // ... color of this object
            mat.setColor("Diffuse", ColorRGBA.Blue);   // ... color of light being reflected
        }
       
        ninja.setMaterial(mat);               // Use new material on this Geometry.
        ninjaControl = ninja.getControl(AnimControl.class);
        channelWalk = ninjaControl.createChannel();
        channelWalk.setLoopMode(LoopMode.Loop);
        this.makeIdle();

        channelJump = ninjaControl.createChannel();
        channelJump.setLoopMode(LoopMode.DontLoop);
        
        channelAttack = ninjaControl.createChannel();
        channelAttack.setLoopMode(LoopMode.DontLoop);
        
        this.attachChild(ninja);
    }
    
    public void initSound(AssetManager assetManager){
         //Create the sound
        audioJump = new AudioNode(assetManager, "Sounds/twang.wav", AudioData.DataType.Buffer);
        audioJump.setPositional(true);
        audioJump.setLooping(false);
        audioJump.setVolume(1);
        //audioJump.setLocalTranslation(this);
        this.attachChild(audioJump);
        
        audioShot = new AudioNode(assetManager, "Sounds/explo.wav", AudioData.DataType.Buffer);
        audioShot.setPositional(true);
        audioShot.setLooping(false);
        audioShot.setVolume(1);
        //audioJump.setLocalTranslation(this);
        this.attachChild(audioShot);
    }
    
    public void initZoneBallCatch(AssetManager assetManager, Camera camera, AppSettings settings, float playerHeight){
        Box collisionShape = new Box(1f, 1f, 1f);
        catchZone = new Geometry("collis", collisionShape);
        Material matCube = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matCube.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);  // !
        matCube.setTransparent(true);
        matCube.setColor("Color", new ColorRGBA(0.8f, 0.8f, 0.8f, 0.1f));
        catchZone.setQueueBucket(RenderQueue.Bucket.Transparent);
        catchZone.setMaterial(matCube);
        
        boxCollisionShape = new BoxCollisionShape(new Vector3f(1f, 1f, 1f));
        zoneBallCatch = new GhostControl(boxCollisionShape);
        catchZone.addControl(zoneBallCatch);

        toRotate = new Node("toRotate");
        toRotate.setLocalTranslation(camera.getLocation());
        toRotate.attachChild(catchZone);
        zoneBallCatch.setPhysicsLocation(toRotate.getLocalTranslation());
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
            System.out.println("No better Control");
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
    
    public GhostControl getGhostControl(){
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

    public void makeJump() {
        audioJump.playInstance();

        if(!isLocal){
            channelWalk.setAnim("Jump", 0f);
            if(isWalking){
                this.makeRunning();
            }
            else{
                this.makeIdle();
            }
        }
    }
    
    public void makeAttack(){
        if(!isLocal){
       
            channelWalk.setAnim("Attack1", 0f);
            if(isWalking){
                this.makeRunning();
            }
            else{
                this.makeIdle();
            }
        }
    }
    
    public void makeShoot(){
        audioShot.playInstance();
        if(!isLocal){
            channelWalk.setAnim("Kick", 0f);
            if(isWalking){
                this.makeRunning();
            }
            else{
                this.makeIdle();
            }
        }
       
    }
    
    public void makeRunning(){
        if(!isLocal){
            isWalking = true;
            channelWalk.setAnim("Walk", 1.5f);
            channelWalk.setSpeed(2f);
        }
       
    }
    
    public void makeIdle(){
        if(!isLocal){
            isWalking = false;
            channelWalk.setAnim("Idle1", 1.5f);
            channelWalk.setSpeed(1);
        }
    }
    
    public boolean getIsWalking(){
        return isWalking;
    }
}
