/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

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
        System.out.println("Not supported yet.");
    }

    private void initLevel() {
        sapp.getAssetManager().registerLocator("town.zip", ZipLocator.class);
        Spatial gameLevel = sapp.getAssetManager().loadModel("main.scene");
        gameLevel.setLocalTranslation(0, -5.2f, 0);
        gameLevel.setLocalScale(2);
        sapp.getRootNode().attachChild(gameLevel);
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f).normalizeLocal());
        sapp.getRootNode().addLight(sun);
    }

    private void initPlayer() {
        System.out.println("Not supported yet.");
    }

    private void initPlayers() {
        System.out.println("Not supported yet.");
    }

    private void initBall() {
        System.out.println("Not supported yet.");
    }
}
