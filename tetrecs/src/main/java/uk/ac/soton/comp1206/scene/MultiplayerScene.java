package uk.ac.soton.comp1206.scene;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoardsList;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.event.NameRequestedListener;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Multi Player challenge scene. Holds the UI for the multi player challenge mode in the game.
 */
public class MultiplayerScene extends ChallengeScene {

    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    /**
     * The player's name
     */
    private SimpleStringProperty playerName;

    /**
     * The listener to call when player's name is requested
     */
    private NameRequestedListener nameRequestedListener;

    /**
     * Game's leaderboard
     */
    private Leaderboard leaderboard;

    /**
     * VBox used to display the game's chat
     */
    private VBox chatContainer;

    /**
     * Game's chat's text field
     */
    private TextField chatField;


    /**
     * Create a new Multi Player challenge scene
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    /**
     * Setup the game object and model
     */
    @Override
    public void setupGame() {
        logger.info("Starting a new multiplayer challenge");

        //Start new game
        game = new MultiplayerGame(5, 5, gameWindow);
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        super.build();

        //Changes this scene's title to "Multiplayer Match"
        title.setText("Multiplayer Match");

        //Changes the score's heading to the player's name
        playerName = nameRequestedListener.nameRequested();
        scoreHeading.setText(playerName.get());

        //Clears the right sidebar
        rightSideBar.getChildren().clear();

        //Creates a UI component structure to display the leaderboard
        var leaderboardContainer = new VBox();
        leaderboardContainer.setAlignment(Pos.CENTER);
        var leaderboardHeading = new Text("Versus");
        leaderboardHeading.getStyleClass().add("heading");
        leaderboard = new Leaderboard();
        leaderboard.getPlayerNameProperty().bind(playerName);
        leaderboard.getScoreProperty().bind(game.getNewScoreProperty());
        leaderboard.getScoresListProperty().bind(((MultiplayerGame)game).getLeaderboardEntriesProperty());
        leaderboard.getLifeStatusesProperty().bind(((MultiplayerGame)game).getLifeStatusesProperty());
        leaderboardContainer.getChildren().addAll(leaderboardHeading, leaderboard);

        //Displays the leaderboard and the current and following game pieces' piece boards
        rightSideBar.getChildren().addAll(leaderboardContainer, incomingPieces);

        //Creates a UI component structure to display the other players' game boards
        var gameBoards = new GameBoardsList();
        gameBoards.setTranslateX(10);
        gameBoards.getPlayerNameProperty().bind(playerName);
        gameBoards.getGameBoardsProperty().bind(((MultiplayerGame)game).getGameBoardsProperty());
        gameBoards.getLifeStatusesProperty().bind(((MultiplayerGame)game).getLifeStatusesProperty());
        mainPane.setLeft(gameBoards);

        //Creates a UI component structure to display the game's chat
        chatContainer = new VBox();
        chatContainer.setAlignment(Pos.CENTER);
        var chat = new Text();
        chat.textProperty().bind(((MultiplayerGame)game).getGameChat());
        chat.setTextAlignment(TextAlignment.CENTER);
        chat.getStyleClass().add("game-chat");
        chatContainer.getChildren().add(chat);

        //Creates a VBox to display the game board and the game's chat
        var boardAndChat = new VBox();
        boardAndChat.setAlignment(Pos.CENTER);
        boardAndChat.setSpacing(5);
        boardAndChat.getChildren().addAll(board, chatContainer);
        mainPane.setCenter(boardAndChat);
    }

    /**
     * Sets a listener to handle an event when player's name is requested
     * @param listener the listener to add
     */
    public void setOnNameRequested(NameRequestedListener listener) {
        this.nameRequestedListener = listener;
    }

    /**
     * Displays the game chat's text field
     */
    public void displayChatField() {
        logger.info("Displaying game chat's text field");

        //Creates and displays the game's chat's text field
        chatField = new TextField();
        chatField.setMaxWidth(450);
        chatContainer.getChildren().add(chatField);
        chatField.requestFocus();

        //Plays sound effect
        Multimedia.playAudio("sounds/rotate.wav");
    }

    /**
     * Sends a message to the server
     * @param message the message to send
     */
    public void sendMessage(String message) {
        logger.info("Sending message '" + message + "' to server");

        //Sends message to server
        ((MultiplayerGame)game).sendMessage(message);

        //Stops displaying the game's chat's text field
        chatContainer.getChildren().remove(chatField);
    }

    /**
     * Handles a key being pressed
     * @param event the event
     */
    @Override
    public void keyboardSupport(KeyEvent event) {
        super.keyboardSupport(event);

        //Displays the game's chat's text field if the T key is pressed
        if (event.getCode() == KeyCode.T) {
            this.displayChatField();
        }
    }

    /**
     * Handles the ESC key being pressed
     */
    @Override
    public void handleEscKey() {
        logger.info("Handing ESC key being pressed");

        //Checks if game's chat is being used
        if (chatField != null && chatField.isFocused()) {
            //If so, closes chat and plays a sound effect
            chatContainer.getChildren().remove(chatField);
            Multimedia.playAudio("sounds/rotate.wav");
        } //Otherwise, returns to the menuScene if the ESC key is pressed
        else {
            ((MultiplayerGame)game).leaveChannel();
            Multimedia.getMusicPlayer().stop();
            Multimedia.playAudio("sounds/rotate.wav");
            this.cleanUpGame();
            gameWindow.startMenu();
        }
    }

    /**
     * Handles the Enter key being pressed
     */
    @Override
    public void handleEnterKey() {
        logger.info("Handing Enter key being pressed");

        //Sends message in the game's chat's text field to the server
        if (chatField != null && chatField.isFocused() && !chatField.getText().isBlank()) {
            this.sendMessage(chatField.getText());
        } //Otherwise, places current game piece at current aim if ENTER or X keys are pressed
        else {
            game.blockClicked(board.getBlock(x, y));
            board.getBlock(x, y).hoverOver();
        }
    }


    /**
     * Ends and cleans up the game and loads the scores scene
     */
    @Override
    public void endGame() {
        logger.info("Ending Game");

        //Cleans up game
        Multimedia.playAudio("sounds/explode.wav");
        this.cleanUpGame();

        //Loads scores scene
        var scoresScene = new ScoresScene(gameWindow);
        scoresScene.setOnNameRequested(() -> playerName);
        scoresScene.setOnScoreRequested(() -> game.getNewScoreProperty().get());
        scoresScene.setOnMultiplayerScores(() -> ((MultiplayerGame)game).getLeaderboardEntriesProperty());
        gameWindow.loadScene(scoresScene);
    }

    /**
     * Ends and cleans up the game and loads the scores scene
     */
    @Override
    public void cleanUpGame() {
        super.cleanUpGame();

        //Clears all the listeners
        this.setOnNameRequested(null);
        leaderboard.getPlayerNameProperty().unbind();
        leaderboard.getScoreProperty().unbind();
        leaderboard.getScoresListProperty().unbind();
    }
}
