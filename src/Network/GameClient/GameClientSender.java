/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.GameClient;

import Network.Util.JumpMessage;
import Network.Util.PlayerMovement;
import com.jme3.network.Client;
import java.util.concurrent.LinkedBlockingQueue;
import Network.gameserver.GameToAuthSender;
import com.jme3.network.Message;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rickard
 */
public class GameClientSender implements Runnable {
    private static final float CLIENT_SEND_RATE = 30f;
    
    private Client serverConnection;
    private LinkedBlockingQueue<Message> outgoing;
    
    public GameClientSender(Client serverConnection, LinkedBlockingQueue<Message> outgoing) {
        this.serverConnection = serverConnection;
        this.outgoing = outgoing;
    }

    @Override
    public void run() {
        while(true){
            try {
                Message msg = outgoing.take();
                System.out.println("GOT MESSAGE, is it jump?" + (msg instanceof JumpMessage));
                this.serverConnection.send(msg);
            } catch (InterruptedException ex) {
                Logger.getLogger(GameToAuthSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
