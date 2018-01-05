/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Playboard;

import Game.Game;
import Game.ScoreControl;
import Network.GameApplication;
import Network.Util;
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
import static Playboard.PlaygroundConstant.PLAYGROUND_LENGTH;
import static Playboard.PlaygroundConstant.PLAYGROUND_WIDTH;
import static Playboard.PlaygroundConstant.POSITION_GOAL_LONG;
import static Playboard.PlaygroundConstant.POSITION_GOAL_SIDE_LINE;
import static Playboard.PlaygroundConstant.POSITION_GOAL_SIDE_LINE_WIDTH;
import static Playboard.PlaygroundConstant.SCORE_ZONE_HEIGHT;
import static Playboard.PlaygroundConstant.SCORE_ZONE_LENGTH;
import static Playboard.PlaygroundConstant.SCORE_ZONE_THICKNESS;
import static Playboard.PlaygroundConstant.WALL_HEIGHT;
import static Playboard.PlaygroundConstant.WALL_LENGTH;
import static Playboard.PlaygroundConstant.WALL_THICKNESS;
import static Playboard.PlaygroundConstant.WALL_WIDTH;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Quad;
import com.jme3.util.SkyFactory;


/**
 *
 * @author Quentin
 */

public class GrassPlayground extends PlaygroundAbstract {
  
  private GameApplication app;
  private Game game;
  
  private Node playgroundFloor;
  private Node playgroundWithoutLine;
  private Node playgroundLines;
  
  private ScoreControl blueScoreControl;
  private ScoreControl redScoreControl;

  public GrassPlayground(GameApplication app, Game game){
      super(app.getAssetManager());
      this.app = app;
      this.game = game;
      board = new Node("board");
      playgroundFloor = new Node("GrassPG");
      playgroundWithoutLine = new Node("PlaygroundWlines");
      playgroundLines = new Node("PlaygroundLines");
      initialize();
  } 

