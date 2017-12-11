/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.AuthServer;

import Network.Util;
import Network.Util.GameInformationMessage;
import Network.Util.GameServerLite;
import Network.Util.RefreshMessage;
import com.jme3.app.SimpleApplication;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Quentin
 */
public class AuthServer extends SimpleApplication{
    
    private static Server authServer;
    private final int port;    
    private ConcurrentHashMap< String, GameServerLite > gamingServerInfos;
    private LinkedBlockingQueue<Callable> outgoing;
    
    public static void main(String[] args) {
        System.out.println("Server initializing");
        Util.initialiseSerializables();
        new AuthServer(Util.PORT).start(JmeContext.Type.Headless);
        //new TheServer(Util.PORT).start();
    }
    
    public AuthServer(int port) {
        this.port = port;      
        this.gamingServerInfos = new ConcurrentHashMap<>();
        this.outgoing = new LinkedBlockingQueue<>();
    }
    
    
    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void simpleInitApp() {
        // In a game server, the server builds and maintains a perfect 
        // copy of the game and makes use of that copy to make descisions 
        
        try {
            System.out.println("Using port " + port);
            // create the server by opening a port
            authServer = Network.createServer(port);
            authServer.start(); // start the server, so it starts using the port
        } catch (IOException ex) {
            ex.printStackTrace();
            destroy();
            this.stop();
        }
        System.out.println("Server started");
        
        // add a listeners
        authServer.addMessageListener(new AuthServerListener(authServer, gamingServerInfos, outgoing), 
                GameInformationMessage.class,
                RefreshMessage.class
        );
        
        authServer.addConnectionListener(new AuthConnectionListener(authServer, gamingServerInfos, outgoing));
        
        // add a packet sender that takes messages from the blockingqueue
        new Thread(new AuthMessageSender(outgoing)).start();
    }
    
    
    private class AuthMessageSender implements Runnable {

        private final LinkedBlockingQueue<Callable> outgoing;

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

}

    