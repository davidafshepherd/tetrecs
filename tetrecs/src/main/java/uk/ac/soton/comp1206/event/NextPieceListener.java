package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;

/**
  The Next Piece listener is used to handle the event when the next game piece in a Game is created.
 */
public interface NextPieceListener {

    /**
     * Handles a next piece event
     * @param currentPiece the current game piece
     * @param followingPiece the next game piece
     */
    public void nextPiece(GamePiece currentPiece, GamePiece followingPiece);
}
