package ca.tweetzy.core.gui.events;

import ca.tweetzy.core.gui.Gui;
import ca.tweetzy.core.gui.GuiManager;
import org.bukkit.entity.Player;

/**
 * The current file has been created by Kiran Hart
 * Date Created: March 02 2021
 * Time Created: 4:50 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class GuiCloseEvent extends GuiEvent {

    public GuiCloseEvent(GuiManager manager, Gui gui, Player player) {
        super(manager, gui, player);
    }

}
