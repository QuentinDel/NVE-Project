/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.GameClient;

import Network.Util;
import Network.Util.GameConfigurationMessage;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import Network.Util.JoinAckMessage;
import Network.Util.LobbyInformationMessage;
import Network.Util.PlayerLite;

/**
 * Listener for Packets from the Game server
 * This class is separated from the GameClientAuthListener because 
 * communication with the two different servers uses different packets.
 * This separation should result in a slight performance boost (less if statements)
 * and improve readability
 * 
 * @author Rickard
 */
public class GameClientListener implements MessageListener<Client>{
    
    private Client serverConnection;
    private GameClient gameClient;
    
    public GameClientListener(Client serverConn, GameClient gameClient) {
        serverConnection = serverConn;
        this.gameClient = gameClient;
                
    }     

    // this method is called whenever network packets arrive
    @Override
    public void messageReceived(Client source, Message m) {
        if (m instanceof JoinAckMessage) {
            final JoinAckMessage msg = (JoinAckMessage) m;
            final int myID = msg.getId();
            gameClient.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {
                    gameClient.setPlayerID(myID);
                    return true;
                }
            });
            
        } else if (m instanceof GameConfigurationMessage) {
            final GameConfigurationMessage msg = (GameConfigurationMessage) m;
            final ArrayList<PlayerLite> players = msg.getPlayers();
            gameClient.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {
                    gameClient.putConfig(players);
                    return true;
                }
            });
            
        }
    }
}