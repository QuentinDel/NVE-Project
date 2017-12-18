/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Playboard;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

/**
 *
 * @author Quentin
 */
public abstract class PlaygroundAbstract implements PlaygroundConstant{
  protected Node board;
  protected final AssetManager assetManager;
  
  public PlaygroundAbstract(AssetManager assetManager){
      this.assetManager = assetManager;
  }

  public Node getNode(){
      return board;
  } 
  
}
