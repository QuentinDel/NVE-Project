/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;

/**
 *
 * An abstract class intended for implementing client/server specific game functionality
 * As an example. if a goal is registered on the server, it needs to both update the teams scores,update the ball and broadcast a message
 * while the client would only update team scores and the ball
 * 
 * @author Rickard
 */
public abstract class GameApplication extends SimpleApplication {
    
    //Should be called when a goal is registered on the client or the server
    public abstract void onGoal(int teamID);
    
    //Should be called whenever a player picks up the ball
    public abstract void grabBall(int playerID);
    
    public abstract void shootBall(int playerID, Vector3f direction, float power);
}
