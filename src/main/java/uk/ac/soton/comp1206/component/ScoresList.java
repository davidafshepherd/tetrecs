package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

/**
 * The Visual User Interface component representing a scores list in the scores scene.
 * Extends VBox.
 * Displays an ordered, rainbow coloured list of scores and the players who achieved those scores.
 */
public class ScoresList extends VBox {

    private static final Logger logger = LogManager.getLogger(ScoresList.class);

    /**
     * List of player names and their scores
     */
    protected final SimpleListProperty<Pair<String, Integer>> scoresList;

    /**
     * List of this scores list's score displays
     */
    protected final ArrayList<Text> scoreDisplays = new ArrayList<>();

    /**
     * The player's name
     */
    protected final SimpleStringProperty playerName;

    /**
     * The player's score
     */
    protected final SimpleIntegerProperty playerScore;

    /**
     * Colour scheme to colour each entry in this score list
     */
    protected static final Color[] scoreColours = {Color.DEEPPINK, Color.RED, Color.ORANGE, Color.YELLOW, Color.YELLOWGREEN, Color.LIME, Color.GREEN, Color.DARKGREEN, Color.AQUA, Color.DEEPSKYBLUE};

    /**
     * Creates a new scores list
     */
    public ScoresList() {
        this.getStyleClass().add("score-list");
        this.setAlignment(Pos.CENTER);
        scoresList = new SimpleListProperty<>();
        scoresList.addListener((ListChangeListener<? super Pair<String, Integer>>) change -> build());
        playerName = new SimpleStringProperty();
        playerScore = new SimpleIntegerProperty();
    }

    /**
     * Builds the scores list
     */
    public void build() {
        logger.info("Building scores list");

        //Removes all the scores currently being shown in this scores list
        scoreDisplays.clear();
        this.getChildren().clear();

        //Colours and adds each score in the updated scores list property to be displayed in this scores list
        for(int i = 0; i < scoresList.getSize(); i++) {
            var score = new Text(scoresList.get(i).getKey() + ": " + scoresList.get(i).getValue());
            score.setFill(scoreColours[i % 10]);
            scoreDisplays.add(score);
            this.getChildren().add(score);

            //Highlights score if score is the player's score
            if (scoresList.get(i).getKey().equals(playerName.get()) && scoresList.get(i).getValue() == playerScore.get()) {
                score.getStyleClass().add("my-score");
            }
        }

        //Reveals the scores list
        this.reveal();
    }

    /**
     * Gets the scores list property
     * @return the scores list property
     */
    public SimpleListProperty<Pair<String, Integer>> getScoresListProperty() {
        return scoresList;
    }

    /**
     * Gets the player mame property
     * @return the player name property
     */
    public SimpleStringProperty getPlayerNameProperty() {
        return playerName;
    }

    /**
     * Gets the score property
     * @return the score property
     */
    public SimpleIntegerProperty getScoreProperty() {
        return playerScore;
    }

    /**
     * Animates the scores in this list to fade in one after the other
     */
    public void reveal() {
        logger.info("Revealing scores");

        //Creates a sequential transition to store each score's animation
        var reveal = new SequentialTransition();

        //Animates each score and adds them to the sequential transition
        for(var scorePair : this.getChildren()) {
            var fade = new FadeTransition(new Duration(300), scorePair);
            fade.setFromValue(0);
            fade.setToValue(1);
            reveal.getChildren().add(fade);
        }

        //Plays the animation
        reveal.play();
    }
}
