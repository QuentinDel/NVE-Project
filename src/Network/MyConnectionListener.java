package Network;

import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;

// this class provides a handler for incoming HostedConnections
class MyConnectionListener implements ConnectionListener {
    @Override
    public void connectionAdded(Server s, HostedConnection c) {
        System.out.println("Client #"+c.getId() + " has connected to the server");

        
    }
    
    
    @Override
    public void connectionRemoved(Server s, HostedConnection c) {
        System.out.println("Client #"+c.getId() + " has disconnected from the server");
        //This removes the player from the list of used playerIDs
     
    }
}