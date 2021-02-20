package ca.tweetzy.core.core;

import ca.tweetzy.core.TweetyCore;
import ca.tweetzy.core.compatibility.XMaterial;
import ca.tweetzy.core.inventory.TInventory;
import ca.tweetzy.core.utils.TextUtils;
import ca.tweetzy.core.utils.items.TItemBuilder;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/13/2020
 * Time Created: 7:42 PM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public class TweetzyOverviewInventory extends TInventory {

    private List<List<PluginInfo>> chunks;

    public TweetzyOverviewInventory() {
        setTitle("&eTweetzy Plugin(s) Overview");
        setRows(2);
        setPage(1);
        setDynamic(false);

        chunks = Lists.partition(TweetyCore.getPlugins(), 9);
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, getSize(), getTitle());
        chunks.get(getPage() - 1).forEach(plugin -> {
            inventory.setItem(inventory.firstEmpty(), new TItemBuilder(
                    XMaterial.matchXMaterial(plugin.getIcon()).get().parseMaterial())
                    .setName(TextUtils.formatText("&e" + plugin.getName()))
                    .setLore(TextUtils.formatText("&fVersion&f: &a" + plugin.getVersion()), TextUtils.formatText("&fHas Update&F: " + (plugin.isHasUpdate() ? "&a" + plugin.isHasUpdate() : "&c" + plugin.isHasUpdate())))
                    .toItemStack()
            );
        });
        fillRow(inventory, XMaterial.BLUE_STAINED_GLASS_PANE.parseItem(), 2);
        return inventory;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
    }
}
