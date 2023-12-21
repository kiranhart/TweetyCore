package ca.tweetzy.core.gui;

import ca.tweetzy.core.compatibility.XMaterial;
import ca.tweetzy.core.utils.TextUtils;
import ca.tweetzy.core.utils.items.TItemBuilder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class BaseGUI extends Gui {

	private final Gui parent;

	public BaseGUI(final Gui parent, final String title, final int rows) {
		this.parent = parent;
		setTitle(TextUtils.formatText(title));
		setRows(rows);
		setDefaultSound(null);
		setDefaultItem(GuiUtils.getBorderItem(XMaterial.BLACK_STAINED_GLASS_PANE));
	}

	public BaseGUI(final Gui parent, final String title) {
		this(parent, title, 6);
	}

	public BaseGUI(final String title) {
		this(null, title, 6);
	}

	/**
	 * Draw the gui.
	 */
	protected abstract void draw();


	/**
	 * It adds a back button to the bottom left of the GUI
	 *
	 * @param override The GUI to show when the back button is clicked.
	 */
	protected void applyBackExit(Gui override) {
		setButton(getBackExitButtonSlot(), getBackButton(), click -> click.manager.showGUI(click.player, override));
	}

	/**
	 * If the GUI has a parent, then the back button will be set to the back button, otherwise it will be set to the exit button
	 */
	protected void applyBackExit() {
		if (this.parent == null) {
			setButton(getBackExitButtonSlot(), getExitButton(), click -> click.gui.close());
		} else {
			setButton(getBackExitButtonSlot(), getBackButton(), click -> click.manager.showGUI(click.player, this.parent));
		}

	}

	protected List<Integer> fillSlots() {
		return IntStream.rangeClosed(0, 44).boxed().collect(Collectors.toList());
	}

	protected ItemStack getBackButton() {
		return new TItemBuilder(XMaterial.DARK_OAK_DOOR.parseMaterial()).setName("&aBack").setLore("&7Click to go back").toItemStack();
	}

	protected ItemStack getExitButton() {
		return new TItemBuilder(XMaterial.BARRIER.parseMaterial()).setName("&cExit").setLore("&7Click to close menu").toItemStack();
	}

	protected ItemStack getPreviousPageButton() {
		return new TItemBuilder(XMaterial.ARROW.parseMaterial()).setName("&ePrevious").toItemStack();
	}

	protected ItemStack getNextPageButton() {
		return new TItemBuilder(XMaterial.ARROW.parseMaterial()).setName("&eNext").toItemStack();
	}

	protected int getBackExitButtonSlot() {
		return this.rows * 9 - 9;
	}
}
