/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import Network.Util.PlayerLite;
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
    
    private Geometry ball_geo;
    private ArrayList<Player> playerStore;
    private int userID;
    
    protected final float playerRadius = 1.5f;
    protected final float playerHeight = 6f;
    protected final float playerMass = 1f;
    protected final float playerJumpSpeed = 22;
    protected final float playerGravity = 50;
    
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
        playerStore = new ArrayList(); //Do i even need this?
        
        /** Set up Physics */
        sapp.getStateManager().attach(bulletAppState);
        bulletAppState.setDebugEnabled(true);
        
        // We re-use the flyby camera for rotation, while positioning is handled by physics
        sapp.getViewPort().setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        sapp.getFlyByCamera().setMoveSpeed(100);
        //setUpKeys();
        setUpLight();
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
    
    // Loads the level, creates players and adds physics to them.
    public void initLevel(String level_id) {
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
    
    //Adds a local player to the game
    //A local player does not have a geometry
    //This function also returns the player object
    public Player addLocalPlayer(PlayerLite p) {
        // Setup the player node
        Player playerNode = new Player(p);
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
        playerStore.add(playerNode);
        
        return playerNode;
    }
    
    public Player addPlayer(PlayerLite p) {
        // Setup the player node
        Player playerNode = new Player(p);
        
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
        playerControl.setViewDirection(p.getDirection());
        bulletAppState.getPhysicsSpace().add(playerControl);
        sapp.getRootNode().attachChild(playerNode);
        
        return playerNode;
    }
    
    public void addBall() {
        Sphere sphere = new Sphere(32, 32, 2f, true, false);
        sphere.setTextureMode(TextureMode.Projected);
        
        //Setup the material for the ball
        Material stone_mat = new Material(sapp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
        key.setGenerateMips(true);
        Texture tex = sapp.getAssetManager().loadTexture(key);
        stone_mat.setTexture("ColorMap", tex);
        
        //Setup the geometry for the ball
        ball_geo = new Geometry("cannon ball", sphere);
        ball_geo.setMaterial(stone_mat);
        sapp.getRootNode().attachChild(ball_geo);
        ball_geo.move(new Vector3f(-5, 6f, -5));
        
        CollisionShape ball_shape = new SphereCollisionShape(sphere.getRadius());
        RigidBodyControl ball_phy = new RigidBodyControl(ball_shape, 10f);
        ball_geo.addControl(ball_phy);
        bulletAppState.getPhysicsSpace().add(ball_phy);
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
    }
}
