/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.gameserver;

import Game.Game;
import Game.Player;
import Network.Util;
import com.jme3.app.SimpleApplication;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.system.JmeContext;
import java.io.IOException;
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
    private Game game;
    
    public static void main(String[] args) {
        System.out.println("Server initializing");
        Util.initialiseSerializables();
        new GameServer(Util.PORT_GAME, Util.HOSTNAME).start(JmeContext.Type.Headless);
    }
    
    public GameServer(int port, String hostnameAuth) {
        this.port = port;
        this.hostnameAuth = hostnameAuth;
        this.connPlayerMap = new Util.BiMap();
        this.game = new Game();

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
                                  Util.JoinGameMessage.class,
                                  Util.TeamJoinMessage.class);
        game.setEnabled(true);
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

}
