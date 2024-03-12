package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;

/**
 * The Block Hovered listener is used to handle the event when a block in a GameBoard is hovered over.
 */
public interface BlockHoveredListener {

    /**
     * Handles a block hovered event
     * @param block the block that was hovered over
     */
    public void blockHovered(GameBlock block);
}
