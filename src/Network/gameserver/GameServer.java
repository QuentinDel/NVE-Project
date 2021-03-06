/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.gameserver;

import Game.Ball;
import Game.Game;
import Game.Player;
import Network.GameApplication;
import Network.Util;
import Network.Util.AttackMessage;
import Network.Util.BallPhysics;
import Network.Util.ChatMessage;
import Network.Util.GrabBallMessage;
import Network.Util.JoinGameMessage;
import Network.Util.JumpMessage;
import Network.Util.PlayerDisconnectedMessage;
import Network.Util.PlayerMovementMessage;
import Network.Util.PlayerPhysics;
import Network.Util.ScoreUpdateMessage;
import Network.Util.ShootBallMessage;
import Network.Util.TeamJoinMessage;
import Network.Util.UpdateBallPhysics;
import Network.Util.UpdatePhysics;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;


/**
 *
 * GameServer.
 * Is a client to the at authServer and a server to gameClients
 * This application keeps track of the true state of a game and broadcasts information to clients when it needs to
 * 
 * @author Henrik
 * Implementation of most parts
 * Discussion
 * bugfixes
 * 
 * @author Rickard
 * goaldetection
 * Discussion
 * bugfixes
 * 
 * @author Quentin
 * Discussion
 * bugfixes
 * 
 */
public class GameServer extends GameApplication implements ClientStateListener{
    private Server server;
    private Client auth;
    private final int port;
    private final String hostnameAuth;
    private GameToAuthSender authSender;
    private LinkedBlockingQueue<Message> outgoingAuth;
    private GameInformationGenerator gameInfoGen;
    private Util.BiMap<Integer, Player> connPlayerMap;
    
    private Game game = new Game();

    private static final long PHYSICS_UPDATE_SEND_RATE = 50;
    private static final long BALL_UPDATE_SEND_RATE = 30;
    private static final long SCORE_UPDATE_SEND_RATE = 1;
    
    public static void main(String[] args) {
        System.out.println("Server initializing");
        Util.initialiseSerializables();
        new GameServer(Util.PORT_GAME, Util.HOSTNAME).start(JmeContext.Type.Headless);
    }
    
    public GameServer(int port, String hostnameAuth) {
        this.port = port;
        this.hostnameAuth = hostnameAuth;
        this.connPlayerMap = new Util.BiMap();

        this.stateManager.attach(game);
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void simpleInitApp() {
        try {
            /* Start gameserver */
            System.out.println("Using port " + port);
            server = Network.createServer(port);
            server.start();
            System.out.println("Server started");

            /* Connect to auth server */
            auth = Network.connectToServer(hostnameAuth, Util.PORT);
            auth.start();
            auth.addClientStateListener(this);

        } catch (IOException ex) {
            ex.printStackTrace();
            destroy();
            this.stop();
        }
        /* add auth sender */
        outgoingAuth = new LinkedBlockingQueue<>();
        authSender = new GameToAuthSender(auth, outgoingAuth);
        new Thread(authSender).start();
        /* add game info generator */
        gameInfoGen = new GameInformationGenerator(this, outgoingAuth, connPlayerMap);
        new Thread(gameInfoGen).start();

        /* add connection listener for clients */
        server.addConnectionListener(new ClientConnectionListener(connPlayerMap, this));

        /* add message listener */
        server.addMessageListener(new GameServerListener(connPlayerMap, game, server, this),
                                  JoinGameMessage.class,
                                  TeamJoinMessage.class,
                                  PlayerMovementMessage.class,
                                  JumpMessage.class,
                                  GrabBallMessage.class,
                                  ShootBallMessage.class,
                                  AttackMessage.class,
                                  ChatMessage.class);
        game.setLevel("playground");
        game.setEnabled(true);
        Timer physicsUpdateTimer = new Timer(true);
        physicsUpdateTimer.scheduleAtFixedRate(
            new TimerTask() {
                public void run() {
                    physicsUpdate();
                }
            }, 0, PHYSICS_UPDATE_SEND_RATE);
        Timer ballUpdateTimer = new Timer(true);
        ballUpdateTimer.scheduleAtFixedRate(
            new TimerTask() {
                public void run() {
                    ballUpdate();
                }
            }, 0, BALL_UPDATE_SEND_RATE);
        Timer scoreUpdateTimer = new Timer(true);
        scoreUpdateTimer.scheduleAtFixedRate(
            new TimerTask() {
                public void run() {
                    scoreUpdate();
                }
            }, 0, SCORE_UPDATE_SEND_RATE);
    }
    
    @Override
    public void onGoal(int teamID) {
        game.resetBall();
        game.incrementScore(teamID);
        scoreUpdate();
    }
    
    @Override
    public void grabBall(int playerID) {
        game.setBallToPlayer(playerID);
    }
    
    @Override
    public void shootBall(int playerID, Vector3f direction, float power) {
        game.removeBallToPlayer(playerID, false);
        
        Ball ball = game.getBall();
        ball.setVelocity(direction.mult(power));
    }

    @Override
    public void dropBall(int playerID) {
        Util.DropBallMessage dropMsg = new Util.DropBallMessage(playerID);
        server.broadcast(dropMsg);
        game.removeBallToPlayer(playerID, false);
    }
    
    public void removePlayer(int playerID) {
        game.removePlayer(playerID);
        
        PlayerDisconnectedMessage msg = new PlayerDisconnectedMessage(playerID);
        server.broadcast(msg);
    }

    @Override
    public void simpleUpdate(float tpf) {
        
    }

    @Override
    public void clientConnected(Client c) {
        System.out.println("Connected to auth server");
    }

    @Override
    public void clientDisconnected(Client c, DisconnectInfo info) {
        System.out.println(info);
    }

    public void physicsUpdate() {
        Enumeration<Player> values = connPlayerMap.values();
        ArrayList<PlayerPhysics> players = new ArrayList<>();
        while (values.hasMoreElements()) {
            Player p = values.nextElement();
            if (p.getTeam() == 1 || p.getTeam() == 2) {
                players.add(new PlayerPhysics(p.getId(), p.getDirection(), p.getVelocity(), p.getPosition()));
            }
        }
        if (!players.isEmpty()) {
            UpdatePhysics msg = new UpdatePhysics(players);
            server.broadcast(msg);
        }

    }
    
    public void ballUpdate() {
        Ball ball = game.getBall();
        if (ball != null) {
            BallPhysics ball_phy = new BallPhysics(ball.getPosition(), ball.getVelocity(), ball.getAngularVelocity());
            UpdateBallPhysics msg = new UpdateBallPhysics(ball_phy);
            server.broadcast(msg);
        }
    }
    
    public void scoreUpdate() {
        int blueScore = game.getScore(Util.BLUE_TEAM_ID);
        int redScore = game.getScore(Util.RED_TEAM_ID);
        ScoreUpdateMessage msg = new ScoreUpdateMessage(blueScore, redScore);
        server.broadcast(msg);
    }
    
    public int getPort(){
        return port;
    }
    
    public Game getGame(){
        return game;
    }
    
    public ArrayList<HostedConnection> activeConnections() {
        ArrayList<HostedConnection> conns = new ArrayList();
        Enumeration<Integer> keys = connPlayerMap.keys();
        while (keys.hasMoreElements()) {
            Integer key = keys.nextElement();
            conns.add(server.getConnection(key));
        }
        return conns;
    }

}
