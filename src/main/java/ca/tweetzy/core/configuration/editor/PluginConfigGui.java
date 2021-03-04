package ca.tweetzy.core.configuration.editor;

import ca.tweetzy.core.TweetyPlugin;
import ca.tweetzy.core.compatibility.XMaterial;
import ca.tweetzy.core.configuration.Config;
import ca.tweetzy.core.gui.Gui;
import ca.tweetzy.core.gui.GuiUtils;
import ca.tweetzy.core.gui.SimplePagedGui;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The current file has been created by Kiran Hart
 * Date Created: March 02 2021
 * Time Created: 6:48 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class PluginConfigGui extends SimplePagedGui {

    final JavaPlugin plugin;
    LinkedHashMap<String, MemoryConfiguration> configs = new LinkedHashMap<>();

    public PluginConfigGui(TweetyPlugin plugin) {
        this(plugin, null);
    }

    public PluginConfigGui(TweetyPlugin plugin, Gui parent) {
        super(parent);
        this.plugin = plugin;

        // collect list of plugins
        configs.put(plugin.getCoreConfig().getFile().getName(), plugin.getCoreConfig());
        List<Config> more = plugin.getExtraConfig();
        if (more != null && !more.isEmpty()) {
            for (Config cfg : more) {
                configs.put(cfg.getFile().getName(), cfg);
            }
        }
        init();
    }

    public PluginConfigGui(JavaPlugin plugin) {
        this(plugin, null);
    }

    public PluginConfigGui(JavaPlugin plugin, Gui parent) {
        super(parent);
        this.plugin = plugin;

        // collect list of plugins
        configs.put("config.yml", plugin.getConfig());

        try {
            // can we also grab extra config from this mysterious plugin?
            Object more = plugin.getClass().getDeclaredMethod("getExtraConfig").invoke(plugin);
            if (more != null && more instanceof List && !((List) more).isEmpty()) {
                try {
                    // if we have the getExtraConfig function, we should also be able to get the file
                    Method method_Config_getFile = ((List) more).get(0).getClass().getDeclaredMethod("getFile");
                    for (Object cfg : ((List) more)) {
                        configs.put(((File) method_Config_getFile.invoke(cfg)).getName(), (MemoryConfiguration) cfg);
                    }
                } catch (Exception ex) {
                    // include a failsafe, I guess
                    ((List) more).forEach(cfg -> configs.put("(File " + configs.size() + ")", (MemoryConfiguration) cfg));
                }
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            // I guess not!
        }
        init();
    }

    private void init() {
        this.blankItem = GuiUtils.getBorderItem(XMaterial.CYAN_STAINED_GLASS_PANE);

        // decorate header
        this.setTitle(ChatColor.YELLOW + plugin.getName() + " Plugin Config");
        this.setUseHeader(true);
        headerBackItem = footerBackItem = GuiUtils.getBorderItem(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem());
        this.setButton(8, GuiUtils.createButtonItem(XMaterial.BARRIER, "&cExit"), (event) -> event.player.closeInventory());

        // List out all config files that this plugin has
        int i = 9;
        for (Map.Entry<String, MemoryConfiguration> config : configs.entrySet()) {
            this.setButton(i++, GuiUtils.createButtonItem(XMaterial.BOOK, ChatColor.YELLOW + config.getKey(), "&7Click to edit this config"),
                    (event) -> event.manager.showGUI(event.player, new ConfigEditorGui(event.player, plugin, this, config.getKey(), config.getValue())));
        }
    }
}
