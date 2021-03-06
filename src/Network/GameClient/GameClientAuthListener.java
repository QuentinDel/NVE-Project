/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.GameClient;

import Game.Game;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import java.util.concurrent.Callable;
import Network.Util.GameServerListsMessage;
import Network.Util.GameServerLite;
import java.util.ArrayList;

/**
 * Listener for Packets from the Authentication server
 * This class is separated from the GameClientListener because 
 * communication with the two different servers uses different packets.
 * This separation should result in a slight performance boost (less if statements)
 * and improve readability
 * 
 * @author Rickard
 */
public class GameClientAuthListener implements MessageListener<Client>{
    
    private Client serverConnection;
    private GameClient gameClient;
    private Game game;
    
    public GameClientAuthListener(Client serverConn, GameClient gameClient) {
        serverConnection = serverConn;
        this.gameClient = gameClient;
        this.game = gameClient.game;
                
    }     

    // this method is called whenever network packets arrive
    @Override
    public void messageReceived(Client source, Message m) {
        if (m instanceof GameServerListsMessage) {
            
            final GameServerListsMessage msg = (GameServerListsMessage) m;
            final ArrayList<GameServerLite> servers = msg.getServersList();
            
            gameClient.enqueue(new Callable() {
                @Override
                public Object call() {
                    gameClient.setServerList(servers);
                    return true;
                }
            });
        } 
    }
}
