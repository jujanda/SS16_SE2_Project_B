package com.project_b.se2.mauerhuepfer.interfaces;

import android.support.annotation.IntDef;

import com.project_b.se2.mauerhuepfer.UpdateState;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by rohrbe on 14.05.16.
 */
public interface IReceiveMessage {

    /**
     * Possible Usage Codes:
     * PLAYERID - get Player ID.
     * DICE - any Player diced and share it with the others.
     * MOVE1 - any Player used his first dice.
     * MOVE2 - any Player used his second dice.
     * STARTGAME - Host start the Game.
     */
    @Retention(RetentionPolicy.CLASS)
    @IntDef({USAGE_GAMEBOARDCREATED, USAGE_CLICKEDPLAYER, USAGE_CLICKEDBLOCK, USAGE_PLAYERID, USAGE_DICE, USAGE_FIGURESELECTED, USAGE_FIGUREMOVED, USAGE_NEXTPLAYER, USAGE_STARTGAME, USAGE_MSG, USAGE_JOIN, USAGE_RESTART})
    @interface UpdateUsageCode {
    }

    int USAGE_GAMEBOARDCREATED = 1025;
    int USAGE_CLICKEDPLAYER = 1026;
    int USAGE_CLICKEDBLOCK = 1027;
    int USAGE_PLAYERID = 1028;
    int USAGE_DICE = 1029;
    int USAGE_FIGURESELECTED = 1030;
    int USAGE_FIGUREMOVED = 1031;
    int USAGE_NEXTPLAYER = 1032;
    int USAGE_STARTGAME = 1033;
    int USAGE_MSG = 1034;
    int USAGE_JOIN = 1035;
    int USAGE_RESTART = 1036;


    /**
     * recieve Message and Game Updates
     *
     * @param status Containing Message and Game Parameters
     */
    void receiveMessage(UpdateState status);
}
