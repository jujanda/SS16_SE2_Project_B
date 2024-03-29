package com.project_b.se2.mauerhuepfer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.project_b.se2.mauerhuepfer.interfaces.INetworkManager;
import com.project_b.se2.mauerhuepfer.interfaces.IReceiveMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@SuppressWarnings("deprecation")
public class Game {

    //Colours
    static final int RED = 0;
    static final int GREEN = 1;
    static final int YELLOW = 2;
    static final int BLACK = 3;
    static final int FilterColor = Color.GRAY;
    static final PorterDuff.Mode FilterMode = PorterDuff.Mode.MULTIPLY;

    //Directions
    static final int UP = 0;
    static final int RIGHT = 1;
    static final int DOWN = 2;
    static final int LEFT = 3;
    static final int BASE = 4;
    static final int GOAL = 5;
    static final int START = 6;

    //Block types
    static final int S = 0;
    static final int HR = 1;
    static final int HL = 2;
    static final int EU = 3;
    static final int ER = 4;
    static final int ED = 5;
    static final int EL = 6;
    static final int V = 7;
    static final int F = 8;
    static final int W = 9;
    static final int N = 10;
    static final int BR = 11;
    static final int BG = 12;
    static final int BY = 13;
    static final int BB = 14;
    static final int GR = 15;
    static final int GG = 16;
    static final int GY = 17;
    static final int GB = 18;

    //Measurement variables
    static int unit;

    //Random variables
    private long originalSeed;
    private long currentSeed;

    //Game variables
    private int numberOfPlayers;
    private Player[] players;
    private int myPID;
    private String playerName;
    private int lastPlayerIndex;
    private int currentPlayerIndex;
    List<Block> possibleDestinationBlocks;
    private Figure selectedFigure;
    private int selectedDiceNumber;

    private int startColPos;
    private int startRowPos;
    private int endColPos;
    private int endRowPos;
    private boolean gameWon;
    private boolean lastPlayerCheated;
    private boolean currentPlayerCheated;
    private boolean figureRemoved;


    //Other variables
    private Context context;
    private Resources resources;
    private CustomGameBoardView gameBoardView;
    private CustomPlayerView playerView;
    private Dice dice;
    private INetworkManager networkManager;
    private UpdateState update;


