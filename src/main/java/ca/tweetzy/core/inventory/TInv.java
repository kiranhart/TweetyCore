package ca.tweetzy.core.inventory;

import ca.tweetzy.core.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * The current file has been created by Kiran Hart
 * Date Created: March 01 2021
 * Time Created: 5:55 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class TInv implements InventoryHolder {

    private final Map<Integer, Consumer<InventoryClickEvent>> itemHandlers = new HashMap<>();

    private Set<Consumer<InventoryOpenEvent>> openHandlers;
    private Set<Consumer<InventoryCloseEvent>> closeHandlers;
    private Set<Consumer<InventoryClickEvent>> clickHandlers;

    private Predicate<Player> closeFilter;
    private Inventory inventory;

    public int page = 1;

    /**
     * Create a new Inventory with a custom title and size
     *
     * @param title The title of the inventory
     * @param size  The size of the inventory
     */
    public TInv(String title, int size) {
        this(title, size, InventoryType.CHEST);
    }

    /**
     * Create a new Inventory with a custom type
     *
     * @param type The type of the inventory
     */
    public TInv(InventoryType type) {
        this(type.getDefaultTitle(), type);
    }

    /**
     * Create a new Inventory with a custom title and type
     *
     * @param title The title of the inventory
     * @param type  The type of the inventory
     */
    public TInv(String title, InventoryType type) {
        this(title, 0, type);
    }

    private TInv(String title, int size, InventoryType type) {
        inventory = type == InventoryType.CHEST && size > 0 ? Bukkit.createInventory(this, size, title) : Bukkit.createInventory(this, Objects.requireNonNull(type, "type"), TextUtils.formatText(title));
        if (inventory.getHolder() != this) {
            throw new IllegalStateException("The following inventory's holder is not TInventory, found" + inventory.getHolder());
        }
    }

    protected void open(InventoryOpenEvent event) {
    }

    protected void click(InventoryClickEvent event) {
    }

    protected void close(InventoryCloseEvent event) {
    }

    /**
     * Add an {@link ItemStack} to the inventory on the first empty slot.
     *
     * @param item The ItemStack to add
     */
    public void addItem(ItemStack item) {
        addItem(item, null);
    }

    /**
     * Add an {@link ItemStack} to the inventory on the first empty slot with a click handler.
     *
     * @param item    The item to add.
     * @param handler The the click handler for the item.
     */
    public void addItem(ItemStack item, Consumer<InventoryClickEvent> handler) {
        int slot = inventory.firstEmpty();
        if (slot >= 0) {
            setItem(slot, item, handler);
        }
    }

    /**
     * Add an {@link ItemStack} to the inventory on a specific slot.
     *
     * @param slot The slot where to add the item.
     * @param item The item to add.
     */
    public void setItem(int slot, ItemStack item) {
        setItem(slot, item, null);
    }

    /**
     * Add an {@link ItemStack} to the inventory on specific slot with a click handler.
     *
     * @param slot    The slot where to add the item.
     * @param item    The item to add.
     * @param handler The click handler for the item
     */
    public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> handler) {
        inventory.setItem(slot, item);
        if (handler != null) {
            itemHandlers.put(slot, handler);
        } else {
            itemHandlers.remove(slot);
        }
    }

    /**
     * Add an {@link ItemStack} to the inventory on a range of slots.
     *
     * @param slotFrom Starting slot to add the item in.
     * @param slotTo   Ending slot to add the item in.
     * @param item     The item to add.
     */
    public void setItems(int slotFrom, int slotTo, ItemStack item) {
        setItems(slotFrom, slotTo, item, null);
    }

    /**
     * Add an {@link ItemStack} to the inventory on a range of slots with a click handler.
     *
     * @param slotFrom Starting slot to put the item in.
     * @param slotTo   Ending slot to put the item in.
     * @param item     The item to add.
     * @param handler  The click handler for the item
     */
    public void setItems(int slotFrom, int slotTo, ItemStack item, Consumer<InventoryClickEvent> handler) {
        for (int i = slotFrom; i <= slotTo; i++) {
            setItem(i, item, handler);
        }
    }

    /**
     * Add an {@link ItemStack} to the inventory on multiple slots.
     *
     * @param slots The slots where to add the item
     * @param item  The item to add.
     */
    public void setItems(int[] slots, ItemStack item) {
        setItems(slots, item, null);
    }

    /**
     * Add an {@link ItemStack} to the inventory on multiples slots with a click handler.
     *
     * @param slots   The slots where to add the item
     * @param item    The item to add.
     * @param handler The click handler for the item
     */
    public void setItems(int[] slots, ItemStack item, Consumer<InventoryClickEvent> handler) {
        for (int slot : slots) {
            setItem(slot, item, handler);
        }
    }

    /**
     * Remove an {@link ItemStack} from the inventory
     *
     * @param slot The slot where to remove the item
     */
    public void removeItem(int slot) {
        inventory.clear(slot);
        itemHandlers.remove(slot);
    }

    /**
     * Remove multiples {@link ItemStack} from the inventory
     *
     * @param slots The slots where to remove the items
     */
    public void removeItems(int... slots) {
        for (int slot : slots) {
            removeItem(slot);
        }
    }

    /**
     * Add a close filter to prevent players from closing the inventory.
     * To prevent a player from closing the inventory the predicate should return {@code true}
     *
     * @param closeFilter The close filter
     */
    public void setCloseFilter(Predicate<Player> closeFilter) {
        this.closeFilter = closeFilter;
    }

    /**
     * Add a handler to handle inventory open.
     *
     * @param openHandler The handler to add.
     */
    public void addOpenHandler(Consumer<InventoryOpenEvent> openHandler) {
        if (openHandlers == null) {
            openHandlers = new HashSet<>();
        }
        openHandlers.add(openHandler);
    }

    /**
     * Add a handler to handle inventory close.
     *
     * @param closeHandler The handler to add
     */
    public void addCloseHandler(Consumer<InventoryCloseEvent> closeHandler) {
        if (closeHandlers == null) {
            closeHandlers = new HashSet<>();
        }
        closeHandlers.add(closeHandler);
    }

    /**
     * Add a handler to handle inventory click.
     *
     * @param clickHandler The handler to add.
     */
    public void addClickHandler(Consumer<InventoryClickEvent> clickHandler) {
        if (clickHandlers == null) {
            clickHandlers = new HashSet<>();
        }
        clickHandlers.add(clickHandler);
    }

    /**
     * Open the inventory to a player.
     *
     * @param player The player to open the menu.
     */
    public void open(Player player) {
        player.openInventory(inventory);
    }

    /**
     * Open the inventory to a player.
     *
     * @param player The player to open the menu.
     * @param page  The page that should be opened
     */
    public void openPage(Player player, int page) {
        this.page = this.page <= 0 ? 1 : page;
        player.openInventory(inventory);
    }

    /**
     * Get borders of the inventory. If the inventory size is under 27, all slots are returned
     *
     * @return inventory borders
     */
    public int[] getBorders() {
        int size = inventory.getSize();
        return IntStream.range(0, size).filter(i -> size < 27 || i < 9 || i % 9 == 0 || (i - 8) % 9 == 0 || i > size - 9).toArray();
    }

    /**
     * Get corners of the inventory.
     *
     * @return inventory corners
     */
    public int[] getCorners() {
        int size = inventory.getSize();
        return IntStream.range(0, size).filter(i -> i < 2 || (i > 6 && i < 10) || i == 17 || i == size - 18 || (i > size - 11 && i < size - 7) || i > size - 3).toArray();
    }

    /**
     * Get the Bukkit inventory
     *
     * @return The Bukkit inventory.
     */
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void handleOpen(InventoryOpenEvent e) {
        open(e);

        if (openHandlers != null) {
            openHandlers.forEach(c -> c.accept(e));
        }
    }

    public boolean handleClose(InventoryCloseEvent e) {
        close(e);

        if (closeHandlers != null) {
            closeHandlers.forEach(c -> c.accept(e));
        }

        return closeFilter != null && closeFilter.test((Player) e.getPlayer());
    }

    public void handleClick(InventoryClickEvent e) {
        click(e);

        if (clickHandlers != null) {
            clickHandlers.forEach(c -> c.accept(e));
        }

        Consumer<InventoryClickEvent> clickConsumer = itemHandlers.get(e.getRawSlot());

        if (clickConsumer != null) {
            clickConsumer.accept(e);
        }
    }
}
