/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.gameserver;

import Network.Util;
import com.jme3.network.Client;
import com.jme3.network.Message;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Henrik
 */
public class GameToAuthSender implements Runnable {
    private final Client serverConnection;
    private final LinkedBlockingQueue<Message> outgoing;
    
    public GameToAuthSender(Client serverConnection, LinkedBlockingQueue<Message> outgoing) {
        this.serverConnection = serverConnection;
        this.outgoing = outgoing;
    }

    @Override
    public void run() {
        while(true){
            try {
                Message msg = outgoing.take();
                this.serverConnection.send(msg);
                System.out.println("sent gameinfo");

            } catch (InterruptedException ex) {
                Logger.getLogger(GameToAuthSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
}
