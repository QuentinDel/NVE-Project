
package Network;

import com.jme3.math.Vector2f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;
import java.util.ArrayList;


public class Util {
    
    //public static final String HOSTNAME = "130.240.156.217";
    public static final String HOSTNAME = "127.0.0.1";
    
    public static final int PORT = 7006;
    
    public static void initialiseSerializables() {
        Serializer.registerClasses(
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
    
    /**
     * Used for now to send a collection of data for a player
     */
    @Serializable
    public static class ServerLite{
            
    }
    
   
    @Serializable
    public static class GameServerListsMessage extends MyAbstractMessage {
        private ArrayList<ServerLite> serversList;
        
        public GameServerListsMessage() {
        }
        
        
        public ArrayList<ServerLite> getServersList() {
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
        
        public GameInformationMessage() {
        }
        
    }
    
    
    
}
