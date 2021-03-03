package ca.tweetzy.core.gui.events;

import ca.tweetzy.core.gui.Gui;
import ca.tweetzy.core.gui.GuiManager;

/**
 * The current file has been created by Kiran Hart
 * Date Created: March 02 2021
 * Time Created: 4:52 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class GuiPageEvent {

    final Gui gui;
    final GuiManager manager;
    final int lastPage;
    final int page;

    public GuiPageEvent(Gui gui, GuiManager manager, int lastPage, int page) {
        this.gui = gui;
        this.manager = manager;
        this.lastPage = lastPage;
        this.page = page;
    }
}
