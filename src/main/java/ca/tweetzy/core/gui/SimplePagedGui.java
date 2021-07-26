package ca.tweetzy.core.gui;

import ca.tweetzy.core.compatibility.XMaterial;
import ca.tweetzy.core.gui.events.GuiClickEvent;
import ca.tweetzy.core.gui.methods.Clickable;
import ca.tweetzy.core.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * The current file has been created by Kiran Hart
 * Date Created: March 02 2021
 * Time Created: 6:49 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class SimplePagedGui extends Gui {

    protected boolean useHeader;
    private int rowsPerPage, maxCellSlot;
    protected ItemStack headerBackItem;
    protected ItemStack footerBackItem;
    final int nextPageIndex = 4, prevPageIndex = 6;

    public SimplePagedGui() {
        this(null);
    }

    public SimplePagedGui(Gui parent) {
        super(parent);

        nextPage = GuiUtils.createButtonItem(XMaterial.ARROW, "Next Page");
        prevPage = GuiUtils.createButtonItem(XMaterial.ARROW, "Previous Page");
    }

    public SimplePagedGui setUseHeader(boolean useHeader) {
        this.useHeader = useHeader;
        return this;
    }

    public ItemStack getHeaderBackItem() {
        return headerBackItem;
    }

    public SimplePagedGui setHeaderBackItem(ItemStack headerBackItem) {
        this.headerBackItem = headerBackItem;
        return this;
    }

    public ItemStack getFooterBackItem() {
        return footerBackItem;
    }

    public SimplePagedGui setFooterBackItem(ItemStack footerBackItem) {
        this.footerBackItem = footerBackItem;
        return this;
    }

    @Override
    public SimplePagedGui setItem(int row, int col, ItemStack item) {
        return setItem(col + row * 9, item);
    }

    @Override
    public SimplePagedGui setItem(int cell, ItemStack item) {
        // set the cell relative to the current page
        int cellIndex = cell < 0 ? cell : (page == 1 || (useHeader && cell < 9) ? cell : (cell + (page - 1) * (rowsPerPage * 9)));

        cellItems.put(cellIndex, item);
        if (open && cell >= 0 && cell < inventory.getSize()) {
            inventory.setItem(cell, item);
        }
        return this;
    }

    @Override
    public void nextPage() {
        if (page < pages) {
            ++page;
            showPage();
        }
    }

    @Override
    public void prevPage() {
        if (page > 1) {
            --page;
            showPage();
        }
    }

    public void showPage() {
        int startCell = useHeader ? 9 : 0;
        int cellIndex = startCell + (page - 1) * (rowsPerPage * 9);

        for (int i = startCell; i < (rows - 1) * 9; ++i) {
            final ItemStack item = cellItems.get(cellIndex++);
            inventory.setItem(i, item != null ? item : useLockedCells ? blankItem : XMaterial.AIR.parseItem());
        }
        // page markers
        updatePageNavigation();
    }

    @Override
    protected void updatePageNavigation() {
        if (page > 1) {
            inventory.setItem(inventory.getSize() - prevPageIndex, prevPage);
            this.setButton(-prevPageIndex, prevPage, ClickType.LEFT, (event) -> this.prevPage());
        } else {
            inventory.setItem(inventory.getSize() - prevPageIndex, footerBackItem != null ? footerBackItem : blankItem);
            this.setItem(-prevPageIndex, null);
            this.clearActions(-prevPageIndex);
        }
        if (pages > 1 && page != pages) {
            inventory.setItem(inventory.getSize() - nextPageIndex, nextPage);
            this.setButton(-nextPageIndex, nextPage, ClickType.LEFT, (event) -> this.nextPage());
        } else {
            inventory.setItem(inventory.getSize() - nextPageIndex, footerBackItem != null ? footerBackItem : blankItem);
            this.setItem(-nextPageIndex, null);
            this.clearActions(-nextPageIndex);
        }
    }

    @Override
    protected Inventory generateInventory(GuiManager manager) {
        this.guiManager = manager;
        // calculate pages here
        rowsPerPage = useHeader ? 4 : 5;
        maxCellSlot = (this.cellItems.isEmpty() ? 0 : this.cellItems.keySet().stream().max(Integer::compare).get()) + 1;
        int maxRows = (int) Math.ceil(maxCellSlot / 9.);
        pages = (int) Math.max(1, Math.ceil(maxRows / (double) rowsPerPage));
        this.setRows(maxRows + (useHeader ? 1 : 0));

        // create inventory view
        createInventory();

        // populate and return the display inventory
        setPage(Math.min(page, pages));
        update();
        return inventory;
    }

    @Override
    protected void createInventory() {
        final int cells = rows * 9;
        inventory = Bukkit.getServer().createInventory(new GuiHolder(guiManager, this), cells, title == null ? "" : TextUtils.formatText(trimTitle(title)));
    }

    @Override
    public void update() {
        if (inventory == null) {
            return;
        }

        // calculate pages here
        rowsPerPage = useHeader ? 4 : 5;
        maxCellSlot = (this.cellItems.isEmpty() ? 0 : this.cellItems.keySet().stream().max(Integer::compare).get()) + 1;
        int maxRows = Math.max((useHeader ? 1 : 0), (int) Math.ceil(maxCellSlot / 9.));
        pages = (int) Math.ceil(maxRows / rowsPerPage);

        // create a new inventory if needed
        List<Player> toUpdate = null;
        if (Math.min(54, (maxRows + (useHeader ? 1 : 0)) * 9) != inventory.getSize()) {
            toUpdate = getPlayers();
            this.setRows(maxRows + (useHeader ? 1 : 0));
            createInventory();
        }

        // populate header
        if (useHeader) {
            for (int i = 0; i < 9; ++i) {
                final ItemStack item = cellItems.get(i);
                inventory.setItem(i, item != null ? item : (headerBackItem != null ? headerBackItem : blankItem));
            }
        }

        // last row is dedicated to pagation
        final int cells = rows * 9;
        for (int i = cells - 9; i < cells; ++i) {
            inventory.setItem(i, footerBackItem != null ? footerBackItem : blankItem);
        }

        // fill out the rest of the page
        showPage();

        // did we need to change the display window size?
        if (toUpdate != null) {
            // whoopsie!
            exit();
            toUpdate.forEach(player -> guiManager.showGUI(player, this));
        }
    }

    @Override
    protected boolean onClick(GuiManager manager, Player player, Inventory inventory, InventoryClickEvent event) {
        int cell = event.getSlot();
        Map<ClickType, Clickable> conditionals;

        if (useHeader && cell < 9) {
            conditionals = conditionalButtons.get(cell);
        } else if (cell >= (rows - 1) * 9) {
            // footer row
            conditionals = conditionalButtons.get(cell - (rows * 9));
        } else {
            int cellIndex = page == 1 || (useHeader && cell < 9) ? cell : (cell + (page - 1) * (rowsPerPage * 9));
            conditionals = conditionalButtons.get(cellIndex);
        }

        Clickable button;
        if (conditionals != null && ((button = conditionals.get(event.getClick())) != null || (button = conditionals.get(null)) != null)) {
            button.onClick(new GuiClickEvent(manager, this, player, event, cell, true));
        } else {
            // no event for this button
            return false;
        }
        return true;
    }
}
