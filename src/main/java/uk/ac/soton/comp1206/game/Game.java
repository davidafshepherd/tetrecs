package uk.ac.soton.comp1206.game;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * The current game piece that the player has
     */
    private GamePiece currentPiece;

    /**
     * The next game piece that the player will be given
     */
    private GamePiece followingPiece;

    /**
     * The listener to call when the next game piece is created
     */
    private NextPieceListener nextPieceListener;

    /**
     * The listener to call when a line is cleared
     */
    private LineClearedListener lineClearedListener;

    /**
     * The listener to call when the timer resets
     */
    private GameLoopListener gameLoopListener;

    /**
     * The score value, used for binding to UI components
     */
    protected final SimpleIntegerProperty score = new SimpleIntegerProperty(0);

    /**
     * The value the score value is about to become, used for calculations and access the score value
     */
    protected SimpleIntegerProperty newScore = new SimpleIntegerProperty(0);

    /**
     * The level number
     */
    private final SimpleIntegerProperty level = new SimpleIntegerProperty(0);

    /**
     * The number of lives that the player has
     */
    protected final SimpleIntegerProperty lives = new SimpleIntegerProperty(3);

    /**
     * The multiplier value
     */
    private final SimpleIntegerProperty multiplier = new SimpleIntegerProperty(1);

    /**
     * The timer
     */
    private ScheduledExecutorService executor;


    /**
     * Creates a new game with the specified rows and columns
     * Creates a corresponding grid model
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }

    /**
     * Gets the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Gets the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Gets the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Starts the game
     */
    public void start() {
        logger.info("Starting game");
        this.initialiseGame();
    }

    /**
     * Initialises a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");

        //Creates a game piece and sets it as the current game piece
        currentPiece = this.spawnPiece();

        //Creates a game piece and sets it as the next game piece
        followingPiece = this.spawnPiece();

        //Updates the current and next game pieces displays
        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(currentPiece, followingPiece);
        }

        //Initialises the timer to repeat the game loop at the interval specified by the getTimeDelay() method
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> Platform.runLater(this::gameLoop), this.getTimerDelay(), this.getTimerDelay(), TimeUnit.MILLISECONDS);
    }

    /**
     * Handles what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        var x = gameBlock.getX();
        var y = gameBlock.getY();

        //Checks if the current game piece can be placed at the game block that was just clicked
        if (grid.canPlayPiece(currentPiece, x, y)) {
            //If so, places the game piece there
            grid.playPiece(currentPiece, x, y);
            Multimedia.playAudio("sounds/place.wav");

            //Clears any lines if needed after placing the current game piece and updates the score, level and multiplier if needed
            this.afterPiece();

            //Sets the next game piece as the current game piece and sets a new game piece as the next game piece
            this.nextPiece();
        } else {
            Multimedia.playAudio("sounds/fail.wav");
        }
    }

    /**
     * Creates a new random game piece
     * @return game piece created
     */
    public GamePiece spawnPiece() {
        logger.info("Spawning new game piece");

        //Creates a new random game piece using a random generated integer that is between 0 and 14
        var rand = new Random();
        return GamePiece.createPiece(rand.nextInt(15));
    }

    /**
     * Sets the next game piece as the current game piece and sets a new game piece as the next game piece
     */
    public void nextPiece() {
        logger.info("Getting next game piece");

        //Sets the next game piece as the current game piece
        currentPiece = followingPiece;

        //Creates a new game piece and sets it as the next game piece
        followingPiece = this.spawnPiece();

        //Updates the current and next game pieces displays
        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(currentPiece, followingPiece);
        }
    }

    /**
     * Handles what should happen when a game piece is placed
     */
    public void afterPiece() {
        //Stores number of lines that are full
        var fullLines = 0;

        //Stores the x and y coordinates of the blocks that need to be cleared
        var blocksToBeCleared = new HashSet<GameBlockCoordinate>();

        //Checks if each row is full
        logger.info("Checking if any horizontal lines need to be cleared");
        for (int j = 0; j < this.getRows(); j++) {
            int blocksInRow = 0;
            for (int i = 0; i < this.getCols(); i++) {
                if (grid.get(i, j) != 0) {
                    blocksInRow += 1;
                }
            }
            //If a row is full, adds each block in that row to the hash set to later be reset
            if (blocksInRow == this.getCols()) {
                for (int i = 0; i < this.getCols(); i++) {
                    blocksToBeCleared.add(new GameBlockCoordinate(i, j));
                }
                //Increments number of full lines
                fullLines ++;
            }
        }

        //Checks if each column is full
        logger.info("Checking if any vertical lines need to be cleared");
        for (int i = 0; i < this.getCols(); i++) {
            int blocksInColumn = 0;
            for (int j = 0; j < this.getRows(); j++) {
                if (grid.get(i, j) != 0) {
                    blocksInColumn += 1;
                }
            }
            //If a column is full, adds each block in that column to the hash set, if they aren't in it already, to later be reset
            if (blocksInColumn == this.getCols()) {
                for (int j = 0; j < this.getCols(); j++) {
                    blocksToBeCleared.add(new GameBlockCoordinate(i, j));
                }
                //Increments number of full lines
                fullLines ++;
            }
        }
        logger.info(fullLines + " lines need to be cleared");


        //Clears any lines needed and updates the score, multiplier and level if needed
        if (fullLines != 0) {
            //Clears each block in a full line
            for (GameBlockCoordinate i: blocksToBeCleared) {
                grid.set(i.getX(), i.getY(), 0);
                logger.info("Clearing (" + i.getX() + ", " + i.getY() + ") block");
            } if (lineClearedListener != null) {
                lineClearedListener.lineCleared(blocksToBeCleared);
            }
            Multimedia.playAudio("sounds/clear.wav");

            //Updates score based on the number of lines and blocks that were just cleared
            this.score(fullLines, blocksToBeCleared.size());

            //Increments the multiplier as lines were just cleared
            multiplier.set(multiplier.get() + 1);
            logger.info("Multiplier has been updated");

            //Updates level based on the current score
            if (level.get() != newScore.get()/1000) {
                level.set(newScore.get() / 1000);
                Multimedia.playAudio("sounds/level.wav");
                logger.info("Level has been updated");
            }
        } //Otherwise, resets the multiplier as no lines were just cleared
        else {
            multiplier.set(1);
            logger.info("Multiplier has been updated");
        }

        //Resets the timer
        executor.shutdownNow();
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> Platform.runLater(this::gameLoop), this.getTimerDelay(), this.getTimerDelay(), TimeUnit.MILLISECONDS);

        //Resets the timer bar
        if (gameLoopListener != null) {
            gameLoopListener.gameLoop();
        }
    }

    /**
     * Updates the score based on the number of lines and blocks that were just cleared
     * @param lines number of lines cleared
     * @param blocks number of blocks reset
     */
    public void score(int lines, int blocks) {
        logger.info("Updating score based on " + lines + "lines and " + blocks + " blocks cleared");

        //Calculates the new score based on the number of lines and blocks that were just cleared
        newScore.set(newScore.get() + (lines * blocks * 10 * multiplier.get()));

        //Updates the score and animates it increasing
        var timeLine = new Timeline();
        timeLine.getKeyFrames().add(
                new KeyFrame(Duration.millis(1000), new KeyValue(score, newScore.get()))
        );
        timeLine.play();
    }

    /**
     * Sets a listener to handle an event when a line is cleared
     * @param listener the listener to add
     */
    public void setLineClearedListener(LineClearedListener listener) {
        this.lineClearedListener = listener;
    }

    /**
     * Gets the score property
     * @return score property
     */
    public SimpleIntegerProperty getScoreProperty() {
        return score;
    }

    /**
     * Gets the new score property
     * @return new score property
     */
    public SimpleIntegerProperty getNewScoreProperty() {
        return newScore;
    }

    /**
     * Gets the level property
     * @return level property
     */
    public SimpleIntegerProperty getLevelProperty() {
        return level;
    }

    /**
     * Gets the lives property
     * @return lives property
     */
    public SimpleIntegerProperty getLivesProperty() {
        return lives;
    }

    /**
     * Gets the multiplier property
     * @return multiplier property
     */
    public SimpleIntegerProperty getMultiplierProperty() {
        return multiplier;
    }


    /**
     * Sets a listener to handle an event when the next game piece is created
     * @param listener the listener to add
     */
    public void setNextPieceListener(NextPieceListener listener) {
        this.nextPieceListener = listener;
    }

    /**
     * Rotates the current game piece the given number of rotations
     * @param rotations number of rotations
     */
    public void rotateCurrentPiece(int rotations) {
        logger.info("Rotating current game piece");

        //Rotates the current game piece
        currentPiece.rotate(rotations);

        //Plays a sound effect
        Multimedia.playAudio("sounds/rotate.wav");

        //Updates the current and next game pieces displays
        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(currentPiece, followingPiece);
        }
    }

    /**
     * Swaps the current piece with the following piece
     */
    public void swapCurrentPiece() {
        logger.info("Swapping current game piece with the following game piece");

        //Swaps the current game piece with the following game piece
        var tempPiece = currentPiece;
        currentPiece = followingPiece;
        followingPiece = tempPiece;

        //Plays a sound effect
        Multimedia.playAudio("sounds/rotate.wav");

        //Updates the current and next game pieces displays
        if (nextPieceListener != null) {
            nextPieceListener.nextPiece(currentPiece, followingPiece);
        }
    }

    /**
     * Gets the timer delay
     * @return the timer delay
     */
    public int getTimerDelay() {
        //Calculates the timer delay according to the level
        var timerDelay = 12000 - (500 * level.get());

        //Sets timer delay to 2500 milliseconds if it is below 2500 milliseconds
        if (timerDelay < 2500) {
            timerDelay = 2500;
        }

        //Returns the timer delay
        return timerDelay;
    }

    /**
     * Gets the game timer
     * @return the game timer
     */
    public ScheduledExecutorService getTimer() {
        return executor;
    }

    /**
     * Sets a listener to handle an event when the timer reaches 0
     * @param listener the listener to add
     */
    public void setOnGameLoop(GameLoopListener listener) {
        this.gameLoopListener = listener;
    }

    /**
     * Handles what should happen when the timer reaches 0
     */
    public void gameLoop() {
        logger.info("Timer reached 0");

        //Decrements the lives by 1
        lives.set(lives.get() - 1);
        Multimedia.playAudio("sounds/lifelose.wav");

        //Discards the current game piece
        this.nextPiece();

        //Resets the multiplier to 1
        multiplier.set(1);

        //Resets the timer bar
        if (gameLoopListener != null) {
            gameLoopListener.gameLoop();
        }
    }
}
