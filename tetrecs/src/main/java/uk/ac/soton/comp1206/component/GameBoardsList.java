package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Grid;

import java.util.ArrayList;

/**
 * The Visual User Interface component representing a game boards list in the multiplayer scene.
 * Extends VBox.
 * Displays an ordered, list of game boards and the players who have those game boards.
 * If a player is out of lives, their entry in the game boards list is faded out.
 */
public class GameBoardsList extends VBox {

    private static final Logger logger = LogManager.getLogger(GameBoardsList.class);

    /**
     * List of player names and their game board values
     */
    private final SimpleListProperty<Pair<String, Grid>> gameBoardsList;

    /**
     * List of this game boards list's game board displays
     */
    protected final ArrayList<VBox> gameBoardDisplays = new ArrayList<>();

    /**
     * The player's name
     */
    private final SimpleStringProperty playerName;

    /**
     * List of player names and their number of lives/if they are dead
     */
    protected final SimpleListProperty<Pair<String, String>> lifeStatuses;


    /**
     * Creates a new game boards list
     */
    public GameBoardsList() {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(15);
        gameBoardsList = new SimpleListProperty<>();
        gameBoardsList.addListener((ListChangeListener<? super Pair<String, Grid>>) change -> build());
        playerName = new SimpleStringProperty();
        lifeStatuses = new SimpleListProperty<>();
        lifeStatuses.addListener((ListChangeListener<? super Pair<String, String>>) change -> this.trackLives());
    }

    /**
     * Builds the game boards list
     */
    public void build() {
        logger.info("Building game boards list");

        //Removes all the game boards currently in this game boards list
        this.getChildren().clear();
        gameBoardDisplays.clear();

        //Adds each grid in the updated game boards list property to be displayed in this game boards list
        for (var gameBoard : gameBoardsList) {
            var entry = new VBox();
            entry.setAlignment(Pos.CENTER);
            var entryHeading = new Text(gameBoard.getKey());
            entryHeading.getStyleClass().add("heading");
            var entryBoard = new GameBoard(gameBoard.getValue(), 100, 100);
            entry.getChildren().addAll(entryHeading, entryBoard);
            this.getChildren().add(entry);
            gameBoardDisplays.add(entry);

            //Highlights game board heading if game board is the player's game board
            if (entryHeading.getText().equals(playerName.get())) {
                entryHeading.getStyleClass().add("player-heading");
            }
        }

        //Fades out any game board in this game boards list if the game board's player has run out of lives
        this.trackLives();

        //Reveals the game boards list
        this.reveal();
    }

    /**
     * Fades out any game board in this game boards list if the game board's player has run out of lives
     */
    public void trackLives() {
        logger.info("Fading out any dead players");

        //Loops through each display in this game boards list
        for (int i = 0; i < gameBoardDisplays.size(); i++) {
            //Checks if a player is dead
            if (!lifeStatuses.isEmpty() && lifeStatuses.get(i).getValue().equals("DEAD")) {
                //If so fades out their game board display
                gameBoardDisplays.get(i).setOpacity(0.3);
            }
        }
    }

    /**
     * Gets the game boards list property
     * @return the game boards list property
     */
    public SimpleListProperty<Pair<String, Grid>> getGameBoardsProperty() {
        return gameBoardsList;
    }

    /**
     * Gets the player name property
     * @return the playerName property
     */
    public SimpleStringProperty getPlayerNameProperty() {
        return playerName;
    }

    /**
     * Gets the life statuses property
     * @return the life statuses property
     */
    public SimpleListProperty<Pair<String, String>> getLifeStatusesProperty() {
        return lifeStatuses;
    }

    /**
     * Animates the game boards in this list to fade in one after the other
     */
    public void reveal() {
        logger.info("Revealing game boards");

        //Creates a sequential transition to store each game board's animation
        var reveal = new SequentialTransition();

        //Animates each game board and adds them to the sequential transition
        for (var gameBoard : this.getChildren()) {
            var fade = new FadeTransition(new Duration(300), gameBoard);
            fade.setFromValue(0);
            fade.setToValue(gameBoard.getOpacity());
            reveal.getChildren().add(fade);
        }

        //Plays the animation
        reveal.play();
    }
}
