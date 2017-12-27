/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import Network.GameClient.GameClient;
import Network.Util.InternalMovementMessage;
import Network.Util.JumpMessage;
import Network.Util.PlayerMovementMessage;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector3f;
import java.util.concurrent.Callable;

/**
 *
 * @author Rickard
 */
public class PlayerMovement extends BaseAppState {
    
    private GameClient sapp;
    private Player playerNode;
    private Ball ball;
    private BetterCharacterControl playerControl;
    private boolean playerInitialized = false;
    
    
    private Vector3f walkDirection = new Vector3f();
    private Vector3f lastWalkDirection = new Vector3f();
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();
    private Vector3f lastCamDir = new Vector3f();
    
    //Booleans for character movement directions
    private boolean left = false, right = false, up = false, down = false;
    
    private final float playerMoveSpeed = 20;
    
    
    @Override
    public void initialize(Application app) {
        sapp = (GameClient) app;
        
    }
    
    @Override
    public void onEnable() {
        System.out.println("move: OnEnable");
        //Setup keysmappings
        sapp.getInputManager().addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        sapp.getInputManager().addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        sapp.getInputManager().addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        sapp.getInputManager().addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        sapp.getInputManager().addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        sapp.getInputManager().addMapping("Catch", new KeyTrigger(KeyInput.KEY_R));
        sapp.getInputManager().addListener(actionListener, "Left", "Right", "Up", "Down", "Jump", "Catch");
    }
    
    @Override
    public void onDisable() {
        //Remove keymappings
        sapp.getInputManager().deleteMapping("Left");
        sapp.getInputManager().deleteMapping("Right");
        sapp.getInputManager().deleteMapping("Up");
        sapp.getInputManager().deleteMapping("Down");
        sapp.getInputManager().deleteMapping("Jump");
        
    }
    
    //Sets which player should be moved by keyboard inputs
    //Preferably this is set to the Player that the client is supposed to move
    public void setPlayer(Player p) {
        this.playerNode = p;
        this.playerControl = p.getControl(BetterCharacterControl.class);
        this.playerInitialized = true;
    }
    
    /** These are our custom actions triggered by key presses.
     * We do not walk yet, we just keep track of the direction the user pressed. */
    private ActionListener actionListener = new ActionListener() {
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
                if (isPressed) { 
                    playerControl.jump();
                    sapp.queueGameServerMessage(new JumpMessage(sapp.getPlayerID()));
                }
            } else if (binding.equals("Catch") && isPressed){
                for (PhysicsCollisionObject collObj : playerNode.getGoshtControl().getOverlappingObjects()){
                    if(collObj.getUserObject() instanceof Ball){
                        Ball ballCatch = (Ball) collObj.getUserObject();
                        if(!ballCatch.getIsOwned()){
                            System.out.println("Try to catch the ball");
                            playerNode.attachChild(ballCatch);
                        }
                    }
                    
                }
                    
                //}
                
            }
               
        }
    };
    
    @Override
    public void update(float tpf) {
        if (this.playerInitialized) {
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
            if (walkDirection.equals(new Vector3f()) && !lastWalkDirection.equals(walkDirection)) {
                //If we stopped moving, send a final playermovementmessage to indicate that we are now standing still
                sapp.queueGameServerMessage(new PlayerMovementMessage(walkDirection, camDir));
                float cameraHeight = sapp.getStateManager().getState(Game.class).cameraHeight;

                playerControl.setWalkDirection(walkDirection);
                playerControl.setViewDirection(camDir);
                sapp.getCamera().setLocation(playerNode.getWorldTranslation().add(new Vector3f(0, cameraHeight, 0)));
                lastCamDir = camDir;
                lastWalkDirection = walkDirection.clone();
                return;
            }
            if (walkDirection.equals(new Vector3f()) && lastCamDir.equals(camDir)) {
                //If we are not moving or turning, do nothing
                return;
            }
            
            sapp.queueGameServerMessage(new InternalMovementMessage(walkDirection, camDir, tpf));
            float cameraHeight = sapp.getStateManager().getState(Game.class).cameraHeight;
            
            playerControl.setWalkDirection(walkDirection);
            playerControl.setViewDirection(camDir);
            sapp.getCamera().setLocation(playerNode.getWorldTranslation().add(new Vector3f(0, cameraHeight, 0)));
            
            //Catch zone movement
            playerNode.getNodeCatchZone().setLocalTranslation(sapp.getCamera().getLocation());
            playerNode.getNodeCatchZone().getChild(0).setLocalTranslation(sapp.getCamera().getDirection().multLocal(7));

            lastCamDir = camDir;
            lastWalkDirection = walkDirection.clone();
        }
    }
    
    public void setBall(Ball ball){
        this.ball = ball;
    }
    
    @Override
    public void cleanup(Application app) {
        
    }
    
}
