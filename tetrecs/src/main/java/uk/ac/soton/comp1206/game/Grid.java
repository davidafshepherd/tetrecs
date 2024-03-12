package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }


    /**
     * Checks if a game piece can be placed in this grid
     * @param gamePiece game piece to be checked
     * @param x column where centre of game piece is to be placed in
     * @param y row where centre of game piece is to be placed in
     * @return true if game piece can be placed and false if it can't
     */
    public boolean canPlayPiece(GamePiece gamePiece, int x, int y) {
        //gets the game piece's blocks
        var gamePieceBlocks = gamePiece.getBlocks();

        //checks if each block stores a value
        for (int j = -1; j < 2; j++) {
            for (int i = -1; i < 2; i++) {
                if (gamePieceBlocks[i+1][j+1] != 0) {
                    //If so, checks if where that block would be placed in this grid also stores a value
                    if (this.get(x+i, y+j) != 0) {
                        //If so, returns false as the game piece can't be placed in this grid at column x and row y
                        logger.info(gamePiece + " can't be placed at column " + x + " and row " + y);
                        return false;
                    }
                }
            }
        }

        //Otherwise, returns true as game piece can be placed in this grid at column x and row y
        logger.info(gamePiece + " can be placed at column " + x + " and row " + y);
        return true;
    }

    /**
     * Places a game piece in this grid
     * @param gamePiece game piece to be placed
     * @param x column where centre of game piece is being placed in
     * @param y row where centre of game piece is being placed in
     */
    public void playPiece(GamePiece gamePiece, int x, int y) {
        logger.info("Placing " + gamePiece + " at " + x + " and " + y);

        //gets the game piece's blocks
        var gamePieceBlocks = gamePiece.getBlocks();

        //checks if each block stores a value
        for (int j = -1; j < 2; j++) {
            for (int i = -1; i < 2; i++) {
                if (gamePieceBlocks[i+1][j+1] != 0) {
                    //If so it places that block in this grid at column x and row y
                    this.set(x+i, y+j, gamePiece.getValue());
                }
            }
        }
    }
}
