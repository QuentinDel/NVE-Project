/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.gameserver;

import Game.Game;
import Game.Player;
import Network.Util;
import Network.Util.GameConfigurationMessage;
import Network.Util.JoinAckMessage;
import Network.Util.JoinGameMessage;
import Network.Util.PlayerLite;
import Network.Util.PlayerMovement;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 *
 * @author Henrik
 */
public class GameServerListener implements MessageListener<HostedConnection> {
    private Util.BiMap<Integer, Player> connPlayerMap;
    private Game game;
    private Server server;

    public GameServerListener(Util.BiMap<Integer, Player> connPlayerMap, Game game, Server server) {
        this.connPlayerMap = connPlayerMap;
        this.game = game;
        this.server = server;
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
                    ackMsg.setReliable(true);
                    //c.send(ackMsg);
                    server.broadcast(Filters.equalTo(c), ackMsg);
                    Enumeration<Player> values = connPlayerMap.values();
                    ArrayList<PlayerLite> players = new ArrayList<>();
                    while (values.hasMoreElements()) {
                        Player p = values.nextElement();
                        if (p.getTeam() == 1 || p.getTeam() == 2) {
                            players.add(new PlayerLite(p));
                        }
                    }
                    GameConfigurationMessage confMsg = new GameConfigurationMessage(players);
                    confMsg.setReliable(true);
                    //c.send(confMsg);
                    server.broadcast(Filters.equalTo(c), confMsg);
                    assigned = true;
                    break;
                }
            }
            if (!assigned) {
                // There was no open spot
                c.close("Try again later, the game is full");
            }
        } else if (m instanceof Util.TeamJoinMessage) {
            final Util.TeamJoinMessage msg = (Util.TeamJoinMessage) m;
            int team = msg.getTeam();

            if (connPlayerMap.containsKey(c.getId())) {
                Player player = connPlayerMap.get(c.getId());
                if (team == 1 || team == 2) {
                    player.setTeam(team);
                    game.addPlayer(new PlayerLite(player));
                    Util.PlayerMessage pMsg = new Util.PlayerMessage(new PlayerLite(player));
                    pMsg.setReliable(true);
                    //c.send(pMsg);
                    server.broadcast(Filters.equalTo(c), pMsg);
                }

            }
        } else if (m instanceof PlayerMovement) {
            // Some checks needed here maybe?
            final PlayerMovement msg = (PlayerMovement) m;
            Vector3f velocity = msg.getVelocity();
            Vector3f viewDir = msg.getViewDirection();
            Player player = connPlayerMap.get(c.getId());
            player.setDirection(viewDir);
            player.setVelocity(velocity);
        }

    }
    
}
