/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Playboard;

import Playboard.PlaygroundConstant;
import static Playboard.PlaygroundConstant.BOARD_LENGTH;
import static Playboard.PlaygroundConstant.BOARD_WIDTH;
import static Playboard.PlaygroundConstant.GOAL_LINE_LENGTH;
import static Playboard.PlaygroundConstant.LINE_HEIGHT;
import static Playboard.PlaygroundConstant.LINE_LENGTH;
import static Playboard.PlaygroundConstant.LINE_THICKNESS;
import static Playboard.PlaygroundConstant.LINE_WIDTH;
import static Playboard.PlaygroundConstant.POSITION_GOAL_LONG;
import static Playboard.PlaygroundConstant.POSITION_GOAL_SIDE_LINE;
import static Playboard.PlaygroundConstant.POSITION_GOAL_SIDE_LINE_WIDTH;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import static Playboard.PlaygroundConstant.WALL_HEIGHT;
import static Playboard.PlaygroundConstant.WALL_LENGTH;
import static Playboard.PlaygroundConstant.WALL_THICKNESS;
import static Playboard.PlaygroundConstant.WALL_WIDTH;
import com.jme3.asset.plugins.HttpZipLocator;
import com.jme3.material.RenderState;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Quad;
import com.jme3.util.SkyFactory;

/**
 *
 * @author Quentin
 */

public class TestPlayground extends SimpleApplication implements PlaygroundConstant {

  private Node board;

