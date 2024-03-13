package uk.ac.soton.comp1206.event;

import javafx.beans.property.ListProperty;
import javafx.util.Pair;

/**
 * The Multiplayer Scores listener is used to handle the event when last multiplayer game's scores are requested.
 */
public interface MultiplayerScoresListener {

    /**
     * Handles a multiplayer scores event
     */
    ListProperty<Pair<String, Integer>> multiplayerScores();
}
