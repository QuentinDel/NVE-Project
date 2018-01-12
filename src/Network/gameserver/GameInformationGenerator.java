/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.gameserver;

import Game.Player;
import Network.Util;
import Network.Util.GameInformationMessage;
import com.jme3.network.Message;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates and sends information about the server to the auth server
 *
 * @author Henrik, Quentin
 * Implementation
 */
public class GameInformationGenerator implements Runnable {
    LinkedBlockingQueue<Message> outgoing;
    Util.GameServerLite serverInfo;
    private Util.BiMap<Integer, Player> connPlayerMap;
    private GameServer gameServer;

    public GameInformationGenerator(GameServer gameServer, LinkedBlockingQueue<Message> outgoing, Util.BiMap<Integer, Player> connPlayerMap) {
        this.gameServer = gameServer;
        this.outgoing = outgoing;
        this.connPlayerMap = connPlayerMap;
    }

    @Override
    public void run() {
        while (true) {
            //gather information
            serverInfo = new Util.GameServerLite(Util.GAME_HOSTNAME, gameServer.getGame().getLevelId(), gameServer.getPort(), 
                    connPlayerMap.size(), 1, gameServer.getGame().getScore(Util.BLUE_TEAM_ID), gameServer.getGame().getScore(Util.RED_TEAM_ID));

            //send it to outgoing queue
            GameInformationMessage msg = new GameInformationMessage(serverInfo);
            try {
                outgoing.put(msg);
            } catch (InterruptedException ex) {
                Logger.getLogger(GameInformationGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
            //sleep
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(GameInformationGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
