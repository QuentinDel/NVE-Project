/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.AuthServer;

import Network.GameServerLite;
import Network.Util;
import Network.Util.GameInformationMessage;
import com.jme3.math.Vector2f;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
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
    
    private ConcurrentHashMap< String, GameServerLite > gamingServerInfos;
    private LinkedBlockingQueue<Callable> outgoing;
    
    public AuthServerListener(ConcurrentHashMap< String, GameServerLite > gamingServerInfos, LinkedBlockingQueue<Callable> outgoing){
        this.gamingServerInfos = gamingServerInfos;
        this.outgoing = outgoing;
    }


    @Override
    public void messageReceived(HostedConnection source, Message m) {
        
        if (m instanceof Network.Util.GameInformationMessage){
            GameInformationMessage msg = (GameInformationMessage) m;
            gamingServerInfos.put(msg.getGameServerInfo().getAddress(), msg.getGameServerInfo());
        }
        
        
        if (m instanceof Network.Util.RefreshMessage) {
            try {
                outgoing.put(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        
                        //AuthServer.server.broadcast(msg);
                        return true;
                    }
                });
            } catch (InterruptedException ex) {
                Logger.getLogger(AuthServerListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

          
    }
}