/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network;

import com.jme3.app.SimpleApplication;
import com.jme3.input.controls.ActionListener;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Network;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import Network.Util.*;
import com.jme3.network.Message;

/**
 *
 * Client simpleApplication for running the game.
 * 
 * @author Rickard
 */

public class GameClient extends SimpleApplication implements ClientStateListener {
    
    private GameClientAuthListener authListener;
    private GameClientListener gameListener;
    private GameClientSender authSender;
    private GameClientSender gameSender;
    
    // the connection back to the server
    private Client authConnection;
    private Client gameConnection;
    
    // Messsage queues
    private LinkedBlockingQueue<Message> outgoingAuth;
    private LinkedBlockingQueue<Message> outgoingGame;
    
    // Serverlist
    private ArrayList<GameServerLite> servers;
    
    private final String hostname; // where the authentication server can be found
    private final int port; // the port att the server that we use
    
    public GameClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }
    
    public static void main(String[] args) {
        Util.initialiseSerializables();
        new GameClient(Util.HOSTNAME, Util.PORT).start();
    }

    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);
        setDisplayFps(false);
        try {
            //Initialize the queue to use to send informations
            outgoingAuth = new LinkedBlockingQueue<>();
            
            authConnection = Network.connectToServer(hostname, port); 
            this.authSender = new GameClientSender(authConnection, outgoingAuth);

            //Setup the listener for the authentication server
            authListener = new GameClientAuthListener(authConnection, this);
            authConnection.addMessageListener(authListener,
                GameServerListsMessage.class,
                RefreshMessage.class);
            
            // finally start the communication channel to the server
            authConnection.addClientStateListener(this);
            authConnection.start();
            new Thread(authSender).start();
            outgoingAuth.put(new Util.RefreshMessage());
        }catch (IOException ex) {
            ex.printStackTrace();
            this.destroy();
            this.stop();
        }catch (InterruptedException ex) {
            
        }
        
    }

    public void setServerList(ArrayList<GameServerLite> servers) {
        this.servers = servers;
    }
    
    
    @Override
    public void simpleUpdate(float tpf) {
        
    }
    
    // takes down all communication channels gracefully, when called
    @Override
    public void destroy() {
        authConnection.close();
        gameConnection.close();
        super.destroy();
    }
    
    @Override
    public void clientConnected(Client c) {
        System.out.println("Client connected succesfully !");
        
    }

    @Override
    public void clientDisconnected(Client c, DisconnectInfo info) {
        System.out.println("DisconnectInfo "+ info);
        System.exit(0);
    }
}
