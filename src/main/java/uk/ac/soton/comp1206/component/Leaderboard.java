package uk.ac.soton.comp1206.component;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a leaderboard in the multiplayer scene.
 * Extends ScoresList.
 * Displays an ordered, rainbow coloured list of scores and the players who have those scores.
 * If a player is out of lives, their entry in the leaderboard is crossed out.
 */
public class Leaderboard extends ScoresList {

    private static final Logger logger = LogManager.getLogger(Leaderboard.class);

    /**
     * List of player names and their number of lives/if they are dead
     */
    protected final SimpleListProperty<Pair<String, String>> lifeStatuses;

    /**
     * Creates a new Leaderboard
     */
    public Leaderboard() {
        super();
        this.getStyleClass().add("leaderboard");
        lifeStatuses = new SimpleListProperty<>();
        lifeStatuses.addListener((ListChangeListener<? super Pair<String, String>>) change -> this.trackLives());
    }

    /**
     * Crosses out any entry in this leaderboard if the entry's player has run out of lives
     */
    public void trackLives() {
        logger.info("Crossing out any dead players");

        //Loops through each entry in this leaderboard
        for (int i = 0; i < scoreDisplays.size(); i++) {
            //Checks if the entry's player is dead
            if (lifeStatuses.get(i).getValue().equals("DEAD")) {
                //If so crosses their name out
                scoreDisplays.get(i).getStyleClass().add("dead-score");
            }
        }
    }

    /**
     * Gets the life statuses property
     * @return the life statuses property
     */
    public SimpleListProperty<Pair<String, String>> getLifeStatusesProperty() {
        return lifeStatuses;
    }
}
