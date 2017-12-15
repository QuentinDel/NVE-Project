/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.gameserver;

import Game.Player;
import Network.Util;
import Network.Util.JoinAckMessage;
import Network.Util.JoinGameMessage;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;

/**
 *
 * @author Henrik
 */
public class GameServerListener implements MessageListener<HostedConnection> {
    private Util.BiMap<Integer, Player> connPlayerMap;

    public GameServerListener(Util.BiMap<Integer, Player> connPlayerMap) {
        this.connPlayerMap = connPlayerMap;
    }

    @Override
    public void messageReceived(HostedConnection c, Message m) {
        if (m instanceof JoinGameMessage) {
            final JoinGameMessage msg = (JoinGameMessage) m;
            String name = msg.getName();
            
            //Assign playerID
            boolean assigned = false;
            for (int i = 1; i<8; i++) {
                //If there is a free playerID, assign it to the new player
                boolean freeID = true;
                for (int j = 0; j < connPlayerMap.size(); j++) {
                    if (connPlayerMap.get(j).getId() == i) {
                        freeID = false;
                    }    
                }
                if (freeID) {
                    Player newPlayer = new Player(i, name);
                    connPlayerMap.put(c.getId(), newPlayer);
                    JoinAckMessage ackMsg = new JoinAckMessage(i);
                    c.send(ackMsg);
                    //game->get players
                    //GameConfigurationMessage confMsg = new GameConfigurationMessage(players);
                    //c.send(confMsg);
                    assigned = true;
                    break;
                }
            }
            if (!assigned) {
                // There was no open spot
                c.close("Try again later, the game is full");
            }
        }
        if (m instanceof Util.TeamJoinMessage) {
            final Util.TeamJoinMessage msg = (Util.TeamJoinMessage) m;
            int team = msg.getTeam();

            if (connPlayerMap.containsKey(c.getId())) {
                Player player = connPlayerMap.get(c.getId());
                if (team == 1 || team == 2) {
                    player.setTeam(team);
                    //send playermessage
                }

            }
        }

    }
    
}
