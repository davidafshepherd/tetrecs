package uk.ac.soton.comp1206.event;

/**
 * The Score Requested listener is used to handle the event when last game's score is requested.
 */
public interface ScoreRequestedListener {

    /**
     * Handles a score requested event
     */
    int scoreRequested();
}


