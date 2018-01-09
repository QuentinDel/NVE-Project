
package Network;

import Game.Player;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * A class containing information needed all across the game, such as packets
 * 
 * @author Rickard, Quentin, Henrik
 * Discussion, various packets
 */

public class Util {
    
    //public static final String HOSTNAME = "130.240.153.160";
    //public static final String GAME_HOSTNAME = "130.240.155.53";
    public static final String HOSTNAME = "127.0.0.1";
    public static final String GAME_HOSTNAME = "127.0.0.1";
    
    public static final int PORT = 7006;
    public static final int PORT_GAME = 7009;
    
    public static final int RUNNING = 0;
    public static final int WAITING = 1;
    
    public static final int SPECTATOR_TEAM_ID = 0;
    public static final int RED_TEAM_ID = 1;
    public static final int BLUE_TEAM_ID = 2;
    
    public static final int MAX_MESSAGE_LENGTH = 50;
    
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
            PlayerMovementMessage.class,
            PlayerPhysics.class,
            BallPhysics.class,
            UpdatePhysics.class,
            UpdateBallPhysics.class,
            ScoreUpdateMessage.class,
            JumpMessage.class,
            GrabBallMessage.class,
            NewPlayerMessage.class,
            ShootBallMessage.class,
            PlayerDisconnectedMessage.class,
            AttackMessage.class,
            DropBallMessage.class,
            ChatMessage.class,
            BallInitInformation.class
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
    public static class PlayerPhysics{
        protected int id;
        protected Vector3f direction;
        protected Vector3f velocity;
        protected Vector3f position;
        
        public PlayerPhysics(){}
        
        public PlayerPhysics(int id, Vector3f direction, Vector3f velocity, Vector3f position){
            this.id = id;
            this.direction = direction;
            this.velocity = velocity;
            this.position = position;
        }
        
        public int getId() {
            return id;
        }
        
        public Vector3f getDirection() {
            return direction;
        }
        
        public Vector3f getVelocity(){
            return velocity;
        }
        
        public Vector3f getPosition() {
            return position;
        }
        
    }

    @Serializable
    public static class PlayerLite extends PlayerPhysics{
        private String name;
        private int team;

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
            this.velocity = player.getVelocity();
            this.team = player.getTeam();
        }

        public String getName() {
            return name;
        }

