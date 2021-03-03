package ca.tweetzy.core.gui.events;

import ca.tweetzy.core.gui.Gui;
import ca.tweetzy.core.gui.GuiManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * The current file has been created by Kiran Hart
 * Date Created: March 02 2021
 * Time Created: 4:46 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class GuiClickEvent extends GuiEvent {

    public final int slot;
    public final boolean guiClicked;
    public final ItemStack cursor, clickedItem;
    public final ClickType clickType;
    public final InventoryClickEvent event;

    public GuiClickEvent(GuiManager manager, Gui gui, Player player, InventoryClickEvent event, int slot, boolean guiClicked) {
        super(manager, gui, player);
        this.slot = slot;
        this.guiClicked = guiClicked;
        this.cursor = event.getCursor();
        Inventory clicked = event.getClickedInventory();
        this.clickedItem = clicked == null ? null : clicked.getItem(event.getSlot());
        this.clickType = event.getClick();
        this.event = event;
    }
}
