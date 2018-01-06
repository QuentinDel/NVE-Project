/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;
//D:\Program Files\MATLAB\R2016a\toolbox\sl3d\vrealm\program\sounds
import Network.GameApplication;
import Network.GameClient.GameClient;
import Network.Util;
import Network.Util.BallPhysics;
import Network.Util.PlayerLite;
import Network.Util.PlayerPhysics;
import Playboard.GrassPlayground;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
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
import java.util.HashMap;

/**
 *
 * @author Rickard
 */
public class Game extends BaseAppState {
    
    private GameApplication sapp;
    private boolean needCleaning = false;
    
    private Spatial sceneModel;
    private BulletAppState bulletAppState;
    private BetterCharacterControl playerControl;
    
   
    private HashMap<Integer, Player> playerStore;
    private int userID;
    private Ball ball;
            
    protected final float playerRadius = 1.5f;
    protected final float playerHeight = 6f;
    protected final float playerMass = 2f;
    protected final float playerJumpSpeed = 30;
    protected final float playerGravity = 80;
    protected final float cameraHeight = playerHeight*0.8f;
    private String level_id = "grassPlayGround"; //Default level
    public static final int MAX_PLAYER_COUNT = 8;
    
    private GrassPlayground playground;
    
    private int blueScore = 0;
    private int redScore = 0;
    
    @Override
    protected void initialize(Application app) {
        System.out.println("Game: initialize");
        sapp = (GameApplication) app;
        
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
        playerStore = new HashMap();
        
        /** Set up Physics */
        sapp.getStateManager().attach(bulletAppState);
        bulletAppState.setDebugEnabled(true);
        
        // We re-use the flyby camera for rotation, while positioning is handled by physics
        sapp.getViewPort().setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        sapp.getFlyByCamera().setMoveSpeed(0);
        sapp.getCamera().setLocation(new Vector3f(-100, 30, -100));
        sapp.getCamera().lookAt(new Vector3f(180, -45, 0), sapp.getCamera().getUp());
        setUpLight();
        initLevel();
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
    
    public void setLevel(String level_id) {
        this.level_id = level_id;
    }
    
    // Loads the level, creates players and adds physics to them.
    public void initLevel() {
        //sapp.getAssetManager().registerLocator(level_id+".zip", ZipLocator.class);
        //sceneModel = sapp.getAssetManager().loadModel("main.scene");
        //sceneModel.setLocalScale(2f);
        playground = new GrassPlayground(sapp, this);
        sceneModel = playground.getNode();
        
        // We set up collision detection for the level by creating a
        // compound collision shape and a static RigidBodyControl with mass zero.
        // First setup the floor of the level
        CollisionShape floorShape =
                CollisionShapeFactory.createMeshShape(playground.getPlayGroundFloor());
        RigidBodyControl landscape = new RigidBodyControl(floorShape, 0);
        landscape.setRestitution(0.5f);
        sceneModel.addControl(landscape);
        
        // Setup collision with walls/goals/etc
        CollisionShape sceneShape =
                CollisionShapeFactory.createMeshShape(playground.getPlayGroundWithoutLine());
        RigidBodyControl scene = new RigidBodyControl(sceneShape, 0);
        scene.setRestitution(0.5f);
        scene.setFriction(0.0f);
        sceneModel.addControl(scene);
        
        //Get collisionlisteners for the scorezones
        ScoreControl blue = playground.getBlueScoreControl();
        ScoreControl red = playground.getRedScoreControl();
        
        //Setup collisiongroups
        landscape.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
        scene.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
        blue.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        red.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        
        landscape.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
        scene.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
        blue.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_02);
        red.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_02);
                
