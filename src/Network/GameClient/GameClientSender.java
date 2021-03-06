/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.GameClient;

import Network.Util.InternalMovementMessage;
import Network.Util.PlayerMovementMessage;
import Network.Util.TerminateMessage;
import com.jme3.network.Client;
import java.util.concurrent.LinkedBlockingQueue;
import Network.gameserver.GameToAuthSender;
import com.jme3.math.Vector3f;
import com.jme3.network.Message;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This Runnable handles the sending of messages from gameClient to gameServer.
 * In the case of movementmessages, they are aggregated to save bandwidth
 *
 * @author Rickard
 * Implementation,
 * Discussion
 * 
 * @author Quentin, Henrik
 * Discussion
 */
public class GameClientSender implements Runnable {
    private static final float CLIENT_SEND_RATE = 60f;
    private static float timer = 0f;
    private PlayerMovementMessage aggregatedMovement;
    
    private Client serverConnection;
    private LinkedBlockingQueue<Message> outgoing;
    
    public GameClientSender(Client serverConnection, LinkedBlockingQueue<Message> outgoing) {
        this.serverConnection = serverConnection;
        this.outgoing = outgoing;
        
        aggregatedMovement = new PlayerMovementMessage(new Vector3f(), new Vector3f());
    }

    @Override
    public void run() {
        while(true){
            try {
                Message msg = outgoing.take();
                
                if(msg instanceof InternalMovementMessage) {
                    InternalMovementMessage internalMsg = (InternalMovementMessage) msg;
                    float tpf = internalMsg.getTpf();
                    //Scale the velocity of each individual packet with tpf
                    aggregatedMovement.updateVelocity(internalMsg.getVelocity().mult(tpf));
                    aggregatedMovement.updateViewDirection(internalMsg.getViewDirection());

                    timer += tpf;
                    if (timer >= 1/CLIENT_SEND_RATE) {
                        //Scale the velocity of the aggregated packet back to the original magnitude/size.
                        //Each of the non-aggregated packets now contribute according to the tpf value
                        aggregatedMovement.scaleVelocity(1/timer);
                        this.serverConnection.send(aggregatedMovement);
                        aggregatedMovement = new PlayerMovementMessage(new Vector3f(), new Vector3f());
                        timer = 0f;
                    }
                } else if (msg instanceof PlayerMovementMessage) {
                    PlayerMovementMessage movementMsg = (PlayerMovementMessage) msg;
                    movementMsg.setReliable(true);

                    //First send any built-up aggregation
                    this.serverConnection.send(aggregatedMovement);
                    this.serverConnection.send(movementMsg);
                    aggregatedMovement = new PlayerMovementMessage(new Vector3f(), new Vector3f());
                    timer = 0f;
                } else if (msg instanceof TerminateMessage){
                    //Terminate this thread
                    break;
                } else {
                    this.serverConnection.send(msg);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(GameToAuthSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
