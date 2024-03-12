package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.Set;

/**
 * The Line Cleared listener is used to handle the event when a line in a Game is cleared.
 */
public interface LineClearedListener {

    /**
     * Handles a line cleared event
     * @param gameBlockCoordinates coordinates of game blocks in that line
     */
    public void lineCleared(Set<GameBlockCoordinate> gameBlockCoordinates);
}
