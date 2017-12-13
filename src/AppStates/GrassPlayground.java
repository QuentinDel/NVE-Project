/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AppStates;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;


/**
 *
 * @author Quentin
 */

public class GrassPlayground implements PlaygroundConstant {

  private Node board;
  private final AssetManager assetManager;
  
  
  public GrassPlayground(AssetManager assetManager){
      this.assetManager =  assetManager;
      initialize();
  } 

  public void initialize() {
  
    Node playgroundNode = new Node("GrassPG");
    Spatial playground  = assetManager.loadModel("Scenes/PlayGround.j3o");
    playgroundNode.attachChild(playground);
  
    
    Node linesNode = new Node("lines"); 
    
    //Creation of the different geometry for the limitations of the playground
    Box lengthLine = new Box(LINE_LENGTH, LINE_HEIGHT, LINE_THICKNESS);
    Geometry leftLine = new Geometry("line", lengthLine);
    Box widthLine = new Box(LINE_THICKNESS, LINE_HEIGHT, LINE_WIDTH);
    Geometry topLine = new Geometry("line", widthLine);
    
    //Geometry for the goal keeper zone
    Node goalZoneNode = new Node("Goal");
    Box goalLine = new Box(GOAL_WIDTH, LINE_HEIGHT, LINE_THICKNESS);
    Geometry goalWidth  = new Geometry("line", goalLine);
    Box goalLineSide = new Box(LINE_THICKNESS, LINE_HEIGHT, GOAL_LENGTH);
    Geometry goalLong = new Geometry("line", goalLineSide);
    
    
    //Material used for the line
    Material matLine = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    matLine.setColor("Color", ColorRGBA.White);
    
    leftLine.setMaterial(matLine);
    topLine.setMaterial(matLine);
    goalWidth.setMaterial(matLine);
    goalLong.setMaterial(matLine);
    
    Geometry rightLine = leftLine.clone(true);
    Geometry bottomLine = topLine.clone(true);
    Geometry goalWidthOtherSide = goalWidth.clone(true);
            
    linesNode.attachChild(leftLine);
    linesNode.attachChild(rightLine);
    linesNode.attachChild(topLine);
    linesNode.attachChild(bottomLine);
    goalZoneNode.attachChild(goalWidth);
    goalZoneNode.attachChild(goalLong);
    goalZoneNode.attachChild(goalWidthOtherSide);

    
    //Set at the right place
    leftLine.setLocalTranslation(0f, 0f, -BOARD_WIDTH);
    rightLine.setLocalTranslation(0f, 0f, BOARD_WIDTH);
    topLine.setLocalTranslation(BOARD_LENGTH, 0f, 0f);
    bottomLine.setLocalTranslation(-BOARD_LENGTH, 0f, 0f);
    goalWidth.setLocalTranslation(POSITION_GOAL_SIDE_LINE, 0f,POSITION_GOAL_SIDE_LINE_WIDTH);
    goalWidthOtherSide.setLocalTranslation(POSITION_GOAL_SIDE_LINE, 0f, -POSITION_GOAL_SIDE_LINE_WIDTH);
    goalLong.setLocalTranslation(POSITION_GOAL_LONG, 0f, 0f);
    
    Node otherGoal = goalZoneNode.clone(true);
    otherGoal.rotate(0f , 0f , 180*FastMath.DEG_TO_RAD);

    board = new Node("board");
    board.attachChild(linesNode);
    board.attachChild(playgroundNode);
    board.attachChild(goalZoneNode);
    board.attachChild(otherGoal);
    
  }
  
  public Node getNode(){
      return board;
  }
}