package ca.tweetzy.core.gui.methods;

import ca.tweetzy.core.gui.events.GuiDropItemEvent;

/**
 * The current file has been created by Kiran Hart
 * Date Created: March 02 2021
 * Time Created: 4:50 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public interface Droppable {

    boolean onDrop(GuiDropItemEvent event);
}
