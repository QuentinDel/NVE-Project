/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.gameserver;

import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import Network.Util;

/**
 *
 * @author Henrik
 */
public class ClientConnectionListener implements ConnectionListener {
    private Util.BiMap<Integer,Integer> connPlayerMap;

    public ClientConnectionListener(Util.BiMap<Integer,Integer> connPlayerMap) {
        this.connPlayerMap = connPlayerMap;
    }

    @Override
    public void connectionAdded(Server server, HostedConnection c) {
        //Assign playerID
        boolean assigned = false;
        for (int i = 1; i<8; i++) {
            //If there is a free playerID, assign it to the new player
            if (connPlayerMap.containsValue(i)) {
                connPlayerMap.put(c.getId(), i);
                assigned = true;
                break;
            }
        }
        if (!assigned) {
            // There was no open spot
            c.close("Try again later, the game is full");
        }
        System.out.println("Client #"+c.getId() + " has connected to the server");
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection c) {
        if (connPlayerMap.get(c.getId()) != null) {
            connPlayerMap.remove(c.getId());
        }
        System.out.println("Client #"+c.getId() + " has disconnected from the server");
    }
    
}
