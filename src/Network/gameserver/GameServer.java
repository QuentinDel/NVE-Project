/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.gameserver;

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
    
    public static void main(String[] args) {
        System.out.println("Server initializing");
        Util.initialiseSerializables();
        new GameServer(Util.PORT, Util.HOSTNAME).start(JmeContext.Type.Headless);
    }
    
    public GameServer(int port, String hostnameAuth) {
        this.port = port;
        this.hostnameAuth = hostnameAuth;
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void simpleInitApp() {
        try {
            /* Connect to auth server */
            auth = Network.connectToServer(hostnameAuth, port); 
            auth.start();
            auth.addClientStateListener(this);
            
            /* Start gameserver */
            System.out.println("Using port " + port);
            server = Network.createServer(port);
            server.start();
            System.out.println("Server started");
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
        gameInfoGen = new GameInformationGenerator(outgoingAuth);
        new Thread(gameInfoGen).start();
        // TODO: add auth listener (if we need one?)
        // TODO: add connection listener for clients
        // TODO: add listener for clients
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