        sapp.getRootNode().attachChild(sceneModel);
        bulletAppState.getPhysicsSpace().addCollisionListener(blue);
        bulletAppState.getPhysicsSpace().addCollisionListener(red);
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(scene);
        bulletAppState.getPhysicsSpace().add(blue);
        bulletAppState.getPhysicsSpace().add(red);
    }
    
    //Adds a local player to the game and returns it
    //A local player does not have a geometry
    public Player addLocalPlayer(PlayerLite p) {
        // Setup the player node
        Player playerNode = new Player(p, true);
        playerNode.initSound(sapp.getAssetManager());
        playerNode.initZoneBallCatch(sapp.getAssetManager(), sapp.getCamera(), sapp.getContext().getSettings(), playerHeight);
        //playerNode.initSpatial(sapp.getAssetManager());
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
        bulletAppState.getPhysicsSpace().addCollisionObject(playerNode.getGhostControl());
        sapp.getRootNode().attachChild(playerNode);
        sapp.getRootNode().attachChild(playerNode.getNodeCatchZone());
        playerStore.put(p.getId(), playerNode);
        System.out.println("addLocalPlayer");
        return playerNode;
        
    }
    
    //Adds a non-local player to the game and returns it
    //A non-local player has a geometry
    public Player addPlayer(PlayerLite p) {
        // Setup the player node
        Player playerNode = new Player(p, false);
        //playerNode.initZoneBallCatch(sapp.getAssetManager());
        
        // Setup the geometry for the player
        playerNode.initSpatial(sapp.getAssetManager());
        playerNode.initSound(sapp.getAssetManager());
        
        // Setup the control for the player
        BetterCharacterControl playerControl = new BetterCharacterControl(playerRadius, playerHeight, playerMass);
        playerNode.addControl(playerControl);
        playerControl.setJumpForce(new Vector3f(0, playerJumpSpeed, 0));
        playerControl.setGravity(new Vector3f(0, playerGravity, 0));
        playerControl.warp(p.getPosition());
        //playerControl.setViewDirection(p.getDirection());
        playerControl.setViewDirection(new Vector3f(1,1,1));
        bulletAppState.getPhysicsSpace().add(playerControl);
        
        sapp.getRootNode().attachChild(playerNode);
        playerStore.put(p.getId(), playerNode);
        
        return playerNode;
    }
    
    //Removes a player from the game
    public void removePlayer(int playerID) {
        Player player = playerStore.get(playerID);
        
        bulletAppState.getPhysicsSpace().remove(player.getControl(BetterCharacterControl.class));
        playerStore.remove(playerID);
        sapp.getRootNode().detachChild(player);
    }
    
    public void addBall() {
        ball = new Ball(sapp, bulletAppState);   
        sapp.getRootNode().attachChild(ball);
        ball.setPosition(new Vector3f(-20, 6f, -5));
    }
    
    public void resetBall() {
        ball.setPosition(new Vector3f(0, 30f, 0));
        ball.setAngularVelocity(new Vector3f(0, 0, 0));
        ball.setVelocity(new Vector3f(0, 30f, 0));
    }

    public Ball getBall() {
        return ball;
    }
    
    public void setBallToPlayer(int id){
        Player player = playerStore.get(id);
        player.attachChild(ball);
        System.out.println("playerPosition " + player.getPosition());
        ball.setPosition(player.getPosition().add(new Vector3f(0, 2*cameraHeight, 0)));
        ball.removePhysic();
        ball.setOwned(id);
        player.setHasBall(true);
    }
    
    public void setBallToPlayer(int id, Vector3f position){
        Player player = playerStore.get(id);
        player.attachChild(ball);
        ball.setPosition(position.add(new Vector3f(0, 2*cameraHeight, 0)));
        ball.removePhysic();
        ball.setOwned(id);
        player.setHasBall(true);
    }
    
    public void removeBallToPlayer(int id, boolean isShoot){
        Player player = playerStore.get(id);
        player.detachChild(ball);
        Vector3f position = player.getPosition();
        sapp.getRootNode().attachChild(ball);
        ball.addPhysic();
        ball.notOwnedAnymore();
        ball.setPosition(position.add(new Vector3f(0f, 2*cameraHeight, 0f)));
        ball.setVelocity(new Vector3f(0, -1, 0));
        if(isShoot)
            player.makeShoot();
        player.setHasBall(false);
    }
    
    public void incrementScore(int teamID) {
        switch (teamID) {
            case Util.BLUE_TEAM_ID:
                blueScore++;
                break;
            case Util.RED_TEAM_ID:
                redScore++;
                break;
            default:
                break;
        }
    }
    
    public void setScore(int teamID, int newScore) {
        switch (teamID) {
            case Util.BLUE_TEAM_ID:
                blueScore++;
                break;
            case Util.RED_TEAM_ID:
                redScore++;
                break;
            default:
                break;
        }
    }
    
    public int getScore(int teamID) {
        switch (teamID) {
            case Util.BLUE_TEAM_ID:
                return blueScore;
            case Util.RED_TEAM_ID:
                return redScore;
            default:
                return 0;
        }
    }
    
    private void checkWinCondition() {
        //TODO: end the game if score for either team ends up above a certain treshold
        //Or maybe the game is just time-based? Discussion needed
        
        //This function should call a function of the GameClient (abstract) since clients and server might end the game in different ways
    }
    
    //Returns a Player with id "playerID" from the playerStore
    //Returns null if not found
    public Player getPlayer(int playerID) {
        return playerStore.get(playerID);
    }

    //Makes the player with the given id jump
    public void makeJump(int playerID) {
        Player p = getPlayer(playerID);
        p.getControl(BetterCharacterControl.class).jump();
        p.makeJump();
    }
    
    public void updatePlayerPhysics(ArrayList<PlayerPhysics> physics) {
        for (PlayerPhysics pp: physics) {
            Player p = getPlayer(pp.getId());
            if (p != null) {
                p.setDirection(pp.getDirection());
                p.setVelocity(pp.getVelocity());
                //System.out.println(pp.getVelocity());
                //System.out.print(p.getIsWalking());
                if(p.getIsWalking() && pp.getVelocity().equals(new Vector3f(0, 0, 0))){                   
                    p.makeIdle();
                }
                else if(!p.getIsWalking() && !pp.getVelocity().equals(new Vector3f(0, 0, 0))){
                    p.makeRunning();
                }
                    
                p.setPosition(pp.getPosition());
                if (p.getId() == this.userID) {
                    sapp.getCamera().setLocation(p.getWorldTranslation().add(new Vector3f(0, cameraHeight, 0)));
                }
            }
        }
    }
    
    public void updateBallPhysics(BallPhysics physics) {
        if(!ball.getIsOwned()){
            ball.setPosition(physics.getPosition()); //This line causes the ball to spin in the wrong direction. Not sure why
            ball.setVelocity(physics.getVelocity());
            ball.setAngularVelocity(physics.getAngularVelocity());
        }  
    }
    
    @Override
    public void update(float tpf) {
        
    }
    
    @Override
    public void onDisable() {
        System.out.println("Game: onDisable");
        bulletAppState.getPhysicsSpace().destroy();
        sapp.getStateManager().detach(bulletAppState); //will this break anything?
        sapp.getRootNode().detachAllChildren();
    }

    public void performAttackAnim(int playerID) {
       Player p = getPlayer(playerID);
       p.makeAttack();

    }

    public void initBallToPlayer() {
        Player p = playerStore.get(ball.getOwner());
        ball.setPosition(p.getPosition());
        this.setBallToPlayer(ball.getOwner());
        
    }
}
