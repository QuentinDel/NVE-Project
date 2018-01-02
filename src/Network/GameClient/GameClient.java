/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.GameClient;

import Game.Ball;
import Game.Game;
import Game.PlayerMovement;
import Network.Util.GameServerLite;
import Network.Util;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Network;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import Network.Util.*;
import Game.Menu;
import Game.Player;
import Network.GameApplication;
import com.jme3.math.Vector3f;
import com.jme3.network.Message;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * Client simpleApplication for running the game.
 *
 * @author Rickard
 */

public class GameClient extends GameApplication implements ClientStateListener {
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
    protected PlayerMovement move = new PlayerMovement();
    protected Game game = new Game();
    
    private final String hostname; // where the authentication server can be found
    private final int port; // the port att the server that we use
    
    //Gameinformation
    private int myPlayerID;
    private PlayerLite myPlayerLite;
    
    public GameClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.setPauseOnLostFocus(false);
        
        this.stateManager.attach(menu);
        this.stateManager.attach(game);
        this.stateManager.attach(move);
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
        move.setEnabled(true);
        
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
        game.setLevel(server.getIdMap());
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
                PlayerMessage.class,
                JumpMessage.class,
                NewPlayerMessage.class,
                UpdatePhysics.class,
                UpdateBallPhysics.class,
                ScoreUpdateMessage.class,
                GrabBallMessage.class,
                ShootBallMessage.class);
            
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
    
    public int getPlayerID() {
        return myPlayerID;
    }
    
    public void queueGameServerMessage(MyAbstractMessage msg) {
        try {
            this.outgoingGame.put(msg);
        }
        catch(InterruptedException ex) {
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
    public void putConfig(GameConfigurationMessage msg){
        // Add players
        ArrayList<PlayerLite> playerList = msg.getPlayers();
        for (final PlayerLite player : playerList){
            System.out.println("player pos: "+player.getPosition());
            System.out.println("player dir: "+player.getDirection());
            game.addPlayer(player);
        }
        // Update the ball
        BallPhysics ball = msg.getBall();
        game.updateBallPhysics(ball);
        
        // Set the team scores
        int blueScore = msg.getBlueScore();
        int redScore = msg.getRedScore();
        updateScores(blueScore, redScore);
    }
    
    public void updateScores(int blueScore, int redScore) {
        game.setScore(Util.BLUE_TEAM_ID, blueScore);
        game.setScore(Util.RED_TEAM_ID, redScore);
        menu.setScore(Util.BLUE_TEAM_ID, blueScore);
        menu.setScore(Util.RED_TEAM_ID, redScore);
    }
    
    @Override
    public void onGoal(int teamID) {
        game.resetBall();
        game.incrementScore(teamID);
        int currentScore = game.getScore(teamID);
        menu.setScore(teamID, currentScore);
    }
    
    @Override
    public void grabBall(int playerID) {
        game.setBallToPlayer(playerID);
        move.insertLoadBar();
    }
    
    @Override
    public void shootBall(int playerID, Vector3f direction, float power) {
        game.removeBallToPlayer(playerID);
        move.removeLoadBar();
        
        Ball ball = game.getBall();
        ball.setVelocity(direction.mult(power));
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
