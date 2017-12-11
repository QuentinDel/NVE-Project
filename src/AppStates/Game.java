/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AppStates;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;

/**
 *
 * @author Rickard
 */
public class Game extends BaseAppState {
    
    private SimpleApplication sapp;
    private boolean needCleaning = false;
    
    
    @Override
    protected void initialize(Application app) {
        System.out.println("Game: initialize");
        sapp = (SimpleApplication) app;
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
        
        this.initCam();
        //init the level? should this be done here?
        this.initLevel();
        //init the player character
        this.initPlayer();
        //init the other players
        this.initPlayers();
        //initialise the ball
        this.initBall();
        
    }
    
    @Override
    public void onDisable() {
        System.out.println("Game: onDisable");
        //Remove player controls
    }
    

    @Override
    public void update(float tpf) {
        
    }

    private void initCam() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void initLevel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void initPlayer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void initPlayers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void initBall() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
