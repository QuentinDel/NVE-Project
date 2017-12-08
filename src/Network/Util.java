
package Network;

import com.jme3.math.Vector2f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;
import java.util.ArrayList;
import java.util.Collection;


public class Util {
    
    //public static final String HOSTNAME = "130.240.156.217";
    public static final String HOSTNAME = "127.0.0.1";
    
    public static final int PORT = 7006;
    
    public static final int RUNNING = 0;
    public static final int WAITING = 1;
    
    public static void initialiseSerializables() {
        Serializer.registerClasses(
            //GameServerLite.class,
            GameServerListsMessage.class,
            RefreshMessage.class,
            JoinGameMessage.class,
            JoinAckMessage.class,    
            LobbyInformationMessage.class,
           GameInformationMessage.class
        );
    }
    
     
    
    abstract public static class MyAbstractMessage extends AbstractMessage {


        protected int messageID;
        protected static int globalCounter = 1000;

        public MyAbstractMessage() {
            this.messageID = globalCounter++; // default messageID
        }

        public int getMessageID() {
            return messageID;
        }

    }
    
    
   
    @Serializable
    public static class GameServerListsMessage extends MyAbstractMessage {
        private Collection<GameServerLite> serversList;
        
        public GameServerListsMessage(){}
        
        public GameServerListsMessage(Collection<GameServerLite> serversList) {
            this.serversList = serversList;
        }
        
        
        public Collection<GameServerLite> getServersList() {
            return serversList;
        }
        
    }
    
    @Serializable
    public static class RefreshMessage extends MyAbstractMessage {
        
        public RefreshMessage() {
        }
        
    }
    
    @Serializable
    public static class JoinGameMessage extends MyAbstractMessage {
        
        public JoinGameMessage() {
        }
        
    }
    
    @Serializable
    public static class JoinAckMessage extends MyAbstractMessage {
        
        public JoinAckMessage() {
        }
        
    }
    
    @Serializable
    public static class LobbyInformationMessage extends MyAbstractMessage {
        
        public LobbyInformationMessage() {
        }
        
    }
    
    @Serializable
    public static class GameInformationMessage extends MyAbstractMessage {
        private GameServerLite gameServerInfo;
        
        public GameInformationMessage(){}
        
        public GameInformationMessage(GameServerLite gameServerInfo) {
            this.gameServerInfo = gameServerInfo;
        }
        
        public GameServerLite getGameServerInfo(){
            return gameServerInfo;
        }
    }
    
    
     
}
