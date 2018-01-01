/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import Network.GameApplication;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.GhostControl;

/**
 *
 * @author Rickard
 */
public class ScoreControl extends GhostControl implements PhysicsCollisionListener {
    
    private GameApplication app;
    private Game game;
    private int teamID;
    
    public ScoreControl(GameApplication app, BoxCollisionShape shape, int teamID){
        super(shape);
        this.app = app;
        this.game = game;
        this.teamID = teamID;
    }
    
    //TODO sometimes multiple goals are registered before the ball is reset, fix
    //This is probably because the onGoal() function gets called multiple times in a single physics tick,
    //that is, before the overlapping objects are emptied out
    public void collision(PhysicsCollisionEvent event) {
        if (event.getObjectA() == this && event.getNodeB() instanceof Ball) {
            app.onGoal(teamID);
        } else if (event.getObjectB() == this && event.getNodeA() instanceof Ball) {
            app.onGoal(teamID);
        }
    }
}
