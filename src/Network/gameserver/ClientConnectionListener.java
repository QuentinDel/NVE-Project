/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.gameserver;

import Game.Player;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import Network.Util;
import Network.Util.JoinAckMessage;

/**
 *
 * @author Henrik
 */
public class ClientConnectionListener implements ConnectionListener {
    private Util.BiMap<Integer, Player> connPlayerMap;
    private GameServer gameServer;

    public ClientConnectionListener(Util.BiMap<Integer, Player> connPlayerMap, GameServer gameServer) {
        this.connPlayerMap = connPlayerMap;
        this.gameServer = gameServer;
    }

    @Override
    public void connectionAdded(Server server, HostedConnection c) {
        System.out.println("Client #"+c.getId() + " has connected to the server");
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection c) {
        if (connPlayerMap.containsKey(c.getId())) {
            connPlayerMap.remove(c.getId());
            gameServer.removePlayer(c.getId());
        }
        System.out.println("Client #"+c.getId() + " has disconnected from the server");
    }
    
}
