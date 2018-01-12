/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.math.Vector3f;
import java.util.Enumeration;

/**
 * 
 * This PhysicsTickListener handles the converging of predicted values for players and the ball.
 * In each physicsTick, we move the actual values toward the predicted ones, the amount is dependent on the convergence factor
 * We also calculate the predicted values for the next physicstick.
 * Only the position of the players is predicted, velocity and direction of players are the result of human interaction and are impossible to predict
 * (Note that velocity for players is only the players movement controls, it does not affect gravity which we let the inbuilt-physics handle)
 * 
 * For the ball we do not predict velocity and angular velocity as they are calculated by the inbuilt-physics.
 * 
 * @author Rickard
 * implementation, discussion
 * 
 * @author Quentin, Henrik
 * discussion
 */
public class ConvergenceTickListener implements PhysicsTickListener {
    
    //How much we move towards the predicted values in each tick
    private final float CONVERGENCE_FACTOR = 0.2f;
    
    private Game game;
    
    public ConvergenceTickListener(Game game){
        this.game = game;
    }
    
    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        //Move towards the predicted values
        Enumeration<Player> players = game.getPlayerStore().elements();
        while (players.hasMoreElements()) {
            Player player = players.nextElement();
            //Position
            Vector3f positionChange = (player.getPredictedPosition().subtract(player.getPosition())).mult(CONVERGENCE_FACTOR);
            Vector3f newPosition = player.getPosition().add(positionChange);
            player.setPosition(newPosition);
            
            //Velocity
            Vector3f velocityChange = (player.getPredictedVelocity().subtract(player.getVelocity())).mult(CONVERGENCE_FACTOR).mult(tpf);
            Vector3f newVelocity = player.getVelocity().add(velocityChange);
            player.setVelocity(newVelocity);
            
            //Direction
            Vector3f directionChange = (player.getPredictedDirection().subtract(player.getDirection())).mult(CONVERGENCE_FACTOR);
            Vector3f newDirection = player.getDirection().add(directionChange);
            player.setDirection(newDirection);
            
            //Update predicted position
            Vector3f newPredictedPosition = player.getPredictedPosition().add(player.getPredictedVelocity().mult(tpf));
            player.setPredictedPosition(newPredictedPosition);
        }
        
        //Convergence for the ball
        Ball ball = game.getBall();
        //If the ball is owned (picked up) there is nothing to predict or converge
        if (!ball.getIsOwned()) {
            //Position
            Vector3f positionChange = (ball.getPredictedPosition().subtract(ball.getPosition())).mult(CONVERGENCE_FACTOR);
            Vector3f newPosition = ball.getPosition().add(positionChange);
            ball.setPosition(newPosition);

            //Velocity
            Vector3f velocityChange = (ball.getPredictedVelocity().subtract(ball.getVelocity())).mult(CONVERGENCE_FACTOR).mult(tpf);
            Vector3f newVelocity = ball.getVelocity().add(velocityChange);
            ball.setVelocity(newVelocity);

            //Angular Velocity
            Vector3f angularVelocityChange = (ball.getPredictedAngularVelocity().subtract(ball.getAngularVelocity())).mult(CONVERGENCE_FACTOR).mult(tpf);
            Vector3f newAngularVelocity = ball.getAngularVelocity().add(angularVelocityChange);
            ball.setAngularVelocity(newAngularVelocity);

            //Update predicted position
            Vector3f newPredictedPosition = ball.getPredictedPosition().add(ball.getPredictedVelocity().mult(tpf));
            ball.setPredictedPosition(newPredictedPosition);
        }
    }
    
    @Override
    public void physicsTick(PhysicsSpace space, float tpf) {}
    
}
