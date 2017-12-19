
package Network;

import Game.Player;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;


public class Util {
    
    //public static final String HOSTNAME = "130.240.156.217";
    public static final String HOSTNAME = "127.0.0.1";
    
    public static final int PORT = 7006;
    public static final int PORT_GAME = 7007;
    
    public static final int RUNNING = 0;
    public static final int WAITING = 1;
    
    public static void initialiseSerializables() {
        Serializer.registerClasses(
            GameServerLite.class,
            GameServerListsMessage.class,
            RefreshMessage.class,
            JoinGameMessage.class,
            JoinAckMessage.class,    
            LobbyInformationMessage.class,
            GameInformationMessage.class,
            GameConfigurationMessage.class,
            TeamJoinMessage.class,
            PlayerMessage.class,
            PlayerLite.class,
            PlayerMovement.class
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
    public static class GameServerLite{

        private String address;
        private String idMap;
        private int port;
        private int nbPlayers;
        private int status;
        private int scoreBlue;
        private int scoreRed;

        public GameServerLite(){}

        public GameServerLite(String address, String idMap, int port, int nbPlayers, int status, int scoreBlue, int scoreRed){
            this.address = address;
            this.idMap = idMap;
            this.port = port;
            this.nbPlayers = nbPlayers;
            this.status = status;
            this.scoreBlue = scoreBlue;
            this.scoreRed = scoreRed;
        }

        public String getAddress(){ return address; }

        public String getIdMap(){ return idMap; }

        public int getPort() { return port; }

        public int getNbPlayers(){ return nbPlayers; }

        public int getStatus(){ return status; }

        public int getScoreBlue(){ return scoreBlue; }

        public int getScoreRed(){ return scoreRed; }

        @Override
        public String toString() {
            return address + tabs(20) + idMap + tabs(10) + nbPlayers + tabs(10) + scoreBlue + "-" + scoreRed;
        }

        private String tabs(int n) {
            return new String(new char[n]).replace("\0", "    ");
        }

    }

    @Serializable
    public static class PlayerLite {
        private int id;
        private String name;
        private int team;
        private Vector3f position;
        private Vector3f direction;

        public PlayerLite(){
        }

        public PlayerLite(int id, String name, Vector3f position, Vector3f direction, int team) {
            this.id = id;
            this.name = name;
            this.position = position;
            this.direction = direction;
            this.team = team;
        }

        public PlayerLite(Player player) {
            this.id = player.getId();
            this.name = player.getName();
            this.position = player.getPosition();
            this.direction = player.getDirection();
            System.out.println("position: "+this.position);
            System.out.println("direction: "+this.direction);
            this.team = player.getTeam();
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Vector3f getPosition() {
            return position;
        }

        public Vector3f getDirection() {
            return direction;
        }

        public int getTeam() {
            return team;
        }
    }
   
    @Serializable
    public static class GameServerListsMessage extends MyAbstractMessage {
        private ArrayList<GameServerLite> serversList;
        
        public GameServerListsMessage(){}
        
        public GameServerListsMessage(ArrayList<GameServerLite> serversList) {
            this.serversList = serversList;
        }
        
        
        public ArrayList<GameServerLite> getServersList() {
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
        private String name;

        public JoinGameMessage() {
        }

        public JoinGameMessage(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
        
    }
    
    @Serializable
    public static class JoinAckMessage extends MyAbstractMessage {
        private int id;

        public JoinAckMessage() {
        }

        public JoinAckMessage(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
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

    @Serializable
    public static class GameConfigurationMessage extends MyAbstractMessage {
        private ArrayList<PlayerLite> players;

        public GameConfigurationMessage(){
        }

        public GameConfigurationMessage(ArrayList<PlayerLite> players) {
            this.players = players;
        }

        public ArrayList<PlayerLite> getPlayers() {
            return players;
        }
    }

    @Serializable
    public static class TeamJoinMessage extends MyAbstractMessage {
        private int team;

        public TeamJoinMessage() {
        }

        public TeamJoinMessage(int team) {
            this.team = team;
        }

        public int getTeam() {
            return team;
        }
    }

    @Serializable
    public static class PlayerMessage extends MyAbstractMessage {
        private PlayerLite player;

        public PlayerMessage() {
        }

        public PlayerMessage(PlayerLite player) {
            this.player = player;
        }

        public PlayerLite getPlayer() {
            return player;
        }
    }
    
    @Serializable
    public static class PlayerMovement extends MyAbstractMessage{
        private final Vector3f velocity;
        private final Vector3f viewDirection;
        
        public PlayerMovement(Vector3f velocity, Vector3f viewDirection){
            this.velocity = velocity;
            this.viewDirection = viewDirection;
        }
        
        public Vector3f getVelocity(){
            return velocity;
        }
        
        public Vector3f getViewDirection(){
            return viewDirection;
        }
        
    }

    public static class BiMap<K,V> {
        ConcurrentHashMap<K,V> map = new ConcurrentHashMap<>();
        ConcurrentHashMap<V,K> inversedMap = new ConcurrentHashMap<>();

        public void put(K k, V v) {
            map.put(k, v);
            inversedMap.put(v, k);
        }

        public V get(K k) {
            return map.get(k);
        }

        public K getKey(V v) {
            return inversedMap.get(v);
        }

        public int size() {
            return map.size();
        }

        public boolean containsKey(K k) {
            return map.containsKey(k);
        }

        public boolean containsValue(V v) {
            return map.containsValue(v);
        }

        public V remove(K k) {
            V v = map.remove(k);
            inversedMap.remove(v);
            return v;
        }

        public Enumeration<V> values() {
            return inversedMap.keys();
        }

        public Enumeration<K> keys() {
            return map.keys();
        }
    }
    
    

     
}
