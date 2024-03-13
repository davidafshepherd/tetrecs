package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

/**
 * The Single Player challenge scene.
 * Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    protected Game game;

    /**
     * Current aim's x coordinate
     */
    protected int x = 0;

    /**
     * Current aim's y coordinate
     */
    protected int y = 0;

    /**
     * BorderPane used to display the main contents of this scene
     */
    protected BorderPane mainPane;

    /**
     * Game's game board
     */
    protected GameBoard board;

    /**
     * This scene's title
     */
    protected Text title;

    /**
     * Text displaying the score's heading
     */
    protected Text scoreHeading;

    /**
     * Text displaying current score
     */
    private Text scoreText;

    /**
     * Text displaying current number of lives
     */
    private Text livesText;

    /**
     * VBox displaying the current and following game pieces' piece boards
     */
    protected VBox incomingPieces;

    /**
     * Current game piece's piece board
     */
    private PieceBoard currentPieceBoard;

    /**
     * Next game piece's piece board
     */
    private PieceBoard followingPieceBoard;

    /**
     * VBox displaying the right sidebar
     */
    protected VBox rightSideBar;


    /**
     * Text displaying current local high score
     */
    private Text highScoreText;


    /**
     * Text displaying current level
     */
    private Text levelText;

    /**
     * Text displaying current multiplier
     */
    private Text multiplierText;

    /**
     * The time bar
     */
    private Rectangle timerBar;

    /**
     * The time bar's animation
     */
    private Transition timerBarAnimation;

    /**
     * Creates a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Sets up the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialises the scene, starts the game and sets up anything that needs to be done at the start
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");

        game.start();

        //Stops any music being currently played and plays the game background music
        Multimedia.getMusicPlayer().stop();
        Multimedia.playMusic("music/game_start.wav");
        Multimedia.getMusicPlayer().setOnEndOfMedia(() -> {
            Multimedia.getMusicPlayer().stop();
            Multimedia.playMusic("music/game.wav");
        });

        //Adds keyboard support to this scene and the game
        scene.setOnKeyPressed(this::keyboardSupport);

        //Ends the game if the player has no more lives
        game.getLivesProperty().addListener((observable, oldValue, newValue) -> {if (newValue.equals(-1)) this.endGame();});
    }

    /**
     * Builds the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        this.setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);

        mainPane = new BorderPane();
        mainPane.setPadding(new Insets(10, 10, 10, 10));
        challengePane.getChildren().add(mainPane);

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);

        //Handle block in game board grid being left-clicked
        board.setOnBlockClick(this::blockClicked);

        //Handles block in game board grid being right-clicked
        board.setOnRightClicked(this::blockRightClicked);

        //Handles a block in game board grid being hovered over
        board.setOnBlockHovered(this::blockHovered);

        //Handles a line of game blocks in the game board being cleared
        game.setLineClearedListener(this::lineCleared);

        //Creates a UI component structure to display the score
        //Bind the UI component that displays the score value itself to a simple integer property in the Game Class
        var score = new VBox();
        score.setAlignment(Pos.CENTER);
        scoreHeading = new Text("Score");
        scoreHeading.getStyleClass().add("heading");
        scoreText = new Text();
        scoreText.getStyleClass().add("score");
        scoreText.textProperty().bind(game.getScoreProperty().asString());
        score.getChildren().addAll(scoreHeading, scoreText);

        //Handles current score exceeding high score
        scoreText.textProperty().addListener((observableValue, previousValue, newValue) -> {if (Integer.parseInt(newValue) > Integer.parseInt(highScoreText.getText())) this.setScoreAsHighScore();});

        //Creates a UI component structure to display the lives
        //Binds the UI component that displays the lives number itself to a simple integer property in the Game Class
        var lives = new VBox();
        lives.setAlignment(Pos.CENTER);
        var livesHeading = new Text("Lives");
        livesHeading.getStyleClass().add("heading");
        livesText = new Text();
        livesText.getStyleClass().add("lives");
        livesText.textProperty().bind(game.getLivesProperty().asString());
        lives.getChildren().addAll(livesHeading, livesText);

        //Creates a BorderPane to display the heading
        //I.e. the title, the score and the lives
        var heading = new BorderPane();
        title = new Text("Singleplayer Match");
        title.getStyleClass().add("title");
        heading.setLeft(score);
        heading.setCenter(title);
        heading.setRight(lives);
        mainPane.setTop(heading);

        //Creates a UI component structure to display the level
        //Binds the UI component that displays the level number itself to a simple integer property in the Game Class
        var level = new VBox();
        level.setAlignment(Pos.CENTER);
        Text levelHeading = new Text("Level");
        levelHeading.getStyleClass().add("heading");
        levelText = new Text();
        levelText.getStyleClass().add("level");
        levelText.textProperty().bind(game.getLevelProperty().asString());
        level.getChildren().addAll(levelHeading, levelText);

        //Creates a UI component structure to display the multiplier
        //Binds the UI component that displays the multiplier value itself to a simple integer property in the Game Class
        var multiplier = new VBox();
        multiplier.setAlignment(Pos.CENTER);
        var multiplierHeading = new Text("Multiplier");
        multiplierHeading.getStyleClass().add("heading");
        multiplierText = new Text();
        multiplierText.getStyleClass().add("multiplier");
        multiplierText.textProperty().bind(Bindings.concat(game.getMultiplierProperty().asString(), "x"));
        multiplier.getChildren().addAll(multiplierHeading, multiplierText);

        //Creates a UI component structure to display the current and following game pieces' piece boards
        incomingPieces = new VBox();
        incomingPieces.setAlignment(Pos.CENTER);
        var incomingPiecesHeading = new Text("Incoming Pieces");
        incomingPiecesHeading.getStyleClass().add("heading");
        currentPieceBoard = new PieceBoard(new Grid(3, 3),130, 130);
        currentPieceBoard.setPadding(new Insets(5, 0, 0, 0));
        followingPieceBoard = new PieceBoard(new Grid(3, 3),80, 80);
        followingPieceBoard.setPadding(new Insets(10, 0, 0, 0));
        incomingPieces.getChildren().addAll(incomingPiecesHeading, currentPieceBoard, followingPieceBoard);

        //Handles updating the current and following game pieces' piece boards
        game.setNextPieceListener(this::updatePieceBoards);

        //Handles block on current game piece's piece board grid being left-clicked
        currentPieceBoard.setOnBlockClick(this::currentPieceBoardClicked);

        //Handles block on following game piece's piece board grid being left-clicked
        followingPieceBoard.setOnBlockClick(this::followingPieceBoardClicked);

        //Creates a UI component structure to display the local high score
        var highScore = new VBox();
        highScore.setAlignment(Pos.CENTER);
        var highScoreHeading = new Text("High Score");
        highScoreHeading.getStyleClass().add("heading");
        highScoreText = new Text(this.getHighScore());
        highScoreText.getStyleClass().add("high-score");
        highScore.getChildren().addAll(highScoreHeading, highScoreText);

        //Creates a sidebar on the right side using VBox
        rightSideBar = new VBox();
        rightSideBar.setSpacing(15);
        rightSideBar.getChildren().addAll(highScore, level, multiplier, incomingPieces);
        mainPane.setRight(rightSideBar);
        rightSideBar.setAlignment(Pos.CENTER);

        //Creates an animated timer bar in the bottom using Rectangle
        timerBar = new Rectangle();
        timerBar.setWidth(gameWindow.getWidth()-20);
        timerBar.setHeight(20);
        mainPane.setBottom(timerBar);
        timerBarAnimation = this.animateTimeBar();
        timerBarAnimation.play();

        //Handles resetting the timer bar
        game.setOnGameLoop(this::resetTimerBar);
    }

    /**
     * Handles a block in the game board grid being left-clicked
     * @param gameBlock the Game Block that was left-clicked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    /**
     * Handles a block in the game board grid being right-clicked
     * @param gameBlock the Game Block that was right-clicked
     */
    public void blockRightClicked(GameBlock gameBlock) {
        game.rotateCurrentPiece(1);
    }

    /**
     * Handles a block in the game board grid being hovered over
     * @param gameBlock the Game Block that was hovered over
     */
    public void blockHovered(GameBlock gameBlock) {
        gameBlock.hoverOver();
    }

    /**
     * Handles a line of game blocks in the game board being cleared
     * @param gameBlockCoordinates set of the coordinates of the game blocks that were cleared
     */
    public void lineCleared(Set<GameBlockCoordinate> gameBlockCoordinates) {
        board.fadeOut(gameBlockCoordinates);
    }

    /**
     * Gets the highest local high score
     * @return the highest local high score
     */
    public String getHighScore() {
        logger.info("Getting local high score");
        try {
            //Opens scores file
            var scoresFile = new File("scores.txt");
            //Checks if scores file exists
            if (scoresFile.createNewFile()) {
                //If not, creates scores file and populates it with a default list
                var writer = new FileWriter(scoresFile);
                for (int i = 10; i > 0; i--) {
                    writer.write("David:" + i+ "000" + "\n");
                }
                writer.close();
                //Returns default local high score
                return "10000";
            } //Otherwise, reads the first line in the scores file to find the highest local high score
            else {
                var reader = new Scanner(scoresFile);
                var line = reader.nextLine();
                var splitLine = line.split(":");
                //Returns the highest local high score
                return splitLine[1];
            }
        } //Catches exception in case it was not possible to open the scores file
        //Or create the scores file in the event of it not already existing
        //Or read from the scores file
        catch (IOException e) {
            logger.info("Not able to open scores file / create scores file / read scores file");
            e.printStackTrace();
            //Returns 0 as it was not possible to get the highest local high score
            return "0";
        }
    }

    /**
     * Handles the current score in the game exceeding the high score
     */
    public void setScoreAsHighScore() {
        highScoreText.textProperty().bind(scoreText.textProperty());
    }

    /**
     * Handles updating the current and following game pieces' piece boards
     * @param currentPiece the current game piece the player has
     * @param followingPiece the next game piece the player will be given
     */
    public void updatePieceBoards(GamePiece currentPiece, GamePiece followingPiece) {
        currentPieceBoard.setPieceToDisplay(currentPiece);
        followingPieceBoard.setPieceToDisplay(followingPiece);
    }

    /**
     * Handles a block in the current game piece's piece board grid being left-clicked
     * @param gameBlock the Game Block that was left-clicked
     */
    public void currentPieceBoardClicked(GameBlock gameBlock) {
        game.rotateCurrentPiece(1);
    }

    /**
     * Handles a block in the following game piece's piece board grid being left-clicked
     * @param gameBlock the Game Block that was left-clicked
     */
    public void followingPieceBoardClicked(GameBlock gameBlock) {
        game.swapCurrentPiece();
    }

    /**
     * Animates the timer bar
     * @return the animation
     */
    public Transition animateTimeBar() {
        logger.info("Animating timer bar");

        //Creates colour change part of animation
        var fill = new FillTransition(new Duration((game.getTimerDelay())/2), timerBar, Color.DARKGREEN, Color.YELLOW);
        var fill2 = new FillTransition(new Duration((game.getTimerDelay())/3), timerBar, Color.YELLOW, Color.RED);
        var fiilSequential = new SequentialTransition(fill, fill2);

        //Creates size change part of animation
        var scale = new ScaleTransition(new Duration(game.getTimerDelay()), timerBar);
        scale.setToX(0);
        var move = new TranslateTransition(new Duration(game.getTimerDelay()), timerBar);
        move.setToX(-(gameWindow.getWidth()-20)/2);

        //Puts the animation together and returns it
        return new ParallelTransition(fiilSequential, scale, move);
    }

    /**
     * Handles resetting the timer bar
     */
    public void resetTimerBar() {
        logger.info("Resetting timer bar");
        timerBarAnimation.pause();
        timerBar.setScaleX(1);
        timerBar.setTranslateX(0);
        timerBarAnimation = this.animateTimeBar();
        timerBarAnimation.play();
    }

    /**
     * Handles a key being pressed
     * @param event the event
     */
    public void keyboardSupport(KeyEvent event) {
        logger.info("Handling a key being pressed");

        //Returns to the menuScene if the ESC key is pressed
        if (event.getCode() == KeyCode.ESCAPE) {
            this.handleEscKey();
        } //Moves current aim up if UP ARROW or W keys are pressed
        else if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.W) {
            if (y != 0) {
                board.getBlock(x, y).paint();
                y-= 1;
                board.getBlock(x, y).hoverOver();
            }
        } //Moves current aim to the right if RIGHT ARROW or D keys are pressed
        else if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
            if (x != game.getCols()-1) {
                board.getBlock(x, y).paint();
                x+= 1;
                board.getBlock(x, y).hoverOver();
            }
        } //Moves current aim down if DOWN ARROW or S keys are pressed
        else if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) {
            if (y != game.getRows()-1) {
                board.getBlock(x, y).paint();
                y+= 1;
                board.getBlock(x, y).hoverOver();
            }
        } //Moves current aim to the left if LEFT ARROW or A keys are pressed
        else if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
            if (x != 0) {
                board.getBlock(x, y).paint();
                x-= 1;
                board.getBlock(x, y).hoverOver();
            }
        } //Places current game piece at current aim if ENTER or X keys are pressed
        else if (event.getCode() == KeyCode.ENTER) {
            this.handleEnterKey();
        } else if (event.getCode() == KeyCode.X) {
            game.blockClicked(board.getBlock(x, y));
            board.getBlock(x, y).hoverOver();
        } //Swap current game piece with the following game piece if SPACE or R keys are pressed
        else if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.R) {
            game.swapCurrentPiece();
        } //Rotates current game piece to the left if Q or Z or [ keys are pressed
        else if (event.getCode() == KeyCode.Q || event.getCode() == KeyCode.Z || event.getCode() == KeyCode.OPEN_BRACKET) {
            game.rotateCurrentPiece(3);
        } //Rotates current game piece to the right if E or C or ] keys are pressed
        else if (event.getCode() == KeyCode.E || event.getCode() == KeyCode.C || event.getCode() == KeyCode.CLOSE_BRACKET) {
            game.rotateCurrentPiece(1);
        }
    }

    /**
     * Handles the ESC key being pressed
     */
    public void handleEscKey() {
        logger.info("Handing ESC key being pressed");

        //Returns to the menuScene
        Multimedia.getMusicPlayer().stop();
        Multimedia.playAudio("sounds/rotate.wav");
        this.cleanUpGame();
        gameWindow.startMenu();
    }

    /**
     * Handles the Enter key being pressed
     */
    public void handleEnterKey() {
        logger.info("Handing Enter key being pressed");

        //Places current game piece at current aim
        game.blockClicked(board.getBlock(x, y));
        board.getBlock(x, y).hoverOver();
    }

    /**
     * Ends and cleans up the game and loads the scores scene
     */
    public void endGame() {
        logger.info("Ending Game");

        //Cleans up game
        Multimedia.playAudio("sounds/explode.wav");
        this.cleanUpGame();

        //Loads scores scene
        var scoresScene = new ScoresScene(gameWindow);
        scoresScene.setOnScoreRequested(() -> game.getNewScoreProperty().get());
        gameWindow.loadScene(scoresScene);
    }

    /**
     * When switching from this scene to another, performs any cleanup on the game needed
     * Such as removing previous listeners
     */
    public void cleanUpGame() {
        logger.info("Cleaning up Game");

        //Clears all the listeners and shuts down the timer
        game.getTimer().shutdownNow();
        game.setOnGameLoop(null);
        game.setLineClearedListener(null);
        game.setNextPieceListener(null);
        levelText.textProperty().unbind();
        livesText.textProperty().unbind();
        multiplierText.textProperty().unbind();
        scoreText.textProperty().unbind();
        highScoreText.textProperty().unbind();
        board.setOnBlockHovered(null);
        board.setOnBlockClick(null);
        board.setOnRightClicked(null);
    }
}
