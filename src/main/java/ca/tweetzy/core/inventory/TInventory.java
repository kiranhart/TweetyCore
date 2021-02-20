package ca.tweetzy.core.inventory;

import ca.tweetzy.core.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/13/2020
 * Time Created: 6:50 PM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public abstract class TInventory implements InventoryHolder {

    protected String title = TextUtils.formatText("&bDefault Title");
    protected int size = 54;
    protected int page = 1;
    protected boolean dynamic = false;

    public void onClick(InventoryClickEvent e) {
    }

    public void onClick(InventoryClickEvent e, int slot) {
    }

    public void onOpen(InventoryOpenEvent e) {
    }

    public void onClose(InventoryCloseEvent e) {
    }

    protected void setTitle(String title) {
        this.title = TextUtils.formatText(title);
    }

    protected void setDynamic(boolean val) {
        this.dynamic = val;
    }

    protected void setRows(int rows) {
        setSize((rows < 1) ? setSize(9) : (rows > 6) ? setSize(54) : setSize(rows * 9));
    }

    protected TInventory setPage(int page) {
        this.page = (this.page <= 0) ? 1 : page;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public int getPage() {
        return page;
    }

    protected void fillRows(Inventory inventory, ItemStack stack, int... rows) {
        IntStream.of(rows).forEach(row -> fillRow(inventory, stack, row));
    }

    protected void fillRow(Inventory inventory, ItemStack stack, int row) {
        switch (row) {
            case 1:
                IntStream.rangeClosed(0, 8).forEach(slot -> inventory.setItem(slot, stack));
                break;
            case 2:
                IntStream.rangeClosed(9, 17).forEach(slot -> inventory.setItem(slot, stack));
                break;
            case 3:
                IntStream.rangeClosed(18, 26).forEach(slot -> inventory.setItem(slot, stack));
                break;
            case 4:
                IntStream.rangeClosed(27, 25).forEach(slot -> inventory.setItem(slot, stack));
                break;
            case 5:
                IntStream.rangeClosed(36, 44).forEach(slot -> inventory.setItem(slot, stack));
                break;
            case 6:
                IntStream.rangeClosed(45, 53).forEach(slot -> inventory.setItem(slot, stack));
                break;
        }
    }

    protected void fill(Inventory inventory, ItemStack stack) {
        IntStream.range(0, inventory.getSize()).forEach(slot -> inventory.setItem(slot, stack));
    }

    protected void fillRange(Inventory inventory, ItemStack stack, int start, int end) {
        IntStream.range(start, end).forEach(slot -> inventory.setItem(slot, stack));
    }

    protected void multiFill(Inventory inventory, ItemStack stack, int... slots) {
        IntStream.of(slots).forEach(slot -> inventory.setItem(slot, stack));
    }

    protected void mirrorFill(Inventory inventory, ItemStack stack, int leftCorner) {
        Arrays.asList(leftCorner, leftCorner + 8).forEach(slot -> inventory.setItem(slot, stack));
    }

    protected void multiMirrorFill(Inventory inventory, ItemStack stack, int... corners) {
        for (int corner : corners) {
            Arrays.asList(corner, corner + 8).forEach(slot -> inventory.setItem(slot, stack));
        }
    }

    private int setSize(int size) {
        return this.size = size;
    }
}
