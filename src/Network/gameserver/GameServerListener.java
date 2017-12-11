/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.gameserver;

import Network.Util.JoinGameMessage;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;

/**
 *
 * @author Henrik
 */
public class GameServerListener implements MessageListener<HostedConnection> {

    @Override
    public void messageReceived(HostedConnection source, Message m) {
        if (m instanceof JoinGameMessage) {
        }

    }
    
}
