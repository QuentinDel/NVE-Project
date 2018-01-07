/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;
import Network.GameApplication;
import Network.Util;
import Network.Util.BallPhysics;
import Network.Util.PlayerLite;
import Network.Util.PlayerPhysics;
import Playboard.GrassPlayground;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * This is the main Game Appstate.
 * This appstate keeps track of the current state of the game; positions/velocities of players/ball, etc.
 * It does NOT contain any game logic for updating positions or such, as those things are handles separately by physicsControls
 * 
 * @author Rickard
 * Physics setup, loading of level
 * 
 * @author Quentin
 * Animations, work on ball/players such as ownership
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
    
    //Sets the level that is to be loaded
    public void setLevel(String level_id) {
        this.level_id = level_id;
    }
    
    // Loads the level, creates players and adds physics to them.
    public void initLevel() {
        //Currently the level we use is hardcoded
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
        GoalControl blue = playground.getBlueGoalControl();
        GoalControl red = playground.getRedGoalControl();
        
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
        this.userID = p.getId();
        
        // Move the player to their initial Position (should be based on team(;
        playerNode.move(new Vector3f(0, 3.5f, 0));
        
        // Setup the control for the player
        playerControl = new BetterCharacterControl(playerRadius, playerHeight, playerMass);
        playerNode.addControl(playerControl);
        playerControl.setJumpForce(new Vector3f(0, playerJumpSpeed, 0));
        playerControl.setGravity(new Vector3f(0, playerGravity, 0));
        playerControl.warp(p.getPosition());
        playerControl.setViewDirection(p.getDirection());
        
        //Attach player/control to the scene graph and the physicsspace
        bulletAppState.getPhysicsSpace().add(playerControl);
        bulletAppState.getPhysicsSpace().addCollisionObject(playerNode.getGhostControl());
        sapp.getRootNode().attachChild(playerNode);
        sapp.getRootNode().attachChild(playerNode.getNodeCatchZone());
        playerStore.put(p.getId(), playerNode);
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
    
    //Add a ball to the game, the ball is initially dropped from the sky
    //Currently only supports one ball
    public void addBall() {
        ball = new Ball(sapp, bulletAppState);   
        sapp.getRootNode().attachChild(ball);
        ball.setPosition(new Vector3f(0, 30f, 0));
        ball.setVelocity(new Vector3f(0, 30f, 0));
    }
    
    //Remove the current ball from the game and add a new one
    public void resetBall() {
        ball.removePhysic();
        sapp.getRootNode().detachChild(ball);
        
        addBall();
    }

    public Ball getBall() {
        return ball;
    }
    
    //Give a player ownership of the ball (they pick it up)
    public void setBallToPlayer(int id){
        Player player = playerStore.get(id);
        player.attachChild(ball);
        ball.setPosition(player.getPosition().add(new Vector3f(0, 2*cameraHeight, 0)));
        ball.removePhysic();
        ball.setOwned(id);
        player.setHasBall(true);
    }
    
    //Similar to above, but takes the position of the player that owns the ball
    public void setBallToPlayer(int id, Vector3f position){
        Player player = playerStore.get(id);
        player.attachChild(ball);
        ball.setPosition(position.add(new Vector3f(0, 2*cameraHeight, 0)));
        ball.removePhysic();
        ball.setOwned(id);
        player.setHasBall(true);
    }
    
    //Force a player to drop the ball
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
                blueScore = newScore;
                break;
            case Util.RED_TEAM_ID:
                redScore = newScore;
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
    
    //Updates the position, velocity and viewdirection of all players in the given Arraylist
    public void updatePlayerPhysics(ArrayList<PlayerPhysics> physics) {
        for (PlayerPhysics pp: physics) {
            Player p = getPlayer(pp.getId());
            if (p != null) {
                p.setDirection(pp.getDirection());
                p.setVelocity(pp.getVelocity());
                p.setPosition(pp.getPosition());
                if(p.getIsWalking() && pp.getVelocity().equals(new Vector3f(0, 0, 0))){                   
                    p.makeIdle();
                }
                else if(!p.getIsWalking() && !pp.getVelocity().equals(new Vector3f(0, 0, 0))){
                    p.makeRunning();
                }
                    
                if (p.getId() == this.userID) {
                    sapp.getCamera().setLocation(p.getWorldTranslation().add(new Vector3f(0, cameraHeight, 0)));
                }
            }
        }
    }
    
    //Update the position, velocity and angularvelocity of the ball
    public void updateBallPhysics(BallPhysics physics) {
        if(!ball.getIsOwned()){
            ball.setPosition(physics.getPosition());
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
        sapp.getStateManager().detach(bulletAppState);
        sapp.getRootNode().detachAllChildren();
    }

    public void performAttackAnim(int playerID) {
       Player p = getPlayer(playerID);
       p.makeAttack();

    }
    
    public String getLevelId(){
        return level_id;
    }
}