  public static void main(String[] args) {
    TestPlayground app = new TestPlayground();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    flyCam.setMoveSpeed(50);
    cam.setLocation(new Vector3f(0f, 100f, 0f));
    
    Node playgroundNode = new Node("GrassPG");
    
    Quad grassBoard = new Quad(PLAYGROUND_LENGTH, PLAYGROUND_WIDTH);
    Geometry playground = new Geometry("playboard", grassBoard);
    Material matPlayground = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    matPlayground.setTexture("ColorMap", assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg"));
    playground.setMaterial(matPlayground);
    playgroundNode.attachChild(playground);
    playgroundNode.rotate(-90*FastMath.DEG_TO_RAD , 0f , 0f);
    playgroundNode.setLocalTranslation(-PLAYGROUND_LENGTH/2, 0f, PLAYGROUND_WIDTH/2);
  
    
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
    
    //Geometry for the walls
    Node walls = new Node("walls");
    Box wallBoxLength = new Box(WALL_LENGTH, WALL_HEIGHT, WALL_THICKNESS);
    Geometry wallLengthLeft = new Geometry("wallL", wallBoxLength);
    Box wallBoxWidth = new Box(WALL_THICKNESS, WALL_HEIGHT, WALL_WIDTH);
    Geometry wallWidthTop = new Geometry("wallT", wallBoxWidth);
    
    //Geometry for the markPoint
    Node scoreZoneBlue = new Node("markPointBlue");
    Box goalScoreBox = new Box(SCORE_ZONE_THICKNESS, SCORE_ZONE_HEIGHT, SCORE_ZONE_LENGTH);
    Geometry goalScoreBlue = new Geometry("scoreZoneBlue", goalScoreBox);
    
    
    // PART FOR MATERIALS SETTINGS 
    
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
    
    //Material used for the walls
    Material matWalls = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    matWalls.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);  // !
    matWalls.setTransparent(true);
    matWalls.setColor("Color", new ColorRGBA(0.8f, 0.8f, 0.8f, 0.2f));
    
    wallLengthLeft.setQueueBucket(RenderQueue.Bucket.Transparent);
    wallWidthTop.setQueueBucket(RenderQueue.Bucket.Transparent);   
    wallLengthLeft.setMaterial(matWalls);
    wallWidthTop.setMaterial(matWalls);
   
    
    //Material for score zone
    Material matScoreZone = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    matScoreZone.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);  // !
    matScoreZone.setTransparent(true);
    matScoreZone.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.2f, 0.5f));   
    goalScoreBlue.setMaterial(matScoreZone);
    
    // CREATION OF OTHER PARTS OF THE BOARD FROM THE ONES ALREADY CREATED
    
    //Generate the other
    Geometry rightLine = leftLine.clone(true);
    Geometry bottomLine = topLine.clone(true);
    Geometry middleLine = topLine.clone(true);
    Geometry goalWidthOtherSide = goalWidth.clone(true);
    Geometry otherPicket = goalPicket.clone(true);
    Geometry wallWidthBottom = wallWidthTop.clone(true);
    Geometry wallLengthRight = wallLengthLeft.clone(true);
    
    
    //Attach all the nodes
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
    walls.attachChild(wallWidthTop);
    walls.attachChild(wallWidthBottom);
    walls.attachChild(wallLengthLeft);
    walls.attachChild(wallLengthRight);
    scoreZoneBlue.attachChild(goalScoreBlue);

    
    //Set at the right place
    leftLine.setLocalTranslation(0f, 0f, -BOARD_WIDTH);
    rightLine.setLocalTranslation(0f, 0f, BOARD_WIDTH);
    topLine.setLocalTranslation(BOARD_LENGTH, 0f, 0f);
    bottomLine.setLocalTranslation(-BOARD_LENGTH, 0f, 0f);
    middleDot.rotate( 90*FastMath.DEG_TO_RAD , 0f, 0f);
    goalWidth.setLocalTranslation(POSITION_GOAL_SIDE_LINE, 0f,POSITION_GOAL_SIDE_LINE_WIDTH);
    goalWidthOtherSide.setLocalTranslation(POSITION_GOAL_SIDE_LINE, 0f, -POSITION_GOAL_SIDE_LINE_WIDTH);
    goalLong.setLocalTranslation(POSITION_GOAL_LONG, 0f, 0f);
    goalPicket.setLocalTranslation(BOARD_LENGTH, GOAL_PICKET_HEIGHT, GOAL_TRANSV - GOAL_PICKET_THICKNESS);
    otherPicket.setLocalTranslation(BOARD_LENGTH, GOAL_PICKET_HEIGHT, -GOAL_TRANSV + GOAL_PICKET_THICKNESS);
    goalTrans.setLocalTranslation(BOARD_LENGTH, 2*GOAL_PICKET_HEIGHT, 0f);
    goalTrans.rotate(0f , 90*FastMath.DEG_TO_RAD, 0f);
    wallWidthTop.setLocalTranslation(BOARD_LENGTH, WALL_HEIGHT, 0f);
    wallWidthBottom.setLocalTranslation(-BOARD_LENGTH, WALL_HEIGHT, 0f);
    wallLengthLeft.setLocalTranslation(0f, WALL_HEIGHT, -BOARD_WIDTH);
    wallLengthRight.setLocalTranslation(0f, WALL_HEIGHT, BOARD_WIDTH);
    goalScoreBlue.setLocalTranslation(BOARD_LENGTH - 0.01f, SCORE_ZONE_HEIGHT, 0f);
    
    
    Node otherGoalZone = goalZoneNode.clone(true);
    otherGoalZone.rotate(0f , 0f , 180*FastMath.DEG_TO_RAD);
    Node otherGoal = goal.clone(true);
    otherGoal.rotate(0f , 180*FastMath.DEG_TO_RAD , 0f);
    Node scoreZoneRed = scoreZoneBlue.clone(true);
    scoreZoneRed.rotate(0f , 180*FastMath.DEG_TO_RAD , 0f);


    board = new Node("board");
    board.attachChild(walls);
    board.attachChild(linesNode);
    board.attachChild(playgroundNode);
    board.attachChild(goalZoneNode);
    board.attachChild(otherGoalZone);
    board.attachChild(goal);
    board.attachChild(otherGoal);
    board.attachChild(scoreZoneBlue);
    board.attachChild(scoreZoneRed);
    board.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
    
    
    rootNode.attachChild(board);
  }
  
  public Node getNode(){
      return board;
  }
}