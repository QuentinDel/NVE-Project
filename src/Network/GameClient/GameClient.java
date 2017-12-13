/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.GameClient;

import Game.Game;
import Network.Util.GameServerLite;
import Network.Util;
import com.jme3.app.SimpleApplication;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Network;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import Network.Util.*;
import Game.Menu;
import com.jme3.network.Message;
import java.util.Collection;

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
    private Collection<GameServerLite> servers;
    
    // AppStates
    private Menu menu = new Menu();
    private Game game = new Game();
    
    private final String hostname; // where the authentication server can be found
    private final int port; // the port att the server that we use
    
    public GameClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        
        this.stateManager.attach(menu);
        this.stateManager.attach(game);
    }
    
    public static void main(String[] args) {
        Util.initialiseSerializables();
        new GameClient(Util.HOSTNAME, Util.PORT).start();
    }

    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);
        setDisplayFps(false);
        /*
        try {
            //Initialize the queue to use to send informations
            outgoingAuth = new LinkedBlockingQueue<>();
            
            authConnection = Network.connectToServer(hostname, port); 
            this.authSender = new GameClientSender(authConnection, outgoingAuth);

            //Setup the listener for the authentication server
            authListener = new GameClientAuthListener(authConnection, this);
            authConnection.addMessageListener(authListener,
                GameServerListsMessage.class);
            
            // finally start the communication channel to the server
            authConnection.addClientStateListener(this);
            authConnection.start();
            new Thread(authSender).start();
        }catch (IOException ex) {
            ex.printStackTrace();
            this.destroy();
            this.stop();
        }
        toMenu();
        */
        menu.setEnabled(false);
        game.setEnabled(true);
    }

    public void setServerList(Collection<GameServerLite> servers) {
        if (menu.isEnabled()) {
            menu.populateServerbrowser(servers);
        }
    }
    
    public void joinServer(GameServerLite server, String name) {
        if (gameConnection != null) {
            gameConnection.close();
            //TODO close the old senderThread here
        }
        try {           
            System.out.println("Connect to a game server");
            //Initialize the queue to use to send informations
            outgoingGame = new LinkedBlockingQueue<>();
            
            gameConnection = Network.connectToServer(server.getAddress(), server.getPort());
            this.gameSender = new GameClientSender(gameConnection, outgoingGame);

            //Setup the listener for the game server
            gameListener = new GameClientListener(gameConnection, this);
            gameConnection.addMessageListener(authListener,
                JoinAckMessage.class,
                LobbyInformationMessage.class,
                GameInformationMessage.class);
            
            // finally start the communication channel to the server
            gameConnection.addClientStateListener(this);
            gameConnection.start();
            new Thread(gameSender).start();
            outgoingAuth.put(new Util.RefreshMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
            this.destroy();
            this.stop();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public void queueRefreshMessage() {
        System.out.println("Queueing refresh message to auth server");
        try {
            if (outgoingAuth != null) {
                outgoingAuth.put(new RefreshMessage());   
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    // Go to the menu
    public void toMenu() {
        //TODO use gotoscreen, let menu appstate always be enabled
        menu.setEnabled(true);
        //Disable game appstate, etc
    }
    
    
    
    @Override
    public void simpleUpdate(float tpf) {
        
    }
    
    // takes down all communication channels gracefully, when called
    @Override
    public void destroy() {
        if (authConnection != null) {
            authConnection.close();
        }
        if (gameConnection != null) {
            gameConnection.close();
        }
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
