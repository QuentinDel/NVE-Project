package Network.AuthServer;

import Network.Util;
import Network.Util.GameServerListsMessage;
import Network.Util.GameServerLite;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listens for connections by gameServers and gameClients
 * 
 * @author Quentin
 */

// this class provides a handler for incoming HostedConnections
class AuthConnectionListener implements ConnectionListener {
    private Server server;
    private LinkedBlockingQueue<Callable> outgoing;
    private ConcurrentHashMap< Integer, GameServerLite > gamingServerInfos;
            
    AuthConnectionListener(Server server, ConcurrentHashMap< Integer, GameServerLite > gamingServerInfos, LinkedBlockingQueue<Callable> outgoing){
        this.server = server;
        this.gamingServerInfos = gamingServerInfos;
        this.outgoing = outgoing;

    }
    
    @Override
    public void connectionAdded(Server s, final HostedConnection c) {
        try {
            outgoing.put(new Callable() {
                @Override
                public Object call() throws Exception {
                    ArrayList<GameServerLite> serversList = new ArrayList<>();
                    serversList.addAll(gamingServerInfos.values());
                    Util.MyAbstractMessage msg = new GameServerListsMessage(serversList);
                    msg.setReliable(true);
                    server.broadcast(Filters.in(c), msg);

                    return true;
                }
            });        
        } catch (InterruptedException ex) {
            Logger.getLogger(AuthConnectionListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void connectionRemoved(Server s, HostedConnection c) {
        System.out.println("Client #"+c.getId() + " has disconnected from the server address ");
        if (gamingServerInfos.containsKey(c.getId())){
            gamingServerInfos.remove(c.getId());    
        }
    }
}