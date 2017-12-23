/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.GameClient;

import Network.Util.InternalMovementMessage;
import Network.Util.JumpMessage;
import Network.Util.PlayerMovementMessage;
import com.jme3.network.Client;
import java.util.concurrent.LinkedBlockingQueue;
import Network.gameserver.GameToAuthSender;
import com.jme3.math.Vector3f;
import com.jme3.network.Message;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rickard
 */
public class GameClientSender implements Runnable {
    private static final float CLIENT_SEND_RATE = 30f;
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
                /*
                if(msg instanceof InternalMovementMessage) {
                    InternalMovementMessage internalMsg = (InternalMovementMessage) msg;
                    float tpf = internalMsg.getTpf();
                    System.out.println("aggregate this"+internalMsg.getVelocity());
                    System.out.println("before"+aggregatedMovement.getVelocity());
                    aggregatedMovement.updateVelocity(internalMsg.getVelocity().mult(tpf));
                    System.out.println("after"+aggregatedMovement.getVelocity());
                    aggregatedMovement.updateViewDirection(internalMsg.getViewDirection());
                    
                    timer += tpf;
                    if (timer >= 1/CLIENT_SEND_RATE) {
                        System.out.println("Do we sent the aggregated message?");
                        System.out.println(aggregatedMovement.getVelocity());
                        this.serverConnection.send(aggregatedMovement);
                        aggregatedMovement = new PlayerMovementMessage(new Vector3f(), new Vector3f());
                        timer = 0f;
                    }
                } else {*/
                    this.serverConnection.send(msg);
                //}
            } catch (InterruptedException ex) {
                Logger.getLogger(GameToAuthSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
