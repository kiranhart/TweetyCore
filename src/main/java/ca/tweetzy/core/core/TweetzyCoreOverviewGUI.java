package ca.tweetzy.core.core;

import ca.tweetzy.core.TweetyCore;
import ca.tweetzy.core.compatibility.XMaterial;
import ca.tweetzy.core.configuration.editor.PluginConfigGui;
import ca.tweetzy.core.gui.Gui;
import ca.tweetzy.core.utils.TextUtils;
import ca.tweetzy.core.utils.items.TItemBuilder;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

/**
 * The current file has been created by Kiran Hart
 * Date Created: March 02 2021
 * Time Created: 6:10 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class TweetzyCoreOverviewGUI extends Gui {

    protected TweetzyCoreOverviewGUI() {
        List<PluginInfo> plugins = TweetyCore.getPlugins();
        int max = (int) Math.ceil(plugins.size() / 9.);
        setRows(max);
        setTitle("Tweetzy Plugins");

        for (int i = 0; i < plugins.size(); i++) {
            final PluginInfo plugin = plugins.get(i);
            setItem(i, new TItemBuilder(
                    XMaterial.matchXMaterial(plugin.getIcon()).get().parseMaterial())
                    .setName(TextUtils.formatText("&e" + plugin.getName()))
                    .setLore(
                            TextUtils.formatText("&fVersion&f: &a" + plugin.getVersion()),
                            TextUtils.formatText("&fHas Update&F: " + (plugin.isHasUpdate() ? "&a" + plugin.isHasUpdate() : "&c" + plugin.isHasUpdate())),
                            "",
                            TextUtils.formatText("&eLeft Click to edit plugin settings.")
                    ).toItemStack());

            setAction(i, ClickType.LEFT, e -> e.manager.showGUI(e.player, new PluginConfigGui(plugin.getJavaPlugin(), e.gui, "&8[&eTweetyCore&8]")));
        }
    }
}
