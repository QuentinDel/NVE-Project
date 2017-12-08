package Network.AuthServer;

import Network.GameServerLite;
import Network.Util;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filter;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

// this class provides a handler for incoming HostedConnections
class AuthConnectionListener implements ConnectionListener {
    private Server server;
    private LinkedBlockingQueue<Callable> outgoing;
    private ConcurrentHashMap< String, GameServerLite > gamingServerInfos;

            
    AuthConnectionListener(Server server, ConcurrentHashMap< String, GameServerLite > gamingServerInfos, LinkedBlockingQueue<Callable> outgoing){
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
                    Util.MyAbstractMessage msg = new Util.GameServerListsMessage(gamingServerInfos.values());
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
        System.out.println("Client #"+c.getId() + " has disconnected from the server");
        if (gamingServerInfos.contains(c.getAddress()))
            gamingServerInfos.remove(c.getAddress());    
    }
}