package uk.ac.soton.comp1206.event;

/**
 * The Game Loop listener is used to handle the event when the timer in a Game reaches 0.
 */
public interface GameLoopListener {

    /**
     * Handles a game loop event
     */
    void gameLoop();
}
