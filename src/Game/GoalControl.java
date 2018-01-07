/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.GhostControl;

/**
 * This GhostControl represents a goal in the game
 * A goal belongs to a team
 * We use this to check for collisions with the ball (see BallControl)
 * 
 * @author Rickard
 */

public class GoalControl extends GhostControl{
    
    private int teamID;
    
    public GoalControl(BoxCollisionShape shape, int teamID){
        super(shape);
        this.teamID = teamID;
    }
    
    public int getTeam() {
        return teamID;
    }
}

