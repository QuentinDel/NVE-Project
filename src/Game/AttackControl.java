/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import Network.gameserver.GameServer;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.network.Message;
import com.jme3.network.Server;

/**
 * 
 * This GhostControl is a one-time detection of collisions with players for attacks
 * If a there is a collision with a player that owns a ball, that player is attacked and forced to drop it
 * Afterwards, even if there was no collisions, this PhysicsTickListener removes itself from the physicsspace and as a listener
 * 
 * @author Henrik
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
