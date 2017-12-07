/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network;

import com.jme3.math.Vector2f;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Quentin
 */
public class AuthServerListener implements MessageListener<HostedConnection> {
    @Override
    public void messageReceived(HostedConnection source, Message m) {
        if (m instanceof Network.Util.RefreshMessage) {
           
        }

          
    }
}