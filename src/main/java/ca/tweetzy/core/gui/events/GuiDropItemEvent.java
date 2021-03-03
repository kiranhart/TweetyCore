package ca.tweetzy.core.gui.events;

import ca.tweetzy.core.gui.Gui;
import ca.tweetzy.core.gui.GuiManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * The current file has been created by Kiran Hart
 * Date Created: March 02 2021
 * Time Created: 4:51 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class GuiDropItemEvent extends GuiEvent {

    public final ItemStack cursor;
    public final ClickType clickType;
    public final InventoryClickEvent event;

    public GuiDropItemEvent(GuiManager manager, Gui gui, Player player, InventoryClickEvent event) {
        super(manager, gui, player);
        this.cursor = event.getCursor();
        this.clickType = event.getClick();
        this.event = event;
    }
}
