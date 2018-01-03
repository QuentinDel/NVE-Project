/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import com.jme3.app.Application;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

/**
 *
 * @author Quentin
 */
public class Ball extends Node {
    
    private final Application sapp;
    private Geometry ball_geo;
    private RigidBodyControl ball_phy;
    private final BulletAppState bulletAppState;
    private boolean isOwned;
    private int ownedBy;

    
    public Ball(Application sapp, BulletAppState bulletAppState){
        this.sapp = sapp;
        this.bulletAppState = bulletAppState;
        this.isOwned = false;
        ownedBy = 0;
        
        initGeometry();
    }
    
    private void initGeometry(){
        Sphere sphere = new Sphere(64, 64, 2f, true, false);
        sphere.setTextureMode(Sphere.TextureMode.Projected);
        
        //Setup the material for the ball
        Material stone_mat = new Material(sapp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
        key.setGenerateMips(true);
        Texture tex = sapp.getAssetManager().loadTexture(key);
        stone_mat.setTexture("ColorMap", tex);
        
        //Setup the geometry for the ball
        ball_geo = new Geometry("ball", sphere);
        ball_geo.setMaterial(stone_mat);
        super.attachChild(ball_geo);
        
        //Setup the physics controller
        CollisionShape ball_shape = new SphereCollisionShape(sphere.getRadius());
        ball_phy = new RigidBodyControl(ball_shape, 10f);
        ball_phy.setRestitution(0.8f);
        ball_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
        ball_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        super.addControl(ball_phy);
        this.addPhysic();
       

        //Move the ball to the initial position
        //ball_phy.setPhysicsLocation(new Vector3f(-5, 6f, -5));
    }
    
    public Geometry getGeometry(){
        return ball_geo;
    }
        
    public boolean getIsOwned(){
        return isOwned;
    }
    
    public void setOwned(int id){
        isOwned = true;
        ownedBy = id;
    }
    
    public void notOwnedAnymore(){
        isOwned = false;
    }

    public int getOwner() {
        return ownedBy;
    }
    
    public Vector3f getPosition() {
        return this.ball_phy.getPhysicsLocation();
    }

    public void setPosition(Vector3f position) {
        this.ball_phy.setPhysicsLocation(position);
    }

    public Vector3f getAngularVelocity() {
        return this.ball_phy.getAngularVelocity();
    }

    public void setAngularVelocity(Vector3f angularVelocity) {
        this.ball_phy.setAngularVelocity(angularVelocity);
    }

    public Vector3f getVelocity() {
        return this.ball_phy.getLinearVelocity();
    }

    public void setVelocity(Vector3f velocity) {
        this.ball_phy.setLinearVelocity(velocity);
    }
    
    public void addPhysic(){
        bulletAppState.getPhysicsSpace().add(ball_phy);
    }
    
    public void removePhysic(){
        bulletAppState.getPhysicsSpace().remove(ball_phy);
    }
}
