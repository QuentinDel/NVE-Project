/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

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
public class Game extends BaseAppState implements ActionListener {
    
    private SimpleApplication sapp;
    private boolean needCleaning = false;
    
    private Spatial sceneModel;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private Player playerNode;
    private BetterCharacterControl playerControl;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;
    
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();
    
    private Geometry ball_geo;
    private ArrayList<Player> playerStore;
    private int userID;
    
    private final float playerRadius = 1.5f;
    private final float playerHeight = 6f;
    private final float playerMass = 1f;
    private final float playerJumpSpeed = 22;
    private final float playerGravity = 50;
    private final float playerMoveSpeed = 20;
    
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
        setUpKeys();
        setUpLight();

        // Load and add physics to the level and players
        initLevel("town");
        addLocalPlayer(0, "Bob");
        addPlayer(1, "John");
        addBall();
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

    /** We over-write some navigational key mappings here, so we can
     * add physics-controlled walking and jumping: */
    private void setUpKeys() {
        sapp.getInputManager().addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        sapp.getInputManager().addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        sapp.getInputManager().addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        sapp.getInputManager().addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        sapp.getInputManager().addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        sapp.getInputManager().addMapping("test", new KeyTrigger(KeyInput.KEY_K));
        sapp.getInputManager().addListener(this, "Left");
        sapp.getInputManager().addListener(this, "Right");
        sapp.getInputManager().addListener(this, "Up");
        sapp.getInputManager().addListener(this, "Down");
        sapp.getInputManager().addListener(this, "Jump");
        sapp.getInputManager().addListener(this, "test");
    }
    
    // Loads the level, creates players and adds physics to them.
    private void initLevel(String level_id) {
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
    
    public void addLocalPlayer(int id,  String name) {
        // Setup the player node
        playerNode = new Player(id, name);
        this.userID = id;
        
        // Setup the geometry for the player
        playerNode.move(new Vector3f(0, 3.5f, 0));
        
        // Setup the control for the player
        //player = new CharacterControl(playerShape, stepSize);
        playerControl = new BetterCharacterControl(playerRadius, playerHeight, playerMass);
        playerNode.addControl(playerControl);
        playerControl.setJumpForce(new Vector3f(0, playerJumpSpeed, 0));
        playerControl.setGravity(new Vector3f(0, playerGravity, 0));
        playerControl.warp(new Vector3f(0, 10, 0));
        bulletAppState.getPhysicsSpace().add(playerControl);
        sapp.getRootNode().attachChild(playerNode);
    }
    
    public void addPlayer(int id,  String name) {
        // Setup the player node
        Player playerNode = new Player(id, name);
        
        // Setup the geometry for the player
        Spatial teapot = sapp.getAssetManager().loadModel("Models/Teapot/Teapot.obj");
        Material mat_default = new Material(
            sapp.getAssetManager(), "Common/MatDefs/Misc/ShowNormals.j3md");
        teapot.setMaterial(mat_default);
        playerNode.attachChild(teapot);
        playerNode.move(new Vector3f(20, 3.5f, 0));
        
        // Setup the control for the player
        BetterCharacterControl playerControl = new BetterCharacterControl(playerRadius, playerHeight, playerMass);
        playerNode.addControl(playerControl);
        playerControl.setJumpForce(new Vector3f(0, playerJumpSpeed, 0));
        playerControl.setGravity(new Vector3f(0, playerGravity, 0));
        playerControl.warp(new Vector3f(0, 10, 0));
        bulletAppState.getPhysicsSpace().add(playerControl);
        sapp.getRootNode().attachChild(playerNode);
    }
    
    private void addBall() {
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

    /** These are our custom actions triggered by key presses.
     * We do not walk yet, we just keep track of the direction the user pressed. */
    public void onAction(String binding, boolean isPressed, float tpf) {
        if (binding.equals("Left")) {
          left = isPressed;
        } else if (binding.equals("Right")) {
          right= isPressed;
        } else if (binding.equals("Up")) {
          up = isPressed;
        } else if (binding.equals("Down")) {
          down = isPressed;
        } else if (binding.equals("Jump")) {
          if (isPressed) { playerControl.jump(); }
        } else if (binding.equals("test")) {
            ball_geo.getControl(RigidBodyControl.class).setPhysicsLocation(new Vector3f(-10, 5f, -10));
            ball_geo.getControl(RigidBodyControl.class).setLinearVelocity(new Vector3f(30, 0, 30));
        }
    }

    /**
     * This is the main event loop--walking happens here.
     * We check in which direction the player is walking by interpreting
     * the camera direction forward (camDir) and to the side (camLeft).
     * The setWalkDirection() command is what lets a physics-controlled player walk.
     * We also make sure here that the camera moves with player.
     */
    @Override
    public void update(float tpf) {
        camDir = sapp.getCamera().getDirection().clone();
        camLeft = sapp.getCamera().getLeft().clone();
        camDir.y = 0;
        camLeft.y = 0;
        camDir = camDir.normalizeLocal();
        camLeft = camLeft.normalizeLocal();
        walkDirection.set(0, 0, 0);
        
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        walkDirection = walkDirection.multLocal(playerMoveSpeed);
        playerControl.setWalkDirection(walkDirection);
        sapp.getCamera().setLocation(playerNode.getWorldTranslation().add(new Vector3f(0, playerHeight*0.8f, 0)));
    }
    
    @Override
    public void onDisable() {
        System.out.println("Game: onDisable");
        sapp.getStateManager().detach(bulletAppState); //will this break anything?
        //Remove player controls
    }
}
