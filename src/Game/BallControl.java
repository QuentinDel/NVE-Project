/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import Network.GameApplication;
import Network.Util;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;
import java.util.concurrent.Callable;

/**
 * 
 * This GhostControl detects collisions with the Goals in the level
 * After a collision with a goal is detected, remove the ghostControl and the PhysicsTickListener
 * from the physics space to prevent multiple collision detections
 * 
 * @author Rickard
 */
public class BallControl extends GhostControl implements PhysicsTickListener {
    
    private GameApplication app;
    
    public BallControl(CollisionShape shape, GameApplication app){
        super(shape);
        this.app = app;
    }
    
    public void prePhysicsTick(PhysicsSpace space, float tpf) {}
    
    public void physicsTick(PhysicsSpace space, float tpf) {
        for (PhysicsCollisionObject o: getOverlappingObjects()) {
            if (o instanceof GoalControl) {
                GoalControl goal = (GoalControl) o;
                final int team = goal.getTeam();
                space.remove(this);
                space.removeTickListener(this);
                app.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        app.onGoal(team);
                        return true;
                    }
                });
            }
        }
    }
    
}
