package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.BLACK);
        gc.setGlobalAlpha(0.3);
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.GRAY);
        gc.setGlobalAlpha(1);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Color colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillPolygon(new double[]{0, width, width}, new double[]{0, 0, height}, 3);
        gc.setFill(colour.desaturate());
        gc.fillPolygon(new double[]{0, 0, width}, new double[]{0, height, height}, 3);

        //Border
        gc.setStroke(colour.darker());
        gc.setLineWidth(3);
        gc.strokeRect(0,0,width,height);
        gc.setLineWidth(1);
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * Draws a white transparent circle in the middle of this game block
     */
    public void drawIndicator() {
        logger.info("Drawing indicator at the centre of " + this);
        var gc = getGraphicsContext2D();

        //Draws Circle
        gc.setFill(Color.WHITE);
        gc.setGlobalAlpha(0.5);
        gc.fillOval(width/4, height/4, width/2, height/2);
        gc.setGlobalAlpha(1);
    }

    /**
     * Handles painting this block as a hovered over block
     */
    public void hoverOver() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colours
        this.paint();

        //Adds white transparent layer
        gc.setFill(Color.WHITE);
        gc.setGlobalAlpha(0.5);
        gc.fillRect(0, 0, width, height);
        gc.setGlobalAlpha(1);
    }

    /**
     * Fades this block out
     */
    public void fadeOut() {
        var gc = getGraphicsContext2D();

        //Opacity of this block
        var opacity = new SimpleDoubleProperty(1.2);

        //Animation timer used to decrease opacity of this block
        var timer = new AnimationTimer() {

            /**
             * Handles what the timer should do each frame
             * @param now
             *            The timestamp of the current frame given in nanoseconds. This
             *            value will be the same for all {@code AnimationTimers} called
             *            during one frame.
             */
            @Override
            public void handle(long now) {
                //Clears block
                gc.clearRect(0,0,width,height);

                //Colours block green with decreased opacity
                paint();
                opacity.set(opacity.get() - 0.02);
                gc.setGlobalAlpha(opacity.get());
                gc.setFill(Color.LIME);
                gc.fillRect(0, 0, width, height);
                gc.setGlobalAlpha(1);
            }
        };

        //Starts the animation timer
        timer.start();

        //Stops the animation timer when this block is completely transparent
        opacity.addListener(((observable, oldValue, newValue) -> {
            if(newValue.doubleValue() <= 0) {
                timer.stop();

                //Clears block
                gc.clearRect(0,0,width,height);

                //Colours block
                paint();
            }
        }));
    }
}
