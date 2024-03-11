package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;

/**
 * The Instructions scene. Provides a display for the instructions on how to play the game.
 */
public class InstructionsScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

    /**
     * Create a new Instructions scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Instructions Scene");
    }

    /**
     * Initialise the instructions and sets up anything that needs to be done at the start
     */
    @Override
    public void initialise() {
        logger.info("Initialising Instructions");

        //Adds keyboard support to this scene
        scene.setOnKeyPressed(this::keyboardSupport);
    }

    /**
     * Builds the instructions layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var instructionsPane = new StackPane();
        instructionsPane.setMaxWidth(gameWindow.getWidth());
        instructionsPane.setMaxHeight(gameWindow.getHeight());
        instructionsPane.getStyleClass().add("menu-background");
        root.getChildren().add(instructionsPane);

        var mainPane = new BorderPane();
        mainPane.setPadding(new Insets(10, 10, 10, 10));
        instructionsPane.getChildren().add(mainPane);

        //Creates a UI component structure to display the instructions
        var instructions = new VBox();
        instructions.setAlignment(Pos.CENTER);
        var instructionsHeading = new Text("Instructions");
        instructionsHeading.getStyleClass().add("heading");
        var instructionsText = new Text("TetrECS is a fast-paced gravity-free block placement game, where you must survive by clearing rows through careful placement of the upcoming blocks before the time runs out. Lose all 3 lives and you're destroyed!");
        instructionsText.setWrappingWidth(gameWindow.getWidth() - 20);
        instructionsText.setTextAlignment(TextAlignment.CENTER);
        instructionsText.getStyleClass().add("instructions");
        var instructionsImage = new ImageView(new Image(getClass().getResource("/images/Instructions.png").toExternalForm()));
        instructionsImage.setPreserveRatio(true);
        instructionsImage.setFitWidth(550);
        instructions.getChildren().addAll(instructionsHeading, instructionsText, instructionsImage);
        mainPane.setTop(instructions);

        //Creates all 15 game pieces and displays them within 15 different piece boards
        var pieceBoards = new ArrayList<PieceBoard>();
        for (int i = 0; i < 15; i++) {
            pieceBoards.add(new PieceBoard(new Grid(3, 3), 50, 50));
            pieceBoards.get(i).setPieceToDisplay(GamePiece.createPiece(i));
        }
        //Creates HBox to display first 5 piece boards
        var row1 = new HBox();
        row1.setAlignment(Pos.CENTER);
        row1.setSpacing(5);
        row1.getChildren().addAll(pieceBoards.get(0), pieceBoards.get(1), pieceBoards.get(2), pieceBoards.get(3), pieceBoards.get(4));

        //Creates HBox to display next 5 piece boards
        var row2 = new HBox();
        row2.setAlignment(Pos.CENTER);
        row2.setSpacing(5);
        row2.getChildren().addAll(pieceBoards.get(5), pieceBoards.get(6), pieceBoards.get(7), pieceBoards.get(8), pieceBoards.get(9));

        //Creates HBox to display last 5 piece boards
        var row3 = new HBox();
        row3.setAlignment(Pos.CENTER);
        row3.setSpacing(5);
        row3.getChildren().addAll(pieceBoards.get(10), pieceBoards.get(11), pieceBoards.get(12), pieceBoards.get(13), pieceBoards.get(14));

        //Creates VBox to display all piece board rows
        var pieceBoardRows = new VBox();
        pieceBoardRows.setAlignment(Pos.CENTER);
        pieceBoardRows.setSpacing(5);
        pieceBoardRows.getChildren().addAll(row1, row2, row3);

        //Creates a UI component structure to display the game pieces
        var gamePieces = new VBox();
        gamePieces.setAlignment(Pos.CENTER);
        var gamePiecesHeading = new Text("Game Pieces");
        gamePiecesHeading.getStyleClass().add("heading");
        gamePieces.getChildren().addAll(gamePiecesHeading, pieceBoardRows);
        mainPane.setCenter(gamePieces);
    }

    /**
     * Handles a key being pressed
     * @param event the event
     */
    public void keyboardSupport(KeyEvent event) {
        logger.info("Handling a key being pressed");

        //Returns to the menuScene if the ESC key is pressed
        if(event.getCode() == KeyCode.ESCAPE) {
            Multimedia.playAudio("sounds/rotate.wav");
            Multimedia.getMusicPlayer().stop();
            gameWindow.startMenu();
        }
    }
}
