/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import Network.Util.PlayerLite;
import Network.Util.PlayerPhysics;
import Playboard.GrassPlayground;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import java.util.ArrayList;

/**
 *
 * @author Rickard
 */
public class Game extends BaseAppState {
    
    private SimpleApplication sapp;
    private boolean needCleaning = false;
    
    private Spatial sceneModel;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private BetterCharacterControl playerControl;
    
   
    private ArrayList<Player> playerStore;
    private int userID;
    private Ball ball;
            
    protected final float playerRadius = 1.5f;
    protected final float playerHeight = 6f;
    protected final float playerMass = 1f;
    protected final float playerJumpSpeed = 22;
    protected final float playerGravity = 50;
    protected final float cameraHeight = playerHeight*0.8f;
    private String level_id = "grassPlayGround"; //Default level
    
    @Override
    protected void initialize(Application app) {
        System.out.println("Game: initialize");
        sapp = (SimpleApplication) app;
        
        bulletAppState = new BulletAppState();
    }
   
    @Override
    protected void cleanup(Application app) {
        System.out.println("Game: cleanup");
    }
    
    @Override
    public void onEnable() {
        System.out.println("Game: onEnable");
        if (needCleaning) {
            System.out.println("Game: Cleaning up");
            sapp.getRootNode().detachAllChildren();
            needCleaning = false;
        }
        playerStore = new ArrayList();
        
        /** Set up Physics */
        sapp.getStateManager().attach(bulletAppState);
        bulletAppState.setDebugEnabled(true);
        
        // We re-use the flyby camera for rotation, while positioning is handled by physics
        sapp.getViewPort().setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        sapp.getFlyByCamera().setMoveSpeed(100);
        //setUpKeys();
        setUpLight();
        initLevel();
    }

    private void setUpLight() {
        // We add light so we see the scene
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        sapp.getRootNode().addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        sapp.getRootNode().addLight(dl);
    }
    
    public void setLevel(String level_id) {
        this.level_id = level_id;
    }
    
    // Loads the level, creates players and adds physics to them.
    public void initLevel() {
        //sapp.getAssetManager().registerLocator(level_id+".zip", ZipLocator.class);
        //sceneModel = sapp.getAssetManager().loadModel("main.scene");
        //sceneModel.setLocalScale(2f);
        GrassPlayground playground = new GrassPlayground(sapp.getAssetManager());
        sceneModel = playground.getNode();
        
        // We set up collision detection for the level by creating a
        // compound collision shape and a static RigidBodyControl with mass zero.
        CollisionShape sceneShape =
                CollisionShapeFactory.createMeshShape(playground.getPlayGroundWithoutLine());
        landscape = new RigidBodyControl(sceneShape, 0);
        sceneModel.addControl(landscape);
        
        sapp.getRootNode().attachChild(sceneModel);
        bulletAppState.getPhysicsSpace().add(landscape);
    }
    
    //Adds a local player to the game and returns it
    //A local player does not have a geometry
    public Player addLocalPlayer(PlayerLite p) {
        // Setup the player node
        Player playerNode = new Player(p);
        playerNode.initZoneBallCatch(sapp.getAssetManager(), sapp.getCamera(), sapp.getContext().getSettings(), playerHeight);
        this.userID = p.getId();
        
        // Setup the geometry for the player
        playerNode.move(new Vector3f(0, 3.5f, 0));
        
        // Setup the control for the player
        playerControl = new BetterCharacterControl(playerRadius, playerHeight, playerMass);
        playerNode.addControl(playerControl);
        playerControl.setJumpForce(new Vector3f(0, playerJumpSpeed, 0));
        playerControl.setGravity(new Vector3f(0, playerGravity, 0));
        playerControl.warp(p.getPosition());
        playerControl.setViewDirection(p.getDirection());
        bulletAppState.getPhysicsSpace().add(playerControl);
        sapp.getRootNode().attachChild(playerNode);
        sapp.getRootNode().attachChild(playerNode.getNodeCatchZone());
        playerStore.add(playerNode);
        System.out.println("addLocalPlayer");
        return playerNode;
        
    }
    
    //Adds a non-local player to the game and returns it
    //A non-local player has a geometry
    public Player addPlayer(PlayerLite p) {
        // Setup the player node
        Player playerNode = new Player(p);
        //playerNode.initZoneBallCatch(sapp.getAssetManager());
        
        // Setup the geometry for the player
        Spatial teapot = sapp.getAssetManager().loadModel("Models/Teapot/Teapot.obj");
        Material mat_default = new Material(
            sapp.getAssetManager(), "Common/MatDefs/Misc/ShowNormals.j3md");
        teapot.setMaterial(mat_default);
        playerNode.attachChild(teapot);
        
        // Setup the control for the player
        BetterCharacterControl playerControl = new BetterCharacterControl(playerRadius, playerHeight, playerMass);
        playerNode.addControl(playerControl);
        playerControl.setJumpForce(new Vector3f(0, playerJumpSpeed, 0));
        playerControl.setGravity(new Vector3f(0, playerGravity, 0));
        playerControl.warp(p.getPosition());
        System.out.println("playerposition: "+p.getPosition());
        //playerControl.setViewDirection(p.getDirection());
        playerControl.setViewDirection(new Vector3f(1,1,1));
        bulletAppState.getPhysicsSpace().add(playerControl);
        sapp.getRootNode().attachChild(playerNode);
        playerStore.add(playerNode);
        
        return playerNode;
    }
    
    public void addBall() {
        ball = new Ball(sapp, bulletAppState);
        sapp.getRootNode().attachChild(ball.getGeometry());
    }

    public Ball getBall() {
        return ball;
    }
    
    //Returns a Player with id "playerID" from the playerStore
    //Returns null if not found
    public Player getPlayer(int playerID) {
        for (Player p: playerStore) {
            if (p.getId() == playerID) {
                return p;
            }
        }
        return null;
    }

    //Makes the player with the given id jump
    public void makeJump(int playerID) {
        Player p = getPlayer(playerID);
        p.getControl(BetterCharacterControl.class).jump();
    }
    
    public void updatePlayerPhysics(ArrayList<PlayerPhysics> physics) {
        for (PlayerPhysics pp: physics) {
            Player p = getPlayer(pp.getId());
            if (p != null) {
                p.setDirection(pp.getDirection());
                p.setVelocity(pp.getVelocity());
                if (p.getId() == this.userID) {
                    sapp.getCamera().setLocation(p.getWorldTranslation().add(new Vector3f(0, cameraHeight, 0)));
                }
            }
        }
    }
    
    /**
     * This is the main event loop--walking happens here.
     */
    @Override
    public void update(float tpf) {
        
    }
    
    @Override
    public void onDisable() {
        System.out.println("Game: onDisable");
        sapp.getStateManager().detach(bulletAppState); //will this break anything?
        sapp.getRootNode().detachAllChildren();
    }
}
