/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.gameserver;

import Game.AttackControl;
import Game.Ball;
import Game.BallControl;
import Game.Game;
import Game.GrabControl;
import Game.Player;
import Network.Util;
import Network.Util.AttackMessage;
import Network.Util.BallInitInformation;
import Network.Util.BallPhysics;
import Network.Util.ChatMessage;
import Network.Util.DropBallMessage;
import Network.Util.GameConfigurationMessage;
import Network.Util.GrabBallMessage;
import Network.Util.JoinAckMessage;
import Network.Util.JoinGameMessage;
import Network.Util.JumpMessage;
import Network.Util.NewPlayerMessage;
import Network.Util.PlayerLite;
import Network.Util.PlayerMovementMessage;
import Network.Util.ShootBallMessage;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Lisens to packets from the gameClients
 * 
 * @author Henrik, Quentin, Rickard
 * Discussion, implementation
 */
public class GameServerListener implements MessageListener<HostedConnection> {
    private Util.BiMap<Integer, Player> connPlayerMap;
    private Game game;
    private Server server;
    private GameServer gameServer;

    public GameServerListener(Util.BiMap<Integer, Player> connPlayerMap, Game game, Server server, GameServer gameServer) {
        this.connPlayerMap = connPlayerMap;
        this.game = game;
        this.server = server;
        this.gameServer = gameServer;
    }

    @Override
    public void messageReceived(HostedConnection c, Message m) {
        if (m instanceof JoinGameMessage) {
            final JoinGameMessage msg = (JoinGameMessage) m;
            String name = msg.getName();

            if (connPlayerMap.size() >= Game.MAX_PLAYER_COUNT) {
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
            int blueScore = game.getScore(Util.BLUE_TEAM_ID);
            int redScore = game.getScore(Util.RED_TEAM_ID);
            BallInitInformation ball_phys = new BallInitInformation(ball.getPosition(), ball.getVelocity(), ball.getAngularVelocity(), ball.getIsOwned(), ball.getOwner());
            GameConfigurationMessage confMsg = new GameConfigurationMessage(players, ball_phys, blueScore, redScore);
            confMsg.setReliable(true);
            server.broadcast(Filters.equalTo(c), confMsg);

        } else if (m instanceof Util.TeamJoinMessage) {
            final Util.TeamJoinMessage msg = (Util.TeamJoinMessage) m;
            int team = msg.getTeam();

            if (connPlayerMap.containsKey(c.getId())) {
                Player player = connPlayerMap.get(c.getId());
                if (player.getTeam() != 0) {
                    //The player has already joined a team, do nothing.
                    //If we wanted to allow players to switch teams, we would need to remove the current player from the game, and spawn a new one
                    return;
                }
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
                //player.updateCatchZone(msg.getLocation(), msg.getDirection());
                GrabControl grab = new GrabControl(player.getCollisionShape(), server, gameServer, player, msg);
                grab.setPhysicsLocation(msg.getLocation());
                grab.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
                grab.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_02);
                game.getAppState().getPhysicsSpace().add(grab);
                game.getAppState().getPhysicsSpace().addTickListener(grab);
            }
        } else if (m instanceof Util.ShootBallMessage) {
            Util.ShootBallMessage msg = (Util.ShootBallMessage) m;
            Player player = connPlayerMap.get(c.getId());
            if (player.hasBall()) {
                gameServer.shootBall(msg.getPlayerId(), msg.getDirection(), msg.getPower());
                server.broadcast(msg);
            }

        } else if (m instanceof AttackMessage) {
            final AttackMessage msg = (AttackMessage) m;
            Player player = connPlayerMap.get(c.getId());
            if (!player.hasBall()) {
                server.broadcast(msg);
            }
            AttackControl attack = new AttackControl(player.getCollisionShape(), server, gameServer, player, msg);
            attack.setPhysicsLocation(msg.getLocation());
            //attack.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
            //attack.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_03);
            game.getAppState().getPhysicsSpace().add(attack);
            game.getAppState().getPhysicsSpace().addTickListener(attack);

        } else if (m instanceof ChatMessage) {
            final ChatMessage msg = (ChatMessage) m;
            final String message = msg.getMessage();
            if (message.length() > Util.MAX_MESSAGE_LENGTH || message.length() == 0) {
                return; //Invalid message length, ignore
            } else {
                server.broadcast(msg);
            }
        
        }

    }

}
