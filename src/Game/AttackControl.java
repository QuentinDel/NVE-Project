/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import Network.GameApplication;
import Network.gameserver.GameServer;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.network.Message;
import com.jme3.network.Server;
import java.util.concurrent.Callable;

/**
 * 
 * This GhostControl detects collisions with the Goals in the level
 * After a collision with a goal is detected, remove the ghostControl and the PhysicsTickListener
 * from the physics space to prevent multiple collision detections
 * 
 * @author Rickard
 */
public class AttackControl extends GhostControl implements PhysicsTickListener {
    
    private GameServer gameServer;
    private Server server;
    private Player player;
    private Message msg;
    
    public AttackControl(CollisionShape shape, Server server, GameServer gameServer, Player player, Message msg){
        super(shape);
        this.server = server;
        this.gameServer = gameServer;
        this.player = player;
        this.msg = msg;
    }
    
    public void prePhysicsTick(PhysicsSpace space, float tpf) {}
    
    public void physicsTick(PhysicsSpace space, float tpf) {
        for (PhysicsCollisionObject collObj : getOverlappingObjects()){
                if(collObj.getUserObject() instanceof Player){
                    Player target = (Player) collObj.getUserObject();
                    if(target.hasBall()){
                        gameServer.dropBall(target.getId());
                    }
                }
            }
        space.remove(this);
        space.removeTickListener(this);
    }
}
