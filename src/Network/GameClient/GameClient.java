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
import Game.Player;
import Playboard.GrassPlayground;
import com.jme3.network.Message;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * Client simpleApplication for running the game.
 *
 * @author Rickard
 */

public class GameClient extends SimpleApplication implements ClientStateListener {
    // Listeners and senders
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
    protected Menu menu = new Menu();
    protected Game game = new Game();
    
    private final String hostname; // where the authentication server can be found
    private final int port; // the port att the server that we use
    
    //Gameinformation
    private int myPlayerID;
    private PlayerLite myPlayerLite;
    
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
        //setDisplayStatView(false);
        //setDisplayFps(false);
        menu.setEnabled(true);
        
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
        
        //menu.setEnabled(false);
        //game.setEnabled(true);
    }

    public void setServerList(Collection<GameServerLite> servers) {
        if (menu.isEnabled()) {
            menu.populateServerbrowser(servers);
        }
    }
    
    public void joinServer(GameServerLite server, String name) {
        toLobby();
        //putConfig(new ArrayList());
        if (gameConnection != null) {
            gameConnection.close();
            //TODO close the old senderThread here
            //Or do i just refuse to join a new server if im already connected?
        }
        try {
            System.out.println("Connect to a game server");
            //Initialize the queue to use to send informations
            outgoingGame = new LinkedBlockingQueue<>();
            
            gameConnection = Network.connectToServer(server.getAddress(), server.getPort());
            this.gameSender = new GameClientSender(gameConnection, outgoingGame);

            //Setup the listener for the game server
            gameListener = new GameClientListener(gameConnection, this);
            gameConnection.addMessageListener(gameListener,
                JoinAckMessage.class,
                GameConfigurationMessage.class,
                PlayerMessage.class);
            
            // finally start the communication channel to the server
            gameConnection.start();
            gameConnection.addClientStateListener(this);
            new Thread(gameSender).start();
            outgoingGame.put(new Util.JoinGameMessage(name));
        } catch (IOException ex) {
            ex.printStackTrace();
            this.destroy();
            this.stop();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public void joinTeam(int team) {
        try {
            outgoingGame.put(new Util.TeamJoinMessage(team));
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public void setPlayerID(int id) {
        this.myPlayerID = id;
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
        menu.gotoMenu();
        game.setEnabled(false);
    }
    
    public void toLobby() {
        menu.gotoLobby();
        game.setEnabled(true);
    }
    
    // Go to the game
    public void toGame() {
        menu.gotoHud();
    }
    
    // Setups the game by adding players, balls, etc.
    public void putConfig(ArrayList<PlayerLite> playerList){
        // Load the level
        game.initLevel("town");
        
        // Add players
        for (final PlayerLite player : playerList){
            System.out.println("player pos: "+player.getPosition());
            System.out.println("player dir: "+player.getDirection());
            game.addPlayer(player);
        }
        // Add the ball
        game.addBall();
        
        // Set the team scores
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
