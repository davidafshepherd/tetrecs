package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;

/**
 * The Game Loop listener is used to handle the event when the timer in a Game reaches 0.
 */
public interface GameLoopListener {

    /**
     * Handle a game loop event
     */
    public void gameLoop();
}
