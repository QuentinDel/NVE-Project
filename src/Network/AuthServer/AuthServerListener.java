/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.AuthServer;

import Network.Util.GameServerLite;
import Network.Util;
import Network.Util.GameInformationMessage;
import com.jme3.math.Vector2f;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Quentin
 */
public class AuthServerListener implements MessageListener<HostedConnection> {
    
    private Server server;
    private ConcurrentHashMap< Integer, GameServerLite > gamingServerInfos;
    private LinkedBlockingQueue<Callable> outgoing;
    
    public AuthServerListener(Server server, ConcurrentHashMap< Integer, GameServerLite > gamingServerInfos, LinkedBlockingQueue<Callable> outgoing){
        this.server = server;
        this.gamingServerInfos = gamingServerInfos;
        this.outgoing = outgoing;
    }


    @Override
    public void messageReceived(final HostedConnection source, Message m) {
        
        if (m instanceof Network.Util.GameInformationMessage){
            System.out.println("Message received");
            GameInformationMessage msg = (GameInformationMessage) m;
            gamingServerInfos.put(source.getId(), msg.getGameServerInfo());
            System.out.println(source.getId());
        }
        
        
        if (m instanceof Network.Util.RefreshMessage) {
            try {
                outgoing.put(new Callable() {
                @Override
                public Object call() throws Exception {
                    ArrayList<GameServerLite> serversList = new ArrayList<>();
                    serversList.addAll(gamingServerInfos.values());
                    System.out.println(serversList.size());

                    Util.GameServerListsMessage msg = new Util.GameServerListsMessage(serversList);
                    msg.setReliable(true);
                    server.broadcast(Filters.equalTo(source), msg);
                    return true;
                }
            });        
            } catch (InterruptedException ex) {
                Logger.getLogger(AuthServerListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

          
    }
}