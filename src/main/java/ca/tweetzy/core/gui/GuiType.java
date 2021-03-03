package ca.tweetzy.core.gui;

import org.bukkit.event.inventory.InventoryType;

/**
 * The current file has been created by Kiran Hart
 * Date Created: March 02 2021
 * Time Created: 4:09 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public enum GuiType {

    STANDARD(InventoryType.CHEST, 6, 9),
    DISPENSER(InventoryType.DISPENSER, 9, 3),
    HOPPER(InventoryType.HOPPER, 5, 1);

    protected final InventoryType type;
    protected final int rows;
    protected final int columns;

    GuiType(InventoryType type, int rows, int columns) {
        this.type = type;
        this.rows = rows;
        this.columns = columns;
    }
}
