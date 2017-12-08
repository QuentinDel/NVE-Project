/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.gameserver;

import Network.Util.GameInformationMessage;
import com.jme3.network.Message;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Henrik
 */
public class GameInformationGenerator implements Runnable {
    LinkedBlockingQueue<Message> outgoing;

    public GameInformationGenerator(LinkedBlockingQueue<Message> outgoing) {
        this.outgoing = outgoing;
    }

    @Override
    public void run() {
        //gather information(needs to be fed with necessary references or updated with data)
        //send it to outgoing queue
        GameInformationMessage msg = new GameInformationMessage();
        try {
            outgoing.put(msg);
        } catch (InterruptedException ex) {
            Logger.getLogger(GameInformationGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        //sleep
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Logger.getLogger(GameInformationGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
