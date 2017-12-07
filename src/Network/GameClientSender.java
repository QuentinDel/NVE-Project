/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network;

import com.jme3.network.Client;
import java.util.concurrent.LinkedBlockingQueue;
import Network.Util.MyAbstractMessage;

/**
 *
 * @author Rickard
 */
public class GameClientSender implements Runnable {
    private static final float CLIENT_SEND_RATE = 30f;
    
    private Client serverConnection;
    
    public GameClientSender(Client serverConnection) {
        this.serverConnection = serverConnection;
    }
    
    
    private void sendMessage(MyAbstractMessage msg){
        this.serverConnection.send(msg);
        
    }

    @Override
    public void run() {
        while(true){
            //TODO: Construct messages from a BlockingLinkedQUeue
        }
    }
}