        public int getTeam() {
            return team;
        }
    }
    
    @Serializable
    public static class BallPhysics{
        protected Vector3f position;
        protected Vector3f velocity;
        protected Vector3f angularVelocity;
        
        public BallPhysics(){}
        
        public BallPhysics(Vector3f position, Vector3f velocity, Vector3f angularVelocity){
            this.position = position;
            this.velocity = velocity;
            this.angularVelocity = angularVelocity;
            
        }
        
        public Vector3f getPosition() {
            return position;
        }
        
        public Vector3f getVelocity(){
            return velocity;
        }
        
        public Vector3f getAngularVelocity() {
            return angularVelocity;
        }
        
    }
    
     @Serializable   
    public static class BallInitInformation extends BallPhysics{
        protected boolean isOwned;
        protected int idOwner;
        
        public BallInitInformation(){};
        
         public BallInitInformation(Vector3f position, Vector3f velocity, Vector3f angularVelocity, boolean isOwned, int idOwner){
            this.position = position;
            this.velocity = velocity;
            this.angularVelocity = angularVelocity;
            this.isOwned = isOwned;
            this.idOwner = idOwner;
        }
         
        public boolean isOwned(){
            return isOwned;
        }
        
        public int getOwner(){
            return idOwner;
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
        private BallInitInformation ball;
        private int blueScore;
        private int redScore;
        
        public GameConfigurationMessage(){
        }

        public GameConfigurationMessage(ArrayList<PlayerLite> players, BallInitInformation ball, int blueScore, int redScore) {
            this.players = players;
            this.ball = ball;
            this.blueScore = blueScore;
            this.redScore = redScore;
        }

        public ArrayList<PlayerLite> getPlayers() {
            return players;
        }
        
        public BallInitInformation getBall(){
            return ball;
        }
        
        public int getBlueScore() {
            return blueScore;
        }
        
        public int getRedScore() {
            return redScore;
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
    public static class PlayerMovementMessage extends MyAbstractMessage{
        private Vector3f velocity;
        private Vector3f viewDirection;
        
        public PlayerMovementMessage(){}
        
        public PlayerMovementMessage(Vector3f velocity, Vector3f viewDirection){
            this.velocity = velocity;
            this.viewDirection = viewDirection;
        }
        
        public Vector3f getVelocity(){
            return velocity;
        }
        
        public Vector3f getViewDirection(){
            return viewDirection;
        }
        
        public void updateVelocity(Vector3f vel) {
            this.velocity = this.velocity.add(vel);
        }
        
        public void updateViewDirection(Vector3f dir) {
            this.viewDirection = dir;
        }
        
        public void scaleVelocity(float scale) {
            this.velocity = this.velocity.mult(scale);
        }
    }
    
    //Do not serialize and send this message
    //It is used internally within the GameClient to pass tpf for aggregation
    public static class InternalMovementMessage extends PlayerMovementMessage {
        private float tpf;
        
        public InternalMovementMessage(){}
        
        public InternalMovementMessage(Vector3f velocity, Vector3f viewDirection, float tpf) {
            super(velocity, viewDirection);
            this.tpf = tpf;
        }
        
        public float getTpf() {
            return tpf;
        }
    }
    
    @Serializable
    public static class UpdatePhysics extends MyAbstractMessage{
        ArrayList<PlayerPhysics> playersPhys;
        
        public UpdatePhysics(){}
        
        public UpdatePhysics(ArrayList<PlayerPhysics> playersPhys){
            this.playersPhys = playersPhys;
        }
        
        public ArrayList<PlayerPhysics> getPlayersPhys(){
            return playersPhys;
        }
    }
    
    @Serializable
    public static class UpdateBallPhysics extends MyAbstractMessage{
        BallPhysics ball;
        
        public UpdateBallPhysics(){}
        
        public UpdateBallPhysics(BallPhysics ball){
            this.ball = ball;
        }
        
        public BallPhysics getBallPhys() {
            return ball;
        }
    }
    
    @Serializable
    public static class ScoreUpdateMessage extends MyAbstractMessage{
        private int blueScore;
        private int redScore;
        
        public ScoreUpdateMessage(){}
        
        public ScoreUpdateMessage(int blueScore, int redScore) {
            this.blueScore = blueScore;
            this.redScore = redScore;
        }
        
        public int getBlueScore() {
            return blueScore;
        }
        
        public int getRedScore() {
            return redScore;
        }
    }
    
    @Serializable
    public static class JumpMessage extends MyAbstractMessage{
        private int id;
        
        public JumpMessage(){}
        
        public JumpMessage(int id){
            this.id = id;
        }
        
        public int getId(){
            return id;
        }
    }

    @Serializable
    public static class GrabBallMessage extends MyAbstractMessage {
        private int id;
        private Vector3f location;
        
        public GrabBallMessage() {
        }
        
        public GrabBallMessage(int id, Vector3f location){
            this.id = id;
            this.location = location;
        }
        
        public int getId(){
            return id;
        }

        public Vector3f getLocation() {
            return location;
        }
    }

    @Serializable
    public static class NewPlayerMessage extends MyAbstractMessage {
        private PlayerLite player;

        public NewPlayerMessage() {
        }

        public NewPlayerMessage(PlayerLite player) {
            this.player = player;
        }

        public PlayerLite getPlayer() {
            return player;
        }
    }
    
    @Serializable
    public static class ShootBallMessage extends MyAbstractMessage {
        private int playerId;
        private Vector3f direction;
        private float power;

        public ShootBallMessage() {
        }

        public ShootBallMessage(int playerId, Vector3f direction, float power) {
            this.playerId = playerId;
            this.direction = direction;
            this.power = power;
        }
        
        public int getPlayerId(){
            return playerId;
        }
        
        public Vector3f getDirection(){
            return direction;
        }
        
        public float getPower(){
            return power;
        }
    }
    
    @Serializable
    public static class PlayerDisconnectedMessage extends MyAbstractMessage {
        private int playerID;
        
        public PlayerDisconnectedMessage(){}
        
        public PlayerDisconnectedMessage(int playerID) {
            this.playerID = playerID;
        }
        
        public int getId() {
            return this.playerID;
        }
        
    }

    @Serializable
    public static class AttackMessage extends MyAbstractMessage {
        private int id;
        private Vector3f location;

        public AttackMessage() {
        }

        public AttackMessage(int id, Vector3f location) {
            this.id = id;
            this.location = location;
        }

        public int getId() {
            return id;
        }

        public Vector3f getLocation() {
            return location;
        }
    }

    @Serializable
    public static class DropBallMessage extends MyAbstractMessage {
        private int id;

        public DropBallMessage() {
        }

        public DropBallMessage(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
    
    @Serializable
    public static class ChatMessage extends MyAbstractMessage {
        private String message;
        
        public ChatMessage() {}
        
        public ChatMessage(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return this.message;
        }
    }
    
    //Used to tell a thread listening or sending packets to terminate
    //Dont serialize or send this over the network
    public static class TerminateMessage extends MyAbstractMessage {
        
        public TerminateMessage(){}
        
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
