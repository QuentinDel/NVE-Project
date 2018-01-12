/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import Network.GameClient.GameClient;
import Network.Util;
import Network.Util.AttackMessage;
import Network.Util.InternalMovementMessage;
import Network.Util.JumpMessage;
import Network.Util.PlayerMovementMessage;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector3f;

/**
 *
 * Handles input related to the local player.
 * This class also informs the gameclient to queue movementmessages.
 * 
 * The basic movement of the player is based on the "Walking Character" example from the JME3 wiki/documentation pages
 * 
 * @author Rickard
 * Implementation,
 * Discussion
 * 
 * @Quentin
 * Ball catching/shooting
 * Discussion
 * 
 * @Henrik
 * Discussion
 */
public class PlayerMovement extends BaseAppState {
    
    private GameClient sapp;
    private Player playerNode;
    private BetterCharacterControl playerControl;
    private boolean playerInitialized = false;
    
    private Vector3f walkDirection = new Vector3f();
    private Vector3f lastWalkDirection = new Vector3f();
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();
    private Vector3f lastCamDir = new Vector3f();
    
    //Booleans for character movement directions
    private boolean left = false, right = false, up = false, down = false, grapBall = false;
    
    private final float playerMoveSpeed = 20;
    private float powerShoot = 0;
    private final float MAXPOWERSHOOT = 100f;
    
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
        sapp.getInputManager().addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        sapp.getInputManager().addMapping("Attack", new KeyTrigger(KeyInput.KEY_F));
        sapp.getInputManager().addListener(actionListener, "Left", "Right", "Up", "Down", "Jump", "Catch", "Pause", "Attack");
    }
    
    @Override
    public void onDisable() {
        //Remove keymappings
        sapp.getInputManager().deleteMapping("Left");
        sapp.getInputManager().deleteMapping("Right");
        sapp.getInputManager().deleteMapping("Up");
        sapp.getInputManager().deleteMapping("Down");
        sapp.getInputManager().deleteMapping("Jump");
        sapp.getInputManager().deleteMapping("Catch");
        sapp.getInputManager().deleteMapping("Pause");
        sapp.getInputManager().deleteMapping("Attack");
        if (sapp.getInputManager().hasMapping("LoadFire")) {
            sapp.getInputManager().deleteMapping("LoadFire");
        }
    }
    
    //Sets which player should be moved by keyboard inputs
    //Preferably this is set to the Player that the client is supposed to move
    public void setPlayer(Player p) {
        this.playerNode = p;
        this.playerControl = p.getControl(BetterCharacterControl.class);
        this.playerInitialized = true;
    }
    
    //Insert mappings for shooting
    public void insertLoadBar() {
        sapp.getInputManager().addMapping("LoadFire", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        sapp.getInputManager().addListener(actionListener, "LoadFire");
        sapp.getInputManager().addListener(analogListener, "LoadFire");
    }
    
    //Remove mapping for shooting
    public void removeLoadBar() {
        sapp.getInputManager().deleteMapping("LoadFire");
    }
    
    private final AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {
             if (name.equals("LoadFire") && powerShoot < 1) {
               powerShoot += tpf;
               if (powerShoot > MAXPOWERSHOOT) {
                   powerShoot = MAXPOWERSHOOT;
               }
               sapp.getStateManager().getState(Menu.class).setLoadBar(powerShoot, 1);
            }
        }
    };
    
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
                    playerControl.jump(); //Cause the player to jump
                    if (playerControl.isOnGround()) {
                        playerNode.makeJump(); //Play the jump sound if the player is standing on the ground
                    }
                    sapp.queueGameServerMessage(new JumpMessage(sapp.getPlayerID()));
                }
            } else if (binding.equals("Catch") && !isPressed){
                for (PhysicsCollisionObject collObj : playerNode.getGhostControl().getOverlappingObjects()){
                    if(collObj.getUserObject() instanceof Ball){
                        Ball ballCatch = (Ball) collObj.getUserObject();
                        if(!ballCatch.getIsOwned()){
                            grapBall = true;
                        }
                    }
                }
            } else if (binding.equals("LoadFire") && !isPressed) {
                sapp.queueGameServerMessage(new Util.ShootBallMessage(playerNode.getId(), sapp.getCamera().getDirection(), powerShoot * MAXPOWERSHOOT));
                powerShoot = 0f;
                sapp.getStateManager().getState(Menu.class).setLoadBar(powerShoot, 1);
            } else if (binding.equals("Pause") && isPressed) {
                sapp.pause();
            } else if (binding.equals("Attack") && isPressed) {
                if (!playerNode.hasBall()) {
                    Vector3f location = sapp.getCamera().getLocation().add(
                        sapp.getCamera().getDirection().multLocal(7));
                    sapp.queueGameServerMessage(new AttackMessage(
                        sapp.getPlayerID(),
                        location));
                }
            }
        }
    };
    
    @Override
    public void update(float tpf) {
        if (this.playerInitialized) {
            //Here we handle the movement of the local player
            //First get the camera direction, set the y-axis to 0 so we dont walk up into the air
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
            if (grapBall){
                grapBall = false;
                Vector3f location = sapp.getCamera().getLocation().add(
                        sapp.getCamera().getDirection().multLocal(7));
                sapp.queueGameServerMessage(new Util.GrabBallMessage(
                        playerNode.getId(),
                        location));
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
            
            //Queue InternalMovementMessage (used for aggregation)
            sapp.queueGameServerMessage(new InternalMovementMessage(walkDirection, camDir, tpf));
            float cameraHeight = sapp.getStateManager().getState(Game.class).cameraHeight;
            
            //Set walking and view direction and position camera
            playerControl.setWalkDirection(walkDirection);
            playerControl.setViewDirection(camDir);
            sapp.getCamera().setLocation(playerNode.getWorldTranslation().add(new Vector3f(0, cameraHeight, 0)));
            
            //Catch zone movement
            playerNode.updateCatchZone(sapp.getCamera().getLocation(), sapp.getCamera().getDirection().multLocal(5));

            //Used to see if the player has stopped moving/turning
            lastCamDir = camDir;
            lastWalkDirection = walkDirection.clone();
        }
    }
    
    @Override
    public void cleanup(Application app) {
        
    }
    
}
