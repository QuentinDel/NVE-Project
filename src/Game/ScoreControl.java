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
    
    public ScoreControl(GameApplication app, BoxCollisionShape shape, Game game){
        super(shape);
        this.app = app;
        this.game = game;
    }
    
    public void collision(PhysicsCollisionEvent event) {
        if (event.getObjectA() == this && event.getNodeB() instanceof Ball) {
            System.out.println("Detected Collision with ball");
            app.onGoal();
        } else if (event.getObjectB() == this && event.getNodeA() instanceof Ball) {
            System.out.println("Detected Collision with ball");
            app.onGoal();
        }
    }
    
    private void goal() {
        //TODO detect which team's goal it was
        //TODO increment score
        //TODO networking
        //TODO stop ball from spinning after being reset (or is that good/cool?)
    }
}
