package uk.ac.soton.comp1206.event;

import javafx.beans.property.SimpleStringProperty;

/**
 * The Name Requested listener is used to handle the event when the player's name is requested.
 */
public interface NameRequestedListener {

    /**
     * Handles a name requested event
     */
    public SimpleStringProperty nameRequested();
}
