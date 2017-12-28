/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.gameserver;

import Game.Ball;
import Game.Game;
import Game.Player;
import Network.Util;
import Network.Util.BallPhysics;
import Network.Util.GameConfigurationMessage;
import Network.Util.GrabBallMessage;
import Network.Util.JoinAckMessage;
import Network.Util.JoinGameMessage;
import Network.Util.JumpMessage;
import Network.Util.NewPlayerMessage;
import Network.Util.PlayerLite;
import Network.Util.PlayerMovementMessage;
import com.jme3.bullet.control.BetterCharacterControl;
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

            if (connPlayerMap.size() > Game.MAX_PLAYER_COUNT) {
                c.close("Try again later, the game is full");
                return;
            }
            //Assign playerID
            Player newPlayer = new Player(c.getId(), name);
            connPlayerMap.put(c.getId(), newPlayer);
            JoinAckMessage ackMsg = new JoinAckMessage(c.getId());
            ackMsg.setReliable(true);
            server.broadcast(Filters.equalTo(c), ackMsg);

            //Send GameConfigurationMessage to new client
            Enumeration<Player> values = connPlayerMap.values();
            ArrayList<PlayerLite> players = new ArrayList<>();

            while (values.hasMoreElements()) {
                Player p = values.nextElement();
                if (p.getTeam() == 1 || p.getTeam() == 2) {
                    players.add(new PlayerLite(p));
                }
            }
            Ball ball = game.getBall();
            BallPhysics ball_phys = new BallPhysics(ball.getPosition(), ball.getVelocity(), ball.getAngularVelocity());
            GameConfigurationMessage confMsg = new GameConfigurationMessage(players, ball_phys);
            confMsg.setReliable(true);
            server.broadcast(Filters.equalTo(c), confMsg);

        } else if (m instanceof Util.TeamJoinMessage) {
            final Util.TeamJoinMessage msg = (Util.TeamJoinMessage) m;
            int team = msg.getTeam();

            if (connPlayerMap.containsKey(c.getId())) {
                Player player = connPlayerMap.get(c.getId());
                if (team == 1 || team == 2) {
                    player.setTeam(team);
                    Player p = game.addPlayer(new PlayerLite(player));
                    if (p != null) {
                        connPlayerMap.remove(c.getId());
                        connPlayerMap.put(c.getId(), p);
                        /** Send a message to the new client with its player info */
                        Util.PlayerMessage pMsg = new Util.PlayerMessage(new PlayerLite(p));
                        pMsg.setReliable(true);
                        server.broadcast(Filters.equalTo(c), pMsg);
                        /** Send a message all existing clients about a new player joining */
                        NewPlayerMessage newPlayerMsg = new NewPlayerMessage(new PlayerLite(p));
                        newPlayerMsg.setReliable(true);
                        server.broadcast(Filters.notEqualTo(c), newPlayerMsg);    
                    } else {
                        c.close("Failed to spawn player");
                    }
                    
                }

            }
        } else if (m instanceof PlayerMovementMessage) {
            // Some checks needed here maybe?
            final PlayerMovementMessage msg = (PlayerMovementMessage) m;
            Vector3f velocity = msg.getVelocity();
            Vector3f viewDir = msg.getViewDirection();
            Player player = connPlayerMap.get(c.getId());
            player.setDirection(viewDir);
            player.setVelocity(velocity);

        } else if (m instanceof JumpMessage) {
            final JumpMessage msg = (JumpMessage) m;
            Player player = connPlayerMap.get(c.getId());
            player.getControl(BetterCharacterControl.class).jump();
            JumpMessage jMsg = new JumpMessage(player.getId());
            server.broadcast(Filters.notEqualTo(c), jMsg);
            
        } else if (m instanceof GrabBallMessage) {
            final GrabBallMessage msg = (GrabBallMessage) m;
            Player player = connPlayerMap.get(c.getId());
            Ball ball = game.getBall();
            if (ball.getIsOwned()) {
                //ball is owned by someone, do nothing
            } else {
                //check if ball is in range
                System.out.println("Ball Grabbed");
                game.setBallToPlayer(msg.getId());
                server.broadcast(new GrabBallMessage(player.getId()));
            }
        } else if (m instanceof Util.ShootBallMessage) {
            Util.ShootBallMessage msg = (Util.ShootBallMessage) m;
            game.removeBallToPlayer(msg.getPlayerId());
            server.broadcast(msg);
        }

    }
    
}