  public void initialize() {
    
    Node playgroundNode = new Node("GrassPG");
    
    Quad grassBoard = new Quad(PLAYGROUND_LENGTH, PLAYGROUND_WIDTH);
    Geometry playground = new Geometry("playboard", grassBoard);
    Material matPlayground = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    matPlayground.setTexture("ColorMap", assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg"));
    playground.setMaterial(matPlayground);
    playgroundNode.attachChild(playground);
    playgroundNode.rotate(-90*FastMath.DEG_TO_RAD , 0f , 0f);
    playgroundNode.setLocalTranslation(-PLAYGROUND_LENGTH/2, 0f, PLAYGROUND_WIDTH/2);
    playgroundFloor.attachChild(playgroundNode);
  
    //Creation of the skybox
    Spatial skybox = SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap);
    
    Node linesNode = new Node("lines"); 
    
    //Creation of the different geometry for the limitations of the playground
    Box lengthLine = new Box(LINE_LENGTH, LINE_HEIGHT, LINE_THICKNESS);
    Geometry leftLine = new Geometry("line", lengthLine);
    Box widthLine = new Box(LINE_THICKNESS, LINE_HEIGHT, LINE_WIDTH);
    Geometry topLine = new Geometry("line", widthLine);
    Cylinder cylinderMesh = new Cylinder(0, 16, MIDDLE_DOT_RADIUS, 0.01f, true, false);
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
    Box teamColorBox = new Box(GOAL_TRANSV / 2, GOAL_PICKET_THICKNESS * 2, GOAL_PICKET_THICKNESS);
    Geometry teamColorGeom = new Geometry("teamColor", teamColorBox);
    
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
    blueScoreControl = new ScoreControl(app, new BoxCollisionShape(new Vector3f(SCORE_ZONE_THICKNESS, SCORE_ZONE_HEIGHT, SCORE_ZONE_LENGTH)), Util.BLUE_TEAM_ID);
    goalScoreBlue.addControl(blueScoreControl);
    
    Node scoreZoneRed = new Node("markPointRed");
    Geometry goalScoreRed = new Geometry("scoreZoneRed", goalScoreBox.clone());
    redScoreControl = new ScoreControl(app, new BoxCollisionShape(new Vector3f(SCORE_ZONE_THICKNESS, SCORE_ZONE_HEIGHT, SCORE_ZONE_LENGTH)), Util.RED_TEAM_ID);
    goalScoreRed.addControl(redScoreControl);
    

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
    
    //Material for teamcolor
    Material teamColor = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    teamColor.setColor("Color", ColorRGBA.Red);
    teamColorGeom.setMaterial(teamColor);

    
    //Material for score zone
    Material matScoreZone = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    matScoreZone.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);  // !
    matScoreZone.setTransparent(true);
    matScoreZone.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.2f, 0.5f));
    goalScoreBlue.setMaterial(matScoreZone);
    goalScoreRed.setMaterial(matScoreZone);
    
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
    goal.attachChild(teamColorGeom);
    walls.attachChild(wallWidthTop);
    walls.attachChild(wallWidthBottom);
    walls.attachChild(wallLengthLeft);
    walls.attachChild(wallLengthRight);
    scoreZoneBlue.attachChild(goalScoreBlue);
    scoreZoneRed.attachChild(goalScoreRed);

    
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
    teamColorGeom.setLocalTranslation(BOARD_LENGTH, 3.5f*GOAL_PICKET_HEIGHT, 0f);
    goalTrans.rotate(0f , 90*FastMath.DEG_TO_RAD, 0f);
    teamColorGeom.rotate(0f , 90*FastMath.DEG_TO_RAD, 0f);
    wallWidthTop.setLocalTranslation(BOARD_LENGTH, WALL_HEIGHT, 0f);
    wallWidthBottom.setLocalTranslation(-BOARD_LENGTH, WALL_HEIGHT, 0f);
    wallLengthLeft.setLocalTranslation(0f, WALL_HEIGHT, -BOARD_WIDTH);
    wallLengthRight.setLocalTranslation(0f, WALL_HEIGHT, BOARD_WIDTH);
    goalScoreBlue.setLocalTranslation(BOARD_LENGTH - 0.01f, SCORE_ZONE_HEIGHT, 0f);
    goalScoreRed.setLocalTranslation(-BOARD_LENGTH + 0.1f, SCORE_ZONE_HEIGHT, 0f);
    skybox.setLocalTranslation(0, -50, 0);
    
    
    Node otherGoalZone = goalZoneNode.clone(true);
    otherGoalZone.rotate(0f , 0f , 180*FastMath.DEG_TO_RAD);
    Node otherGoal = goal.clone(true);
    ((Geometry)otherGoal.getChild("teamColor")).getMaterial().setColor("Color", ColorRGBA.Blue);
    otherGoal.rotate(0f , 180*FastMath.DEG_TO_RAD , 0f);
    
    playgroundWithoutLine.attachChild(walls);
    //playgroundWithoutLine.attachChild(playgroundFloor);

    playgroundWithoutLine.attachChild(goal);
    playgroundWithoutLine.attachChild(otherGoal);
    
    playgroundLines.attachChild(skybox);
    playgroundLines.attachChild(linesNode);
    playgroundLines.attachChild(goalZoneNode);
    playgroundLines.attachChild(otherGoalZone);
    
    board.attachChild(scoreZoneBlue);
    board.attachChild(scoreZoneRed);
    board.attachChild(playgroundFloor);
    board.attachChild(playgroundWithoutLine);
    board.attachChild(playgroundLines);
    
  }
  
  public Node getPlayGroundFloor(){
      return playgroundFloor;
  }
  
  public Node getPlayGroundWithoutLine(){
      return playgroundWithoutLine;
  }
  
  public Node playgroundLines(){
      return playgroundLines;
  }
  
  public ScoreControl getBlueScoreControl() {
      return blueScoreControl;
  }
  
  public ScoreControl getRedScoreControl() {
      return redScoreControl;
  }
}