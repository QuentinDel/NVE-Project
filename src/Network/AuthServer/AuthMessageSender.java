/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.AuthServer;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Quentin
 */
public class AuthMessageSender implements Runnable {
    
    private LinkedBlockingQueue<Callable> outgoing;
    
    public AuthMessageSender(LinkedBlockingQueue<Callable> outgoing){
        this.outgoing = outgoing;
    }
    
    
    
@Override
   public void run() {
        System.out.println("MesssageSender thread running");
        try {
           while (true) {
               outgoing.take().call();
           }
        }catch (Exception ex) {
           Logger.getLogger(AuthMessageSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