    /**
     * 2D array containing all the blocks that form the game board.
     */
    private Block[][] gameBoard = {
            {new Block(GB), new Block(GB), new Block(GB), new Block(GB), new Block(N), new Block(GG), new Block(GG), new Block(GG), new Block(GG)},
            {new Block(GY), new Block(GY), new Block(GY), new Block(GY), new Block(N), new Block(GR), new Block(GR), new Block(GR), new Block(GR)},
            {new Block(ER), new Block(HR), new Block(HR), new Block(HR), new Block(HR), new Block(HR), new Block(HR), new Block(HR), new Block(F)},
            {new Block(V), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W)},
            {new Block(EU), new Block(HL), new Block(HL), new Block(HL), new Block(HL), new Block(HL), new Block(HL), new Block(HL), new Block(ED)},
            {new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(V)},
            {new Block(ER), new Block(HR), new Block(HR), new Block(HR), new Block(HR), new Block(HR), new Block(HR), new Block(HR), new Block(EL)},
            {new Block(V), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W)},
            {new Block(EU), new Block(HL), new Block(HL), new Block(HL), new Block(HL), new Block(HL), new Block(HL), new Block(HL), new Block(ED)},
            {new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(V)},
            {new Block(ER), new Block(HR), new Block(HR), new Block(HR), new Block(HR), new Block(HR), new Block(HR), new Block(HR), new Block(EL)},
            {new Block(V), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W), new Block(W)},
            {new Block(EU), new Block(HL), new Block(HL), new Block(HL), new Block(HL), new Block(HL), new Block(HL), new Block(HL), new Block(S)},
            {new Block(BB), new Block(BB), new Block(BY), new Block(BY), new Block(N), new Block(BG), new Block(BG), new Block(BR), new Block(BR)},
            {new Block(BB), new Block(BB), new Block(BY), new Block(BY), new Block(N), new Block(BG), new Block(BG), new Block(BR), new Block(BR)},
    };


    public Game(final Context contxt, final int numOfPlayers, INetworkManager manager, int PID, String playerName) {
        //Initialise variables
        this.context = contxt;
        this.resources = this.context.getResources();
        this.originalSeed = -1;
        this.currentSeed = -1;
        this.numberOfPlayers = numOfPlayers;
        this.currentPlayerIndex = 0;
        this.selectedFigure = null;
        this.selectedDiceNumber = -1;
        this.possibleDestinationBlocks = new ArrayList<>();
        this.startColPos = -1;
        this.startRowPos = -1;
        this.endColPos = -1;
        this.endRowPos = -1;
        this.networkManager = manager;
        this.playerName = playerName;
        this.dice = new Dice(contxt, this, networkManager, playerName);
        this.update = new UpdateState();
        this.myPID = PID;
        this.gameWon = false;
        this.lastPlayerCheated = false;
        this.lastPlayerIndex = -1;
        this.figureRemoved = false;

        //Calculate measurement unit
        FrameLayout frameLayout = (FrameLayout) ((Activity) context).findViewById(R.id.game_frameLayout);
        LinearLayout linearLayout = (LinearLayout) ((Activity) context).findViewById(R.id.fullscreen_content_controls);
        Button button = (Button) ((Activity) context).findViewById(R.id.button1);
        int totalHeight = this.resources.getDisplayMetrics().heightPixels;
        int totalWidth = this.resources.getDisplayMetrics().widthPixels;
        int usableHeight = totalHeight
                - frameLayout.getPaddingTop()
                - linearLayout.getPaddingTop()
                - button.getPaddingTop()
                - button.getMinimumHeight()
                - button.getPaddingBottom()
                - linearLayout.getPaddingBottom()
                - frameLayout.getPaddingBottom();
        unit = usableHeight / gameBoard.length;

        //Create the illusion that the game FrameLayout content is centered.
        int usableWidth = unit * gameBoard[0].length;
        int requiredPaddingLeft = (totalWidth - usableWidth) / 2;
        frameLayout.setPadding(requiredPaddingLeft, frameLayout.getPaddingTop(), frameLayout.getPaddingRight(), frameLayout.getPaddingBottom());

        //Set up game logic
        initializePlayers();
        initialiseGameBoard();

        //Set up game views
        gameBoardView = (CustomGameBoardView) ((Activity) context).findViewById(R.id.CustomGameBoardView);
        gameBoardView.setGameBoard(gameBoard);
        playerView = (CustomPlayerView) ((Activity) context).findViewById(R.id.CustomPlayerView);
        playerView.setPlayers(players);
        playerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (players[currentPlayerIndex].getPID() == myPID) {                                             // It's my turn
                        for (Player player : players) {
                            Figure[] figures = player.getFigures();
                            for (Figure fig : figures) {
                                if (fig.getImage().getBounds().contains((int) event.getX(), (int) event.getY())) {  // Clicked on a figure
                                    if (fig.getOwner().getPID() == myPID) {                                         // It's my figure
                                        if (!dice.isDice1removed() && !dice.isDice2removed()) {                     // No dice used yet
                                            if (numberOfPlayers > 1) {
                                                // Share click with others
                                                update.setUsage(IReceiveMessage.USAGE_CLICKED_PLAYER);
                                                update.setColPosition(fig.getColPos());
                                                update.setRowPosition(fig.getRowPos());
                                                networkManager.sendMessage(update);
                                            }
                                            return handleAuthorizedClickOnFigure(fig);
                                        } else { // at least one dice has been used and the player wanted to switch to another figure
                                            Toast.makeText(context, "Du musst beide Würfel auf die selbe Figur verwenden!", Toast.LENGTH_SHORT).show();
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            }
        });

        gameBoardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (players[currentPlayerIndex].getPID() == myPID) {                                                 // It's my turn
                        if (!possibleDestinationBlocks.isEmpty()) {                                                     // There are blocks to click on
                            for (Block block : possibleDestinationBlocks) {
                                if (block.getImage().getBounds().contains((int) event.getX(), (int) event.getY())) {    // The click happened on a valid block
                                    if (numberOfPlayers > 1) {
                                        // Share click with others
                                        update.setUsage(IReceiveMessage.USAGE_CLICKED_BLOCK);
                                        update.setColPosition(block.getColPos());
                                        update.setRowPosition(block.getRowPos());
                                        networkManager.sendMessage(update);
                                    }
                                    return handleAuthorizedClickOnBlock(block.getColPos(), block.getRowPos());
                                }
                            }
                        }
                    }
                }
                return false;
            }
        });

        (((Activity) context).findViewById(R.id.button_cheater)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!figureRemoved) {
                    // check if last Player Cheated
                    if (lastPlayerCheated) {
                        // send random figure of cheater back to base
                        Toast.makeText(context, "Anschuldigung berechtigt! Cheater erwischt!!", Toast.LENGTH_SHORT).show();
                        sendRandomFigureToBase(lastPlayerIndex);
                    } else {
                        // if the player doesn't Cheat your own figure will send to the base
                        Toast.makeText(context, "Unberechtigte Anschuldigung! Deine Figur kommt in die Basis zurück!", Toast.LENGTH_SHORT).show();
                        sendRandomFigureToBase(myPID);
                    }
                    checkIfReallyWon();
                    figureRemoved = true;
                } else {
                    Toast.makeText(context, "nicht möglich!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Set everything that only one player needs to do (if you have PID=0).
        if (myPID == 0) {
            //Generate a seed for everybody
            originalSeed = (long) (Math.random() * 10000);
            currentSeed = originalSeed;

            //Update gameBoard with seed
            initialiseGameBoard();
            gameBoardView.invalidate();

            //Allow myself to roll the dice if I am current player.
            if (players[currentPlayerIndex].getPID() == myPID) {
                dice.setDiceRollAllowed(true);
                dice.setFirstDiceRollThisTurn(true);
            }
            //Share created info with others
            if (numberOfPlayers > 1) {
                update.setUsage(IReceiveMessage.USAGE_GAME_INITIALISED);
                update.setSeed(originalSeed);
                update.setIntValue(currentPlayerIndex);
                networkManager.sendMessage(update);
            }
            //Use this code to test win condition
            /*
            players[currentPlayerIndex].getFigures()[0].setPos(players[currentPlayerIndex].getFigures()[0].getGoalColPos(), players[currentPlayerIndex].getFigures()[0].getGoalRowPos());
            players[currentPlayerIndex].getFigures()[1].setPos(players[currentPlayerIndex].getFigures()[1].getGoalColPos(), players[currentPlayerIndex].getFigures()[1].getGoalRowPos());
            players[currentPlayerIndex].getFigures()[2].setPos(players[currentPlayerIndex].getFigures()[2].getGoalColPos(), players[currentPlayerIndex].getFigures()[2].getGoalRowPos());
            players[currentPlayerIndex].getFigures()[3].setPos(2,2);
            */
        }
    }

    public void setCurrentPlayerCheated(boolean cheated) {
        currentPlayerCheated = cheated;
    }

    private void sendRandomFigureToBase(int playerIndex) {
        Figure[] figures = players[playerIndex].getFigures();
        boolean figureMoved = false;

        for (Figure figure : figures) {
            if (!figureMoved && !(figure.getColPos() == figure.getBaseColPos() && figure.getRowPos() == figure.getBaseRowPos())) {
                update.setUsage(IReceiveMessage.USAGE_REMOVE_FIGURE);
                update.setColPosition(figure.getColPos());
                update.setRowPosition(figure.getRowPos());
                update.setPlayerID(playerIndex);
                networkManager.sendMessage(update);
                figure.setPos(figure.getBaseColPos(), figure.getBaseRowPos()); //Send figure back to base.
                figureMoved = true;
            }
        }
        clearPossibleDestinationBlocks();
        playerView.invalidate();
        gameBoardView.invalidate();
    }

    public Dice getDice() {
        return dice;
    }

    private boolean initialiseGameBoard() {
        for (int col = 0; col < gameBoard.length; col++) {
            for (int row = 0; row < gameBoard[col].length; row++) {
                setBlockParametersByType(gameBoard, col, row);
            }
        }
        return true;
    }

    private void setBlockParametersByType(Block[][] gameBoard, int col, int row) {
        //Create variables
        Block currentBlock = gameBoard[col][row];

        //Assign block position
        currentBlock.setColPos(col);
        currentBlock.setRowPos(row);

        //Set type specific attributes
        Drawable drawable;
        switch (currentBlock.getType()) {
            case S:
                drawable = resources.getDrawable(R.drawable.circle_white_l_arrow);
                currentBlock.setNextBlock(LEFT);
                currentBlock.setPreviousBlock(BASE);
                startColPos = col;
                startRowPos = row;
                break;
            case HR:
                drawable = resources.getDrawable(R.drawable.circle_white_rl);
                currentBlock.setNextBlock(RIGHT);
                currentBlock.setPreviousBlock(LEFT);
                break;
            case HL:
                drawable = resources.getDrawable(R.drawable.circle_white_rl);
                currentBlock.setNextBlock(LEFT);
                currentBlock.setPreviousBlock(RIGHT);
                break;
            case EU:
                drawable = resources.getDrawable(R.drawable.circle_white_ur);
                currentBlock.setNextBlock(UP);
                currentBlock.setPreviousBlock(RIGHT);
                break;
            case ER:
                drawable = resources.getDrawable(R.drawable.circle_white_rd);
                currentBlock.setNextBlock(RIGHT);
                currentBlock.setPreviousBlock(DOWN);
                break;
            case ED:
                drawable = resources.getDrawable(R.drawable.circle_white_dl);
                currentBlock.setNextBlock(LEFT);
                currentBlock.setPreviousBlock(DOWN);
                break;
            case EL:
                drawable = resources.getDrawable(R.drawable.circle_white_ul);
                currentBlock.setNextBlock(UP);
                currentBlock.setPreviousBlock(LEFT);
                break;
            case V:
                drawable = resources.getDrawable(R.drawable.circle_white_ud);
                currentBlock.setNextBlock(UP);
                currentBlock.setPreviousBlock(DOWN);
                break;
            case F:
                drawable = resources.getDrawable(R.drawable.circle_blue_l);
                currentBlock.setNextBlock(GOAL);
                currentBlock.setPreviousBlock(LEFT);
                endColPos = col;
                endRowPos = row;
                break;
            case W:
                int wallNumber = generatePseudoRandomWallNumberMinMax(1, 6);
                currentBlock.setWallNumber(wallNumber);
                switch (wallNumber) {
                    case 1:
                        drawable = resources.getDrawable(R.drawable.wall_1);
                        break;
                    case 2:
                        drawable = resources.getDrawable(R.drawable.wall_2);
                        break;
                    case 3:
                        drawable = resources.getDrawable(R.drawable.wall_3);
                        break;
                    case 4:
                        drawable = resources.getDrawable(R.drawable.wall_4);
                        break;
                    case 5:
                        drawable = resources.getDrawable(R.drawable.wall_5);
                        break;
                    case 6:
                        drawable = resources.getDrawable(R.drawable.wall_6);
                        break;
                    default:
                        drawable = resources.getDrawable(R.drawable.empty);
                }
                break;
            case N:
                drawable = resources.getDrawable(R.drawable.empty);
                break;
            case BR:
                drawable = resources.getDrawable(R.drawable.circle_red);
                currentBlock.setNextBlock(START);
                assignBlockPositionToSuitableFigure(currentBlock);
                break;
            case BG:
                drawable = resources.getDrawable(R.drawable.circle_green);
                currentBlock.setNextBlock(START);
                assignBlockPositionToSuitableFigure(currentBlock);
                break;
            case BY:
                drawable = resources.getDrawable(R.drawable.circle_yellow);
                currentBlock.setNextBlock(START);
                assignBlockPositionToSuitableFigure(currentBlock);
                break;
            case BB:
                drawable = resources.getDrawable(R.drawable.circle_black);
                currentBlock.setNextBlock(START);
                assignBlockPositionToSuitableFigure(currentBlock);
                break;
            case GR:
                drawable = resources.getDrawable(R.drawable.circle_red);
                assignBlockPositionToSuitableFigure(currentBlock);
                break;
            case GG:
                drawable = resources.getDrawable(R.drawable.circle_green);
                assignBlockPositionToSuitableFigure(currentBlock);
                break;
            case GY:
                drawable = resources.getDrawable(R.drawable.circle_yellow);
                assignBlockPositionToSuitableFigure(currentBlock);
                break;
            case GB:
                drawable = resources.getDrawable(R.drawable.circle_black);
                assignBlockPositionToSuitableFigure(currentBlock);
                break;
            default:
                drawable = resources.getDrawable(R.drawable.empty);
        }

        assert drawable != null;
        Drawable clone = drawable.getConstantState().newDrawable().mutate(); //Deep copy to avoid blocks sharing the same image.
        int lengthPos = col * unit;
        int heightPos = row * unit;
        //noinspection SuspiciousNameCombination
        clone.setBounds(heightPos, lengthPos, (heightPos + unit), (lengthPos + unit));
        currentBlock.setImage(clone);
    }

    private void initializePlayers() {
        players = new Player[numberOfPlayers];
        for (int colour = RED; colour < numberOfPlayers; colour++) {
            players[colour] = new Player(context, colour, colour); // colour is also used as PID here.
        }
    }

    /**
     * Find a figure of the right colour which has not yet set a base/goal position and assign it a block's position.
     */
    private void assignBlockPositionToSuitableFigure(Block block) {
        int colour = 0;
        boolean isBase = false;
        boolean isGoal = false;
        int col = block.getColPos();
        int row = block.getRowPos();

        //Determine suitable colour
        switch (block.getType()) {
            case BR:
                colour = RED;
                isBase = true;
                break;
            case BG:
                colour = GREEN;
                isBase = true;
                break;
            case BY:
                colour = YELLOW;
                isBase = true;
                break;
            case BB:
                colour = BLACK;
                isBase = true;
                break;
            case GR:
                colour = RED;
                isGoal = true;
                break;
            case GG:
                colour = GREEN;
                isGoal = true;
                break;
            case GY:
                colour = YELLOW;
                isGoal = true;
                break;
            case GB:
                colour = BLACK;
                isGoal = true;
                break;
        }

        if (players.length > colour) {
            boolean valuesAssigned = false;     //Keeps the block from assigning its position to more than one figure.
            for (int i = 0; i < players[colour].getFigures().length && !valuesAssigned; i++) {
                Figure figure = players[colour].getFigures()[i];
                if (isBase) {
                    if (figure.getBaseColPos() < 0) {    //Keeps the block from overriding already assigned figures.
                        figure.setBaseColPos(col);
                        figure.setBaseRowPos(row);
                        figure.setPos(col, row);
                        valuesAssigned = true;
                    }
                } else if (isGoal) {
                    if (figure.getGoalColPos() < 0) {    //Keeps the block from overriding already assigned figures.
                        figure.setGoalColPos(col);
                        figure.setGoalRowPos(row);
                        valuesAssigned = true;
                    }
                }
            }
        }
    }

    private boolean moveFigureForward(Figure figure) {
        int direction = gameBoard[figure.getColPos()][figure.getRowPos()].getNextBlock();
        return moveFigure(direction, figure);
    }

    private boolean moveFigureBackward(Figure figure) {
        int direction = gameBoard[figure.getColPos()][figure.getRowPos()].getPreviousBlock();
        return moveFigure(direction, figure);
    }

    private boolean moveFigure(int direction, Figure figure) {
        switch (direction) {
            case UP:
                figure.walkUp();
                break;
            case RIGHT:
                figure.walkRight();
                break;
            case DOWN:
                figure.walkDown();
                break;
            case LEFT:
                figure.walkLeft();
                break;
            case BASE:
                return false;
            case GOAL:
                return false;
            case START:
                figure.setPos(startColPos, startRowPos);
                break;
            default:
                return false;
        }
        return true;
    }

    public void setSelectedDiceNumber(int selectedDiceNumber, int dice1or2, boolean share) {
        if (share) {
            //Share selected dice number with others
            update.setUsage(IReceiveMessage.USAGE_DICE_SELECTED);
            update.setIntValue(dice1or2);
            if (dice1or2 == 1) {
                update.setW1(selectedDiceNumber);
            }
            if (dice1or2 == 2) {
                update.setW2(selectedDiceNumber);
            }
            networkManager.sendMessage(update);
        }
        this.selectedDiceNumber = selectedDiceNumber;
        calculatePossibleMoves();
    }

    private void calculatePossibleMoves() {
        if (selectedFigure != null && selectedDiceNumber != -1) {
            int blocksMoved; //Number of actually traversed blocks.
            Figure ghostFig = new Figure(null);                                         //Create new invisible ghost figure
            ghostFig.setPos(selectedFigure.getColPos(), selectedFigure.getRowPos());    //Place the ghost figure on the selected figure.
            clearPossibleDestinationBlocks();                                           //Clear the list to remove any blocks from previous uses.

            //Check way forward.
            //noinspection StatementWithEmptyBody
            for (blocksMoved = 0; blocksMoved < selectedDiceNumber && moveFigureForward(ghostFig); blocksMoved++) {
            }    //Move ghost figure forward for the amount on selected dice.
            if (blocksMoved == selectedDiceNumber) {                                                                    //Check if all of the possible moves were used.
                possibleDestinationBlocks.add(gameBoard[ghostFig.getColPos()][ghostFig.getRowPos()]);                   //Add ghost figures position as new possible destination block.
            }
            ghostFig.setPos(selectedFigure.getColPos(), selectedFigure.getRowPos());                                    //Reset ghost figures position.

            //Check way backward.
            //noinspection StatementWithEmptyBody
            for (blocksMoved = 0; blocksMoved < selectedDiceNumber && moveFigureBackward(ghostFig); blocksMoved++) {
            }   //Move ghost figure backward for the amount on selected dice.
            if (blocksMoved == selectedDiceNumber) {                                                                    //Check if all of the possible moves were used.
                possibleDestinationBlocks.add(gameBoard[ghostFig.getColPos()][ghostFig.getRowPos()]);                   //Add ghost figures position as new possible destination block.
            }
            ghostFig.setPos(selectedFigure.getColPos(), selectedFigure.getRowPos());                                    //Reset ghost figures position.

            //Check way up.
            if (ghostFig.getColPos() - 1 > 0) {                                                             //Check if there is a block above.
                ghostFig.walkUp();                                                                          //Walk up to potentially find a wall block.
                if (gameBoard[ghostFig.getColPos()][ghostFig.getRowPos()].getWallNumber() == selectedDiceNumber) {
                    ghostFig.walkUp();                                                                      //If the block is a wall block and the number fits the dice, then "jump" over the wall.
                    possibleDestinationBlocks.add(gameBoard[ghostFig.getColPos()][ghostFig.getRowPos()]);   //Add ghost figures position as new possible destination block.
                }
                ghostFig.setPos(selectedFigure.getColPos(), selectedFigure.getRowPos());                    //Reset ghost figures position.
            }

            //Check way down.
            if (ghostFig.getColPos() + 1 < gameBoard.length) {                                              //Check if there is a block below.
                ghostFig.walkDown();                                                                        //Walk down to potentially find a wall block.
                if (ghostFig.getColPos() < gameBoard.length && gameBoard[ghostFig.getColPos()][ghostFig.getRowPos()].getWallNumber() == selectedDiceNumber) {
                    ghostFig.walkDown();                                                                    //If the block is a wall block and the number fits the dice, then "jump" over the wall.
                    possibleDestinationBlocks.add(gameBoard[ghostFig.getColPos()][ghostFig.getRowPos()]);   //Add ghost figures position as new possible destination block.
                }
                ghostFig.setPos(selectedFigure.getColPos(), selectedFigure.getRowPos());                    //Reset ghost figures position.
            }

/*
            //Check for trapped figure
            System.out.println("possibleDestinationBlocks.size()" + possibleDestinationBlocks.size());
            boolean trapped = false;
            if (dice.isDice1removed() || dice.isDice2removed()) {
                trapped = true;
                for (Block block : possibleDestinationBlocks) {
                    if (block.getColPos() != selectedFigure.getColPos() || block.getRowPos() != selectedFigure.getRowPos()){
                        trapped = false;
                    }
                }
            }
            if (trapped) {
                if (players[currentPlayerIndex].getPID() == myPID){
                    Toast.makeText(context, "Du kannst nicht mehr ziehen. Zug wird beendet.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, playerName + " kann nicht mehr ziehen. Zug wird beendet.", Toast.LENGTH_SHORT).show();
                }
                startNextTurn();
            }
*/

            //Highlight possible destination blocks
            for (Block block : possibleDestinationBlocks) {

                //Highlight block
                block.getImage().setColorFilter(FilterColor, FilterMode);

                //Check for occupying players
                for (Player player : players) {
                    for (Figure figure : player.getFigures()) {
                        if (figure.getColPos() == block.getColPos() && figure.getRowPos() == block.getRowPos()) {
                            //There is a figure on the block.
                            if (figure.getOwner().getPID() == players[currentPlayerIndex].getPID()) {   //It's one of my own figures.
                                block.getImage().clearColorFilter();                                    //Remove the highlighting of the block underneath.
                            } else {                                                                    //It's an opponent's figure.
                                figure.getImage().setColorFilter(FilterColor, FilterMode);              //Highlight opponent's figure.
                                playerView.invalidate();
                            }
                        }
                    }
                }
            }
            gameBoardView.invalidate();
        }
    }

    private void moveSelectedFigureAndTidyUp(int col, int row) {
        clearSelectedDiceImage();
        selectedFigure.setPos(col, row);
        checkAndHandleFigureCollision();
        tryMovingSelectedFigureIntoRespectiveGoal();
        clearPossibleDestinationBlocks();
        selectedDiceNumber = -1;

        playerView.invalidate();
        gameBoardView.invalidate();


        if (dice.isDice1removed() && dice.isDice2removed()) { // If both dice are used -> start next turn.
            startNextTurn();
        }
    }

    private void clearSelectedDiceImage() {
        if (dice.isDice1Selected()) {
            dice.dice1Used();
        }
        if (dice.isDice2Selected()) {
            dice.dice2Used();
        }
    }

    private void checkAndHandleFigureCollision() {
        int colPos = selectedFigure.getColPos();
        int rowPos = selectedFigure.getRowPos();
        int PID = selectedFigure.getOwner().getPID();
        for (Player player : players) {
            for (Figure figure : player.getFigures()) {
                if (colPos == figure.getColPos() && rowPos == figure.getRowPos() && PID != figure.getOwner().getPID()) {
                    figure.setPos(figure.getBaseColPos(), figure.getBaseRowPos()); //Send figure back to base.
                    figure.getImage().clearColorFilter(); //Remove highlighting from figure.
                }
            }
        }
    }

    private void tryMovingSelectedFigureIntoRespectiveGoal() {
        if (selectedFigure.getColPos() == endColPos && selectedFigure.getRowPos() == endRowPos) {
            if (dice.isDice1removed() && dice.isDice2removed()) {
                selectedFigure.setPos(selectedFigure.getGoalColPos(), selectedFigure.getGoalRowPos());
                checkForWinCondition();
            } else if (players[currentPlayerIndex].getPID() == myPID) { //It's my turn
                Toast.makeText(context, "Du musst beide Würfel verwenden um ins Ziel zu gelangen!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkForWinCondition() {
        int figuresInGoal;
        for (Player player : players) {
            figuresInGoal = 0;
            for (Figure figure : player.getFigures()) {
                if (figure.getColPos() == figure.getGoalColPos() && figure.getRowPos() == figure.getGoalRowPos()) {
                    figuresInGoal++;
                }
            }
            if (figuresInGoal >= player.getFigures().length) {
                if (players[currentPlayerIndex].getPID() == myPID) {
                    Toast.makeText(context, "Glückwunsch, du hast gewonnen!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, playerName + " hat gewonnen!", Toast.LENGTH_LONG).show();
                }
                myPID -= 5; // Basically stops everybody from having a turn.
                gameWon = true;
            }
        }
    }

    public void clearPossibleDestinationBlocks() {
        //Remove highlighting from blocks.
        for (Block block : possibleDestinationBlocks) {
            block.getImage().clearColorFilter();
        }
        //Remove highlighting from opponent's figures.
        for (Player player : players) {
            for (Figure figure : player.getFigures()) {
                if (figure.getOwner().getPID() != players[currentPlayerIndex].getPID()) {
                    figure.getImage().clearColorFilter();
                }
            }
        }
        playerView.invalidate();
        gameBoardView.invalidate();

        //Empty the container for possible destination blocks.
        possibleDestinationBlocks.clear();
    }

    private void increaseCurrentPlayerIndex() {
        lastPlayerIndex = currentPlayerIndex;
        if (currentPlayerIndex + 1 >= numberOfPlayers) {
            currentPlayerIndex = 0;
        } else {
            currentPlayerIndex++;
        }
    }

    public void startNextTurn() {
        //Cheat
        lastPlayerCheated = currentPlayerCheated;
        currentPlayerCheated = false;
        figureRemoved = false;

        //Reset selected figure.
        selectedFigure.getImage().clearColorFilter();
        selectedFigure = null;

        //Reset dices.
        dice.setDice1removed(false);
        dice.setDice2removed(false);
        dice.setDiceRollAllowed(false);

        //Next player gets his turn.
        increaseCurrentPlayerIndex();

        //Allow me to roll dice if I am the current player now.
        checkIfMyTurn();
        //Update view
        playerView.invalidate();
    }

    private boolean handleAuthorizedClickOnFigure(Figure fig) {
        if (fig != null) {
            if (selectedFigure != null) {
                // Deselect previous selected figure.
                selectedFigure.getImage().clearColorFilter();
            }
            if (fig == selectedFigure) {
                // Deselect selected figure.
                selectedFigure.getImage().clearColorFilter();
                clearPossibleDestinationBlocks();
                selectedFigure = null;
                playerView.invalidate();
                gameBoardView.invalidate();
            } else {
                // Select unselected figure.
                selectedFigure = fig;
                selectedFigure.getImage().setColorFilter(FilterColor, FilterMode); //Change new selected figure
                playerView.invalidate();
                calculatePossibleMoves();
            }
            return true;
        }
        return false;
    }

    private boolean handleAuthorizedClickOnBlock(int col, int row) {
        moveSelectedFigureAndTidyUp(col, row);
        return true;
    }

    public void handleUpdate(UpdateState update) {
        switch (update.getUsage()) {
            case IReceiveMessage.USAGE_GAME_INITIALISED:
                originalSeed = update.getSeed();
                currentSeed = originalSeed;
                initialiseGameBoard();
                gameBoardView.invalidate();
                currentPlayerIndex = update.getIntValue();

                //Allow myself to roll the dice if I am current player.
                if (players[currentPlayerIndex].getPID() == myPID) {
                    dice.setDiceRollAllowed(true);
                    dice.setFirstDiceRollThisTurn(true);
                    dice.setHasCheated(false);
                }
                break;
            case IReceiveMessage.USAGE_CLICKED_PLAYER:
                for (Player player : players) {
                    for (Figure figure : player.getFigures()) {
                        if (figure.getColPos() == update.getColPosition() && figure.getRowPos() == update.getRowPosition()) {
                            handleAuthorizedClickOnFigure(figure); // Simulate click on figure
                        }
                    }
                }
                break;
            case IReceiveMessage.USAGE_CLICKED_BLOCK:
                handleAuthorizedClickOnBlock(update.getColPosition(), update.getRowPosition()); // Simulate click on block
                break;
            case IReceiveMessage.USAGE_DICE_SELECTED:
                if (update.getIntValue() == 1) {
                    dice.setDice1Selected(true);
                    dice.setDice2Selected(false);
                    setSelectedDiceNumber(update.getW1(), update.getIntValue(), false);
                }
                if (update.getIntValue() == 2) {
                    dice.setDice1Selected(false);
                    dice.setDice2Selected(true);
                    setSelectedDiceNumber(update.getW2(), update.getIntValue(), false);
                }
                break;
            case IReceiveMessage.USAGE_DICE_ROLLED:
                dice.setDiceImage(update.getW1(), 1);
                dice.setDiceImage(update.getW2(), 2);
                break;
            case IReceiveMessage.USAGE_CHEATED:
                currentPlayerCheated = true;
                break;
            case IReceiveMessage.USAGE_REMOVE_FIGURE:
                Figure[] figures = players[update.getPlayerID()].getFigures();
                for (Figure figure : figures) {
                    if ((figure.getColPos() == update.getColPosition()) && (figure.getRowPos() == update.getRowPosition())) {
                        figure.setPos(figure.getBaseColPos(), figure.getBaseRowPos()); //Send figure back to base.
                        figureRemoved = true;
                        clearPossibleDestinationBlocks();
                        playerView.invalidate();
                        gameBoardView.invalidate();
                        checkIfReallyWon();
                    }
                }
                break;
        }
    }

    private void checkIfReallyWon() {
        if (gameWon && lastPlayerCheated) {
            myPID += 5;
            gameWon = false;
            Toast.makeText(context, "Schummler gewinnen nicht! Weiter gehts!", Toast.LENGTH_SHORT).show();
            checkIfMyTurn();
        }
    }

    private void checkIfMyTurn() {
        if (players[currentPlayerIndex].getPID() == myPID) {
            dice.setDiceRollAllowed(true);
            dice.setFirstDiceRollThisTurn(true);
            dice.setHasCheated(false);
            Toast.makeText(context, "Du bist an der Reihe.", Toast.LENGTH_SHORT).show();
        }
    }

    public int generatePseudoRandomWallNumberMinMax(int min, int max) {
        Random generator = new Random(currentSeed);
        double result = (Math.abs((generator.nextLong() % 0.001) * 1000));
        int wallNumber = min + (int) (result * ((max - min) + 1));
        currentSeed--;
        return wallNumber;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public Player[] getPlayers() {
        return players;
    }

    public int getMyPID() {
        return myPID;
    }

    public boolean isGameWon() {
        return gameWon;
    }
}
