package uk.ac.soton.comp1206.scene;

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * A Base Scene used in the game.
 * Handles common functionality between all scenes.
 */
public abstract class BaseScene {

    protected final GameWindow gameWindow;

    protected GamePane root;
    protected Scene scene;

    /**
     * Creates a new scene, passing in the GameWindow the scene will be displayed in
     * @param gameWindow the game window
     */
    public BaseScene(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    /**
     * Initialises this scene. Called after creation
     */
    public abstract void initialise();

    /**
     * Builds the layout of the scene
     */
    public abstract void build();

    /**
     * Creates a new JavaFX scene using the root contained within this scene
     * @return JavaFX scene
     */
    public Scene setScene() {
        var previous = gameWindow.getScene();
        Scene scene = new Scene(root, previous.getWidth(), previous.getHeight(), Color.BLACK);
        scene.getStylesheets().add(getClass().getResource("/style/game.css").toExternalForm());
        this.scene = scene;
        return scene;
    }

    /**
     * Gets the JavaFX scene contained inside
     * @return JavaFX scene
     */
    public Scene getScene() {
        return this.scene;
    }
}
