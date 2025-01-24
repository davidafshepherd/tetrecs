package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game.
 * Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * The title as an image
     */
    private ImageView titleImage;

    /**
     * Creates a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Builds the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        mainPane.setPadding(new Insets(10, 10, 10, 10));
        menuPane.getChildren().add(mainPane);

        //Creates the title
        titleImage = new ImageView(new Image(getClass().getResource("/images/TetrECS.png").toExternalForm()));
        titleImage.setPreserveRatio(true);
        titleImage.setFitWidth(600);
        mainPane.setCenter(titleImage);

        //Animates the title to rotate
        titleImage.setRotate(-5);
        this.animateTitle();

        //Creates a Text UI component to be used as a button to start the single player challenge
        var playButton = new Text("Singleplayer");
        playButton.getStyleClass().add("menu-item");

        //Binds the playButton action to the startGame method in the menu
        playButton.setOnMouseClicked(this::startGame);

        //Creates a Text UI component to be used as a button to start the multiplayer challenge
        var multiplayerButton = new Text("Multiplayer");
        multiplayerButton.getStyleClass().add("menu-item");

        //Binds the multiplayerButton action to the startMultiplayer() method in the menu
        multiplayerButton.setOnMouseClicked(this::startMultiplayer);

        //Creates a Text UI component to be used as a button to display the game instructions
        var howToPlayButton = new Text("How to Play");
        howToPlayButton.getStyleClass().add("menu-item");

        //Binds the howToPlayButton action to the startInstructions() method in the menu
        howToPlayButton.setOnMouseClicked(this::startInstructions);

        //Creates a Text UI component to be used as a button to exit the game
        var exitButton = new Text("Exit");
        exitButton.getStyleClass().add("menu-item");

        //Binds the exitButton action to the exit() method in the menu
        exitButton.setOnMouseClicked(this::exit);

        //Creates a button list using VBox
        var buttonList = new VBox();
        buttonList.setSpacing(10);
        buttonList.getChildren().addAll(playButton, multiplayerButton, howToPlayButton, exitButton);
        buttonList.setAlignment(Pos.CENTER);
        mainPane.setBottom(buttonList);
    }

    /**
     * Initialises the menu and sets up anything that needs to be done at the start
     */
    @Override
    public void initialise() {
        logger.info("Initialising Menu");

        //Plays the menu's background music on a loop
        Multimedia.playMusic("music/menu.mp3");

        //Adds keyboard support to this scene
        scene.setOnKeyPressed(this::keyboardSupport);
    }

    /**
     * Animates the title to rotate slightly up and slightly down on a loop
     */
    public void animateTitle() {
        logger.info("Playing title rotation animation");

        //Creates transition to rotate title up by 5 degrees
        var rotateClockwise = new RotateTransition(new Duration(3000), titleImage);
        rotateClockwise.setToAngle(5);

        //Creates transition to rotate title down by 5 degrees
        var rotateAnticlockwise = new RotateTransition(new Duration(3000), titleImage);
        rotateAnticlockwise.setToAngle(-5);

        //Makes these two transitions into a looped sequential transition
        var animation = new SequentialTransition(rotateClockwise, rotateAnticlockwise);
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.setAutoReverse(true);

        //Plays the looped sequential transition
        animation.play();
    }

    /**
     * Handles when the Start Game button is pressed
     * @param event event
     */
    private void startGame(MouseEvent event) {
        Multimedia.playAudio("sounds/rotate.wav");
        gameWindow.startChallenge();
    }

    /**
     * Handles when the Multiplayer button is pressed
     * @param event event
     */
    public void startMultiplayer(MouseEvent event) {
        Multimedia.playAudio("sounds/rotate.wav");
        gameWindow.startMultiplayer();
    }

    /**
     * Handles when the How To Play button is pressed
     * @param event event
     */
    public void startInstructions(MouseEvent event) {
        Multimedia.playAudio("sounds/rotate.wav");
        gameWindow.startInstructions();
    }

    /**
     * Handles when the Exit button is pressed
     * @param event event
     */
    public void exit(MouseEvent event) {
        Multimedia.playAudio("sounds/rotate.wav");
        gameWindow.getCommunicator().send("QUIT");
        App.getInstance().shutdown();
    }

    /**
     * Handles a key being pressed
     * @param event the event
     */
    public void keyboardSupport(KeyEvent event) {
        logger.info("Handling a key being pressed");

        //Exits the game if the ESC key is pressed
        if(event.getCode() == KeyCode.ESCAPE) {
            Multimedia.playAudio("sounds/rotate.wav");
            App.getInstance().shutdown();
        }
    }
}
