package uk.ac.soton.comp1206.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * A PieceBoard is a visual component that displays a grid.
 * It can set a GamePiece to be displayed at the centre of this grid.
 */
public class PieceBoard extends GameBoard {

    private static final Logger logger = LogManager.getLogger(PieceBoard.class);

    /**
     * Creates a new PieceBoard, based off a given grid, with a visual width and height
     * @param grid linked grid
     * @param width the visual width
     * @param height the visual height
     */
    public PieceBoard(Grid grid, double width, double height) {
        super(grid, width, height);
    }

    /**
     * Displays a GamePiece within this PieceBoard
     * @param gamePiece GamePiece to display
     */
    public void setPieceToDisplay(GamePiece gamePiece) {
        logger.info("Setting " + gamePiece + " to be displayed at the centre of " + this + "piece board");

        //Clears the piece board's grid
        for (int j = 0; j < grid.getRows(); j++) {
            for (int i = 0; i < grid.getCols(); i++) {
                grid.set(i, j, 0);
            }
        }

        //Displays gamePiece within the centre of the piece board's grid
        grid.playPiece(gamePiece, grid.getCols()/2, grid.getRows()/2);

        //Draws indicator in the center of the piece board
        this.getBlock(grid.getCols()/2, grid.getRows()/2).drawIndicator();
    }
}
