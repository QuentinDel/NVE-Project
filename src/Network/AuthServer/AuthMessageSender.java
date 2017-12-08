/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.AuthServer;

import Network.GameServerLite;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Quentin
 */
public class AuthMessageSender implements Runnable {
    
    private ConcurrentHashMap< String, GameServerLite > gamingServerInfos;

    @Override
    public void run() {
        while(true){
            
        }
    }

    
}
