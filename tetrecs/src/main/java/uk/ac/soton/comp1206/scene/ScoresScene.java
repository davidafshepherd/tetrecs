package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.event.MultiplayerScoresListener;
import uk.ac.soton.comp1206.event.NameRequestedListener;
import uk.ac.soton.comp1206.event.ScoreRequestedListener;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Scores scene. Provides a display for the high scores achieved by players.
 */
public class ScoresScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ScoresScene.class);

    /**
     * BorderPane that contains the scores scene contents
     */
    private BorderPane mainPane;

    /**
     * ArrayList used to hold the local high scores
     */
    private final ArrayList<Pair<String, Integer>> localScores = new ArrayList<>();

    /**
     * ObservableArrayList wrapped around the localScores ArrayList
     */
    private final ObservableList<Pair<String, Integer>> localScoresList = FXCollections.observableArrayList(localScores);

    /**
     * SimpleListProperty used to represent the localScoresList as a property
     * This allows for binding and adding listeners to the localScoresList
     */
    private final ListProperty<Pair<String, Integer>> localScoresWrapper = new SimpleListProperty<>(localScoresList);

    /**
     * ArrayList used to hold the online high scores
     */
    private final ArrayList<Pair<String, Integer>> remoteScores = new ArrayList<>();

    /**
     * ObservableArrayList wrapped around the remoteScores ArrayList
     */
    private final ObservableList<Pair<String, Integer>> remoteScoresList = FXCollections.observableArrayList(remoteScores);

    /**
     * SimpleListProperty used to represent the remoteScoresList as a property
     * This allows for binding and adding listeners to the remoteScoresList
     */
    private final ListProperty<Pair<String, Integer>> remoteScoresWrapper = new SimpleListProperty<>(remoteScoresList);

    /**
     * The listener to call when last game's score is requested
     */
    private ScoreRequestedListener scoreRequestedListener;

    /**
     * The listener to call when last multiplayer game's scores are requested
     */
    private MultiplayerScoresListener multiplayerScoresListener;

    /**
     * The listener to call when the player's name is requested
     */
    private NameRequestedListener nameRequestedListener;

    /**
     * Last game's final score
     */
    private final SimpleIntegerProperty finalScore = new SimpleIntegerProperty();

    /**
     * Prompt's text field
     */
    private TextField promptTextField;

    /**
     * Name entered into the prompt if player is prompted
     */
    private final SimpleStringProperty playerName = new SimpleStringProperty();

    /**
     * Timer used to display scoreboard for a certain amount of time
     */
    private ScheduledExecutorService scoreboardTimer;

    /**
     * Comparator used to sort a scores list
     */
    private final Comparator<Pair<String, Integer>> scoreSorter = (pair1, pair2) -> {
        if (pair1.getValue() > pair2.getValue()) {
            return -1;
        } else if (pair1.getValue().equals(pair2.getValue())) {
            return 0;
        } else {
            return 1;
        }
    };

    /**
     * The communicator
     */
    private final Communicator communicator = gameWindow.getCommunicator();

    /**
     * Create a new Scores scene
     *
     * @param gameWindow the Game Window this will be displayed in
     */
    public ScoresScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Scores Scene");
    }

    /**
     * Initialise the scores and sets up anything that needs to be done at the start
     */
    @Override
    public void initialise() {
        logger.info("Initialising Scores");

        //Stops any music being current played and plays the scores background music
        Multimedia.getMusicPlayer().stop();
        Multimedia.playMusic("music/end.wav");
        Multimedia.getMusicPlayer().setOnEndOfMedia(() -> Multimedia.getMusicPlayer().stop());

        //Adds keyboard support to this scene
        scene.setOnKeyPressed(this::keyboardSupport);

        //Handles receiving a message from the communicator
        communicator.addListener((communication) -> Platform.runLater(() -> {
            var messageSplit = communication.split(" ", 2);
            switch (messageSplit[0]) {
                //If message received are the online high scores, populates the remote scores list with them
                case "HISCORES" -> this.checkToPrompt(messageSplit[1]);
                //If message received is confirmation of a high score being successfully submitted to the server, logs it
                case "NEWSCORE" -> logger.info("New high score successfully submitted to server");
            }
        }));

        //Requests online high scores from server
        communicator.send("HISCORES");
    }

    /**
     * Builds the scores layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var scoresPane = new StackPane();
        scoresPane.setMaxWidth(gameWindow.getWidth());
        scoresPane.setMaxHeight(gameWindow.getHeight());
        scoresPane.getStyleClass().add("menu-background");
        root.getChildren().add(scoresPane);

        mainPane = new BorderPane();
        mainPane.setPadding(new Insets(10, 10, 10, 10));
        scoresPane.getChildren().add(mainPane);

        //Creates the title and the "game over" heading
        var headings = new VBox();
        headings.setAlignment(Pos.CENTER);
        headings.setSpacing(15);
        var titleImage = new ImageView(new Image(getClass().getResource("/images/TetrECS.png").toExternalForm()));
        titleImage.setPreserveRatio(true);
        titleImage.setFitWidth(600);
        var gameOverText = new Text("Game Over");
        gameOverText.getStyleClass().add("big-title");
        headings.getChildren().addAll(titleImage, gameOverText);
        mainPane.setTop(headings);
    }

    /**
     * Sets a listener to handle an event when last game's score is requested
     * @param listener the listener to add
     */
    public void setOnScoreRequested(ScoreRequestedListener listener) {
        this.scoreRequestedListener = listener;
    }

    /**
     * Sets a listener to handle an event when last game's multiplayer scores are requested
     * @param listener the listener to add
     */
    public void setOnMultiplayerScores(MultiplayerScoresListener listener) {this.multiplayerScoresListener = listener;}

    /**
     * Sets a listener to handle an event when the player's name is requested
     * @param listener the listener to add
     */
    public void setOnNameRequested(NameRequestedListener listener) { this.nameRequestedListener = listener;}

    /**
     * Check if prompting the player is needed and either displays a prompt or the scoreboard depending on so
     * @param onlineScores the online high scores received from the server
     */
    public void checkToPrompt(String onlineScores) {
        logger.info("Checking if prompting player is needed");

        //Checks if there was a last game and if it was multiplayer
        if (multiplayerScoresListener != null) {
            //If so, loads its scores and stores them
            localScoresWrapper.addAll(multiplayerScoresListener.multiplayerScores());
            finalScore.set(scoreRequestedListener.scoreRequested());
        } //Otherwise, loads and stores the local high scores
        else {
            this.loadScores();
        }

        //Stores the online high scores received from the server
        this.loadOnlineScores(onlineScores);

        //Displays a prompt if there was a last game AND last game wasn't a multiplayer game AND if last game's score beats any of the local scores
        if (scoreRequestedListener != null && multiplayerScoresListener == null && this.checkScore(localScoresWrapper)) {
            this.buildPrompt();
        }
        //Otherwise, displays the scoreboard
        else {
            this.buildScoreBoard();
        }
    }

    /**
     * Populates the local scores list using the local high scores stored in the scores file
     */
    public void loadScores() {
        //Opens scores file and creates a reader to read it
        try {
            var scoresFile = new File("scores.txt");
            var reader = new Scanner(scoresFile);
            //Clears local scores list
            localScoresWrapper.clear();
            //Reads each line in the file and stores each line's player name and score as a pair in the local scores list
            while (reader.hasNextLine()) {
                var line = reader.nextLine();
                var splitLine = line.split(":");
                var name = splitLine[0];
                var score = Integer.parseInt(splitLine[1]);
                localScoresWrapper.add(new Pair<>(name, score));
            }
            logger.info("Loading local scores");
            //Closes the file reader
            reader.close();
        //Catches exception in case scores file was not found
        } catch (FileNotFoundException e) {
            logger.info("Scores file not found / Not able to read from scores file");
            e.printStackTrace();
        }
    }

    /**
     * Writes each player name and score pair in an ordered list to the scores file
     * @param orderedList the ordered list
     */
    public void writeScores(ListProperty<Pair<String, Integer>> orderedList) {
        //Opens scores file and creates writer to write to it
        try {
            var scoresFile = new File("scores.txt");
            var writer = new FileWriter(scoresFile);
            //Writes each player name and score pair in the ordered list to the scores file
            //Where each pair is in a new line in the file
            for (Pair<String, Integer> scorePair : orderedList) {
                writer.write(scorePair.getKey() + ":" + scorePair.getValue() + "\n");
            }
            logger.info("Scores file written to");
            //Closes the file writer
            writer.close();
        //Catches exception in case it was not possible to write to it
        } catch (IOException e) {
            logger.info("Scores file not found / Not able to write to scores file");
            e.printStackTrace();
        }
    }

    /**
     * Populates the remote scores list using the online high scores received from the server
     * @param onlineScores the online high scores received
     */
    public void loadOnlineScores(String onlineScores) {
        logger.info("Loading online high scores");

        //Clears the remote scores list
        remoteScoresWrapper.clear();

        //Stores each online high score received in a string array
        var onlineScoresSplit = onlineScores.split("\n");

        //Stores each online high score's score and player name as a pair in the remote scores list
        for (var onlineScore: onlineScoresSplit) {
            var onlineScoreSplit = onlineScore.split(":");
            remoteScoresWrapper.add(new Pair<>(onlineScoreSplit[0], Integer.parseInt(onlineScoreSplit[1])));
        }
    }

    /**
     * Sends a score to the server to be saved as a new online high score
     * @param name name of the player who got the score
     * @param score the score to be sent
     */
    public void writeOnlineScore(String name, Integer score) {
        logger.info("Sending score to server");
        communicator.send("HISCORE " + name + ":" + score.toString());
    }

    /**
     * Checks if last game's score is higher than any of the scores in an ordered list
     * @return true if last game's score is higher and false if not
     */
    public boolean checkScore(ListProperty<Pair<String, Integer>> orderedList) {
        //Gets last game's score and stores it
        finalScore.set(scoreRequestedListener.scoreRequested());

        //Loops through each score in the ordered list
        for (var scorePair: orderedList) {
            //Returns true if last game's score is higher than any of the scores
            if (finalScore.get() > scorePair.getValue()) {
                logger.info("Score beats a score");
                return true;
            }
        }
        //Returns false if last game's score is not higher than any of the scores
        logger.info("Score beats no score");
        return false;
    }

    /**
     * Builds and displays the new high score prompt
     */
    public void buildPrompt() {
        logger.info("Building prompt");

        //Creates a prompt
        var prompt = new VBox();
        prompt.setTranslateY(-75);
        prompt.setAlignment(Pos.CENTER);
        prompt.setSpacing(10);
        var promptHeading = new Text("New High Score!");
        promptHeading.getStyleClass().add("title");
        promptTextField = new TextField();
        promptTextField.setPromptText("Enter your name here");
        prompt.getChildren().addAll(promptHeading, promptTextField);
        mainPane.setCenter(prompt);

        //Handles player entering their name to register a new local high score
        promptTextField.setOnKeyPressed((event) -> {if (event.getCode() == KeyCode.ENTER && !promptTextField.getText().isBlank()) this.updateScores();});
    }

    /**
     * Updates the local scores and, if needed, the online scores and displays the scoreboard
     */
    public void updateScores() {
        logger.info("Updating local and online scores");

        //Stores the player's name
        playerName.set(promptTextField.getText());

        //Stores the player's name and their score last game in a pair
        var newHighScore = new Pair<>(playerName.get(), finalScore.get());

        //Updates the local scores list and the scores stored in the scores file
        localScoresWrapper.add(newHighScore);
        localScoresWrapper.sort(scoreSorter);
        localScoresWrapper.remove(localScoresWrapper.get(10));
        this.writeScores(localScoresWrapper);

        //Updates the remote scores list and sends the player's score to the server if it beats any of the online high scores
        if (this.checkScore(remoteScoresWrapper)) {
            remoteScoresWrapper.add(newHighScore);
            remoteScoresWrapper.sort(scoreSorter);
            remoteScoresWrapper.remove(remoteScoresWrapper.get(10));
            this.writeOnlineScore(newHighScore.getKey(), newHighScore.getValue());
        }

        //Displays the scoreboard
        this.buildScoreBoard();
    }

    /**
     * Builds and displays the scoreboard
     */
    public void buildScoreBoard() {
        logger.info("Building scoreboard");

        //Creates scoreboard container and scoreboard heading
        var scoreBoard = new VBox();
        scoreBoard.setAlignment(Pos.CENTER);
        scoreBoard.setSpacing(10);
        var scoreBoardHeading = new Text("High Scores");
        scoreBoardHeading.getStyleClass().add("title");

        //Creates score lists container
        var scoreLists = new HBox();
        scoreLists.setAlignment(Pos.CENTER);
        scoreLists.setSpacing(200);

        //Creates local score list container
        var localScoresContainer = new VBox();
        localScoresContainer.setAlignment(Pos.CENTER);
        Text localScoresHeading = new Text();
        localScoresHeading.getStyleClass().add("heading");
        var localScoresList = new ScoresList();
        if (multiplayerScoresListener != null) {
            localScoresHeading.setText("This game");
            localScoresList.getPlayerNameProperty().bind(nameRequestedListener.nameRequested());
        } else {
            localScoresHeading.setText("Local Scores");
            localScoresList.getPlayerNameProperty().bind(playerName);
        }
        localScoresList.getScoreProperty().bind(finalScore);
        localScoresList.getScoresListProperty().bind(localScoresWrapper);
        localScoresContainer.getChildren().addAll(localScoresHeading, localScoresList);

        //Creates online score list container
        var remoteScoresContainer = new VBox();
        remoteScoresContainer.setAlignment(Pos.CENTER);
        var remoteScoresHeading = new Text("Online Scores");
        remoteScoresHeading.getStyleClass().add("heading");
        var remoteScoresList = new ScoresList();
        remoteScoresList.getPlayerNameProperty().bind(playerName);
        remoteScoresList.getScoreProperty().bind(finalScore);
        remoteScoresList.getScoresListProperty().bind(remoteScoresWrapper);
        remoteScoresContainer.getChildren().addAll(remoteScoresHeading, remoteScoresList);

        //Adds both score lists to score lists container
        scoreLists.getChildren().addAll(localScoresContainer, remoteScoresContainer);

        //Adds scoreboard heading and score lists container to scoreboard container
        scoreBoard.getChildren().addAll(scoreBoardHeading, scoreLists);

        //Sets ScoresScene to returns to the MenuScene after displaying the scoreboard for 15 seconds
        scoreboardTimer = Executors.newSingleThreadScheduledExecutor();
        scoreboardTimer.scheduleAtFixedRate(() -> Platform.runLater(this::endScoresScene), 15, 15, TimeUnit.SECONDS);

        //Displays the scoreboard in the center of the main pane
        mainPane.setCenter(scoreBoard);
    }

    /**
     * Handles exiting this scene
     */
    public void endScoresScene() {
        logger.info("Exiting scores scene");

        //Cleans up this scene and loads the menu scene
        Multimedia.getMusicPlayer().stop();
        this.setOnScoreRequested(null);
        if (scoreboardTimer != null) {
            scoreboardTimer.shutdownNow();
        }
        gameWindow.startMenu();
    }

    /**
     * Handles a key being pressed
     * @param event the event
     */
    public void keyboardSupport(KeyEvent event) {
        logger.info("Handling a key being pressed");

        //Returns to the menuScene if the ESC key is pressed
        if (event.getCode() == KeyCode.ESCAPE) {
            Multimedia.playAudio("sounds/rotate.wav");
            this.endScoresScene();
        }
    }
}
