/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.AuthServer;

import Network.GameServerLite;
import Network.Util;
import Network.Util.GameInformationMessage;
import Network.Util.RefreshMessage;
import com.jme3.app.SimpleApplication;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Quentin
 */
public class AuthServer extends SimpleApplication{
    
    private Server authServer;
    private final int port;    
    
    public static void main(String[] args) {
        System.out.println("Server initializing");
        Util.initialiseSerializables();
        new AuthServer(Util.PORT).start(JmeContext.Type.Headless);
        //new TheServer(Util.PORT).start();
    }
    
    public AuthServer(int port) {
        this.port = port;               
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
        authServer.addMessageListener(new AuthServerListener(), 
                GameInformationMessage.class,
                RefreshMessage.class
        );
        
        authServer.addConnectionListener(new MyConnectionListener());
        
        // add a packet sender that takes messages from the blockingqueue
        new Thread(new AuthMessageSender()).start();
    }

}
