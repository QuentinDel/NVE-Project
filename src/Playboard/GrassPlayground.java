/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Playboard;

import static Playboard.PlaygroundConstant.BOARD_LENGTH;
import static Playboard.PlaygroundConstant.BOARD_WIDTH;
import static Playboard.PlaygroundConstant.GOAL_LINE_LENGTH;
import static Playboard.PlaygroundConstant.GOAL_LINE_WIDTH;
import static Playboard.PlaygroundConstant.GOAL_PICKET_HEIGHT;
import static Playboard.PlaygroundConstant.GOAL_PICKET_THICKNESS;
import static Playboard.PlaygroundConstant.GOAL_TRANSV;
import static Playboard.PlaygroundConstant.LINE_HEIGHT;
import static Playboard.PlaygroundConstant.LINE_LENGTH;
import static Playboard.PlaygroundConstant.LINE_THICKNESS;
import static Playboard.PlaygroundConstant.LINE_WIDTH;
import static Playboard.PlaygroundConstant.MIDDLE_DOT_RADIUS;
import static Playboard.PlaygroundConstant.POSITION_GOAL_LONG;
import static Playboard.PlaygroundConstant.POSITION_GOAL_SIDE_LINE;
import static Playboard.PlaygroundConstant.POSITION_GOAL_SIDE_LINE_WIDTH;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.util.SkyFactory;


/**
 *
 * @author Quentin
 */

public class GrassPlayground extends PlaygroundAbstract {

  public GrassPlayground(AssetManager assetManager){
      super(assetManager);
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
    Cylinder cylinderMesh = new Cylinder(32, 32, MIDDLE_DOT_RADIUS, 0.01f, true, false);
    Geometry middleDot = new Geometry("Cylinder", cylinderMesh);
    
    //Geometry for the goal keeper zone
    Node goalZoneNode = new Node("Goal");
    Box goalLine = new Box(GOAL_LINE_WIDTH, LINE_HEIGHT, LINE_THICKNESS);
    Geometry goalWidth  = new Geometry("line", goalLine);
    Box goalLineSide = new Box(LINE_THICKNESS, LINE_HEIGHT, GOAL_LINE_LENGTH);
    Geometry goalLong = new Geometry("line", goalLineSide);
    
    //Geometry for the goal
    Node goal = new Node("Goal");
    Box picket = new Box(GOAL_PICKET_THICKNESS, GOAL_PICKET_HEIGHT, GOAL_PICKET_THICKNESS);
    Geometry goalPicket = new Geometry("picket", picket);
    Box trans = new Box(GOAL_TRANSV, GOAL_PICKET_THICKNESS, GOAL_PICKET_THICKNESS);
    Geometry goalTrans = new Geometry("trans", trans);
    
    
    
    
    //Material used for the line
    Material matLine = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    matLine.setColor("Color", ColorRGBA.White);
    
    leftLine.setMaterial(matLine);
    topLine.setMaterial(matLine);
    middleDot.setMaterial(matLine);
    goalWidth.setMaterial(matLine);
    goalLong.setMaterial(matLine);
    goalPicket.setMaterial(matLine);
    goalTrans.setMaterial(matLine);
   
    
    Geometry rightLine = leftLine.clone(true);
    Geometry bottomLine = topLine.clone(true);
    Geometry middleLine = topLine.clone(true);
    Geometry goalWidthOtherSide = goalWidth.clone(true);
    Geometry otherPicket = goalPicket.clone(true);
            
    linesNode.attachChild(leftLine);
    linesNode.attachChild(rightLine);
    linesNode.attachChild(topLine);
    linesNode.attachChild(bottomLine);
    linesNode.attachChild(middleLine);
    linesNode.attachChild(middleDot);
    goalZoneNode.attachChild(goalWidth);
    goalZoneNode.attachChild(goalLong);
    goalZoneNode.attachChild(goalWidthOtherSide);
    goal.attachChild(goalPicket);
    goal.attachChild(goalTrans);
    goal.attachChild(otherPicket);

    
    //Set at the right place
    leftLine.setLocalTranslation(0f, 0f, -BOARD_WIDTH);
    rightLine.setLocalTranslation(0f, 0f, BOARD_WIDTH);
    topLine.setLocalTranslation(BOARD_LENGTH, 0f, 0f);
    bottomLine.setLocalTranslation(-BOARD_LENGTH, 0f, 0f);
    middleDot.rotate( 90*FastMath.DEG_TO_RAD , 0f,0f);
    goalWidth.setLocalTranslation(POSITION_GOAL_SIDE_LINE, 0f,POSITION_GOAL_SIDE_LINE_WIDTH);
    goalWidthOtherSide.setLocalTranslation(POSITION_GOAL_SIDE_LINE, 0f, -POSITION_GOAL_SIDE_LINE_WIDTH);
    goalLong.setLocalTranslation(POSITION_GOAL_LONG, 0f, 0f);
    goalPicket.setLocalTranslation(BOARD_LENGTH, GOAL_PICKET_HEIGHT, GOAL_TRANSV - GOAL_PICKET_THICKNESS);
    otherPicket.setLocalTranslation(BOARD_LENGTH, GOAL_PICKET_HEIGHT, -GOAL_TRANSV + GOAL_PICKET_THICKNESS);
    goalTrans.setLocalTranslation(BOARD_LENGTH, 2*GOAL_PICKET_HEIGHT, 0f);
    goalTrans.rotate(0f , 90*FastMath.DEG_TO_RAD, 0f);
    
    Node otherGoalZone = goalZoneNode.clone(true);
    otherGoalZone.rotate(0f , 0f , 180*FastMath.DEG_TO_RAD);
    Node otherGoal = goal.clone(true);
    otherGoal.rotate(0f , 180*FastMath.DEG_TO_RAD , 0f);
    //otherGoal.getChild("trans").rotate(180*FastMath.DEG_TO_RAD , 0f , 0f);

    board = new Node("board");
    board.attachChild(linesNode);
    board.attachChild(playgroundNode);
    board.attachChild(goalZoneNode);
    board.attachChild(otherGoalZone);
    board.attachChild(goal);
    board.attachChild(otherGoal);
    board.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
    
  }
}