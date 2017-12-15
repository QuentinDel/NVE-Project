/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Playboard;

/**
 *
 * @author Quentin
 */
public interface PlaygroundConstant {
    
    //Board dimension
    static final float PLAYGROUND_LENGTH = 500;
    static final float PLAYGROUND_WIDTH = 500;
            
    
    //Limitation playground
    static final float LINE_WIDTH = 75;
    static final float LINE_LENGTH = 125;
    static final float LINE_HEIGHT = 0.01f;
    static final float LINE_THICKNESS = 1;
    static final float MIDDLE_DOT_RADIUS = 3;
    
    //Area of goalkeeper zone
    static final float GOAL_LINE_LENGTH = LINE_WIDTH * 0.55f;
    static final float GOAL_LINE_WIDTH = LINE_LENGTH * 0.2f;
    static final float POSITION_GOAL_SIDE_LINE = LINE_LENGTH - GOAL_LINE_WIDTH;
    static final float POSITION_GOAL_SIDE_LINE_WIDTH = GOAL_LINE_LENGTH; 
    static final float POSITION_GOAL_LONG = POSITION_GOAL_SIDE_LINE - GOAL_LINE_WIDTH + LINE_THICKNESS;
    
    static final float BOARD_WIDTH = LINE_WIDTH - LINE_THICKNESS;
    static final float BOARD_LENGTH = LINE_LENGTH - LINE_THICKNESS;
    
    //Goal zone
    static final float GOAL_TRANSV = GOAL_LINE_LENGTH * 0.6f;
    static final float GOAL_PICKET_HEIGHT = 6f;
    static final float GOAL_PICKET_THICKNESS = 1f;
    
    //Wall
    static final float WALL_LENGTH = LINE_LENGTH;
    static final float WALL_WIDTH = LINE_WIDTH;
    static final float WALL_HEIGHT = 25;
    static final float WALL_THICKNESS = 1;
    
    //Score zone
    static final float SCORE_ZONE_LENGTH = GOAL_TRANSV - 2 * GOAL_PICKET_THICKNESS;
    static final float SCORE_ZONE_HEIGHT = GOAL_PICKET_HEIGHT - 0.5f * GOAL_PICKET_THICKNESS;
    static final float SCORE_ZONE_THICKNESS = LINE_THICKNESS;
        

    
}
