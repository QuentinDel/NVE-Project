/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.gameserver;

import Game.Ball;
import Game.Game;
import Game.Player;
import Network.Util;
import Network.Util.BallPhysics;
import Network.Util.GrabBallMessage;
import Network.Util.JoinGameMessage;
import Network.Util.JumpMessage;
import Network.Util.PlayerMovementMessage;
import Network.Util.PlayerPhysics;
import Network.Util.TeamJoinMessage;
import Network.Util.UpdatePhysics;
import com.jme3.app.SimpleApplication;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Filters;
import com.jme3.network.Message;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;


/**
 *
 * @author Henrik
 */
public class GameServer extends SimpleApplication implements ClientStateListener{
    private Server server;
    private Client auth;
    private final int port;
    private final String hostnameAuth;
    private GameToAuthSender authSender;
    private LinkedBlockingQueue<Message> outgoingAuth;
    private GameInformationGenerator gameInfoGen;
    private Util.BiMap<Integer, Player> connPlayerMap;
    
    private Game game = new Game();

    private static final long PHYSICS_UPDATE_SEND_RATE = 30;
    
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
            System.out.println("No good");
            ex.printStackTrace();
            destroy();
            this.stop();
        }
        /* add auth sender */
        outgoingAuth = new LinkedBlockingQueue<>();
        authSender = new GameToAuthSender(auth, outgoingAuth);
        new Thread(authSender).start();
        /* add game info generator */
        // TODO: it needs some information
        gameInfoGen = new GameInformationGenerator(outgoingAuth, connPlayerMap);
        new Thread(gameInfoGen).start();
        // TODO: add auth listener (if we need one?)

        /* add connection listener for clients */
        server.addConnectionListener(new ClientConnectionListener(connPlayerMap));

        /* add message listener */
        server.addMessageListener(new GameServerListener(connPlayerMap, game, server),
                                  JoinGameMessage.class,
                                  TeamJoinMessage.class,
                                  PlayerMovementMessage.class,
                                  JumpMessage.class,
                                  GrabBallMessage.class);
        game.setLevel("playground");
        game.setEnabled(true);
        Timer physicsUpdateTimer = new Timer(true);
        physicsUpdateTimer.scheduleAtFixedRate(
            new TimerTask() {
                public void run() {
                    physicsUpdate();
                }
            }, 0, PHYSICS_UPDATE_SEND_RATE);
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
                players.add(new PlayerPhysics(p.getId(), p.getDirection(), p.getVelocity()));
            }
        }
        Ball ball = game.getBall();
        if (!players.isEmpty()) {
            if (ball != null) {
                BallPhysics ball_phy = new BallPhysics(ball.getPosition(), ball.getVelocity(), ball.getAngularVelocity());
                UpdatePhysics msg = new UpdatePhysics(players, ball_phy);
                server.broadcast(msg);
            } else {
                UpdatePhysics msg = new UpdatePhysics(players);
                server.broadcast(msg);
            }
        }

    }

}
