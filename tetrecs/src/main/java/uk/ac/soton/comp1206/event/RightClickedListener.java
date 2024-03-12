package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;

/**
 * The Right Clicked listener is used to handle the event when a block in a GameBoard is right-clicked.
 */
public interface RightClickedListener {

    /**
     * Handles a right clicked event
     * @param block the block that was right-clicked
     */
    public void rightClicked(GameBlock block);
}

