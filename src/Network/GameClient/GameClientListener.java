/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Network.GameClient;

import Game.Player;
import Network.Util;
import Network.Util.BallPhysics;
import Network.Util.GameConfigurationMessage;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import Network.Util.JoinAckMessage;
import Network.Util.JumpMessage;
import Network.Util.LobbyInformationMessage;
import Network.Util.NewPlayerMessage;
import Network.Util.PlayerLite;
import Network.Util.PlayerMessage;
import Network.Util.PlayerPhysics;
import Network.Util.UpdatePhysics;

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
        } else if (m instanceof PlayerMessage) {
            final PlayerMessage msg = (PlayerMessage) m;
            final PlayerLite myPlayer = msg.getPlayer();
            gameClient.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {
                    Player myPlayerNode = gameClient.game.addLocalPlayer(myPlayer);
                    gameClient.toGame();
                    gameClient.move.setPlayer(myPlayerNode);
                    return true;
                }
            });
        } else if (m instanceof JumpMessage) {
            final JumpMessage msg = (JumpMessage) m;
            final int playerID = msg.getId();
            gameClient.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {
                    gameClient.game.makeJump(playerID);
                    return true;
                }
            });
        } else if (m instanceof NewPlayerMessage) {
            final NewPlayerMessage msg = (NewPlayerMessage) m;
            gameClient.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {
                    gameClient.game.addPlayer(msg.getPlayer());
                    return true;
                }
            });
        } else if (m instanceof UpdatePhysics) {
            final UpdatePhysics msg = (UpdatePhysics) m;
            final ArrayList<PlayerPhysics> physics = msg.getPlayersPhys();
            final BallPhysics ball_physics = msg.getBallPhys();
            gameClient.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {
                    gameClient.game.updatePlayerPhysics(physics);
                    gameClient.game.updateBallPhysics(ball_physics);
                    return true;
                }
            });
        }
    }
}