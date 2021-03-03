package ca.tweetzy.core.configuration.editor;

import ca.tweetzy.core.compatibility.XMaterial;
import ca.tweetzy.core.configuration.Config;
import ca.tweetzy.core.gui.Gui;
import ca.tweetzy.core.gui.GuiUtils;
import ca.tweetzy.core.gui.SimplePagedGui;
import ca.tweetzy.core.input.ChatPrompt;
import ca.tweetzy.core.utils.items.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * The current file has been created by Kiran Hart
 * Date Created: March 02 2021
 * Time Created: 6:55 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class ConfigEditorGui extends SimplePagedGui {

    final JavaPlugin plugin;
    final String file;
    final MemoryConfiguration config;
    final ConfigurationSection node;
    final Player player;
    Method configSection_getCommentString = null;
    boolean edits = false;
    List<String> sections = new ArrayList<>();
    List<String> settings = new ArrayList<>();

    protected ConfigEditorGui(Player player, JavaPlugin plugin, Gui parent, String file, MemoryConfiguration config) {
        this(player, plugin, parent, file, config, config);
    }

    protected ConfigEditorGui(Player player, JavaPlugin plugin, Gui parent, String file, MemoryConfiguration config, ConfigurationSection node) {
        super(parent);
        this.player = player;
        this.plugin = plugin;
        this.file = file;
        this.config = config;
        this.node = node;
        this.blankItem = GuiUtils.getBorderItem(XMaterial.CYAN_STAINED_GLASS_PANE);

        if (!(parent instanceof ConfigEditorGui)) {
            setOnClose((gui) -> save());
        } else {
            setOnClose((gui) -> ((ConfigEditorGui) parent).edits |= edits);
        }

        // if we have a ConfigSection, we can also grab comments
        try {
            configSection_getCommentString = node.getClass().getDeclaredMethod("getCommentString", String.class);
        } catch (Exception ex) {
        }

        // decorate header
        this.setTitle(ChatColor.YELLOW + file);
        this.setUseHeader(true);
        headerBackItem = footerBackItem = GuiUtils.getBorderItem(XMaterial.GRAY_STAINED_GLASS_PANE);
        final String path = node.getCurrentPath();
        this.setItem(4, configItem(XMaterial.MAP, !path.isEmpty() ? path : file, config, !path.isEmpty() ? path : null, ChatColor.BLACK.toString()));
        this.setButton(8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, "Exit"), (event) -> event.player.closeInventory());

        // compile list of settings
        for (String key : node.getKeys(false)) {
            if (node.isConfigurationSection(key)) {
                sections.add(key);
            } else {
                settings.add(key);
            }
        }

        // next we need to display the config settings
        int index = 9;
        for (final String sectionKey : sections) {
            setButton(index++, configItem(XMaterial.WRITABLE_BOOK, ChatColor.YELLOW + sectionKey, node, sectionKey, "Click to open this section"),
                    (event) -> event.manager.showGUI(event.player, new ConfigEditorGui(player, plugin, this, file, config, node.getConfigurationSection(sectionKey))));
        }

        // now display individual settings
        for (final String settingKey : settings) {
            final Object val = node.get(settingKey);
            if (val == null) continue;
            else if (val instanceof Boolean) {
                // toggle switch
                setButton(index, configItem(XMaterial.LEVER, ChatColor.YELLOW + settingKey, node, settingKey, String.valueOf((Boolean) val), "Click to toggle this setting"),
                        (event) -> this.toggle(event.slot, settingKey));
                if ((Boolean) val) {
                    highlightItem(index);
                }
            } else if (isNumber(val)) {
                // number dial
                this.setButton(index, configItem(XMaterial.CLOCK, ChatColor.YELLOW + settingKey, node, settingKey, String.valueOf((Number) val), "Click to edit this setting"),
                        (event) -> {
                            event.gui.exit();
                            ChatPrompt.showPrompt(plugin, event.player, "Enter a new number value for " + settingKey + ":", response -> {
                                if (!setNumber(event.slot, settingKey, response.getMessage().trim())) {
                                    event.player.sendMessage(ChatColor.RED + "Error: \"" + response.getMessage().trim() + "\" is not a number!");
                                }
                            }).setOnClose(() -> event.manager.showGUI(event.player, this))
                                    .setOnCancel(() -> {
                                        event.player.sendMessage(ChatColor.RED + "Edit canceled");
                                        event.manager.showGUI(event.player, this);
                                    });
                        });
            } else if (isMaterial(val)) {
                // changing a block
                // isMaterial is more of a guess, to be honest.
                setButton(index, configItem(XMaterial.STONE, ChatColor.YELLOW + settingKey, node, settingKey, val.toString(), "Click to edit this setting"),
                        (event) -> {
                            SimplePagedGui paged = new SimplePagedGui(this);
                            paged.setTitle(ChatColor.YELLOW + settingKey);
                            paged.setHeaderBackItem(headerBackItem).setFooterBackItem(footerBackItem).setDefaultItem(blankItem);
                            paged.setItem(4, configItem(XMaterial.FILLED_MAP, settingKey, node, settingKey, "Choose an item to change this value to"));
                            int i = 9;
                            for (XMaterial mat : XMaterial.getAllValidItemMaterials()) {
                                paged.setButton(i++, GuiUtils.createButtonItem(mat, mat.name()), ClickType.LEFT, (matEvent) -> {
                                    setMaterial(event.slot, settingKey, matEvent.clickedItem);
                                    matEvent.player.closeInventory();
                                });
                            }
                            event.manager.showGUI(event.player, paged);
                        });

            } else if (val instanceof String) {
                // changing a "string" value (or change to a feather for writing quill)
                setButton(index, configItem(XMaterial.STRING, ChatColor.YELLOW + settingKey, node, settingKey, val.toString(), "Click to edit this setting"),
                        (event) -> {
                            event.gui.exit();
                            ChatPrompt.showPrompt(plugin, event.player, "Enter a new value for " + settingKey + ":", response -> {
                                node.set(settingKey, response.getMessage().trim());
                                updateValue(event.slot, settingKey);
                            }).setOnClose(() -> event.manager.showGUI(event.player, this))
                                    .setOnCancel(() -> {
                                        event.player.sendMessage(ChatColor.RED + "Edit canceled");
                                        event.manager.showGUI(event.player, this);
                                    });
                        });
            } else if (val instanceof List) {
                setButton(index, configItem(XMaterial.WRITTEN_BOOK, ChatColor.YELLOW + settingKey, node, settingKey, String.format("(%d values)", ((List) val).size()), "Click to edit this setting"),
                        (event) -> {
                            event.manager.showGUI(event.player, (new ConfigEditorListEditorGui(this, settingKey, (List) val)).setOnClose((gui) -> {
                                if (((ConfigEditorListEditorGui) gui.gui).saveChanges) {
                                    setList(event.slot, settingKey, ((ConfigEditorListEditorGui) gui.gui).values);
                                }
                            }));
                        });
            } else {
                // idk. should we display uneditable values?
            }

            ++index;
        }

    }

    public ConfigurationSection getCurrentNode() {
        return node;
    }

    protected void updateValue(int clickCell, String path) {
        ItemStack item = inventory.getItem(clickCell);
        if (item == null || item == AIR) return;
        ItemMeta meta = item.getItemMeta();
        Object val = node.get(path);
        if (meta != null && val != null) {
            String valStr;
            if (val instanceof List) {
                valStr = String.format("(%d values)", ((List) val).size());
            } else {
                valStr = val.toString();
            }
            List<String> lore = meta.getLore();
            if (lore == null || lore.isEmpty()) {
                meta.setLore(Arrays.asList(valStr));
            } else {
                lore.set(0, valStr);
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
            setItem(clickCell, item);
        }
        edits = true;
    }

    void toggle(int clickCell, String path) {
        boolean val = !node.getBoolean(path);
        node.set(path, val);
        if (val) {
            setItem(clickCell, ItemUtils.addGlow(inventory.getItem(clickCell)));
        } else {
            setItem(clickCell, ItemUtils.removeGlow(inventory.getItem(clickCell)));
        }
        updateValue(clickCell, path);
    }

    boolean setNumber(int clickCell, String path, String input) {
        try {
            if (node.isInt(path)) {
                node.set(path, Integer.parseInt(input));
            } else if (node.isDouble(path)) {
                node.set(path, Double.parseDouble(input));
            } else if (node.isLong(path)) {
                node.set(path, Long.parseLong(input));
            }
            updateValue(clickCell, path);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    void setMaterial(int clickCell, String path, ItemStack item) {
        XMaterial mat = XMaterial.matchXMaterial(item);
        if (mat == null) {
            node.set(path, XMaterial.STONE.name());
        } else {
            node.set(path, mat.name());
        }
        updateValue(clickCell, path);
    }

    void setList(int clickCell, String path, List<String> list) {
        node.set(path, list);
        updateValue(clickCell, path);
    }

    void save() {
        if (!edits) {
            return;
        }
        // could also check and call saveChanges()
        if (config instanceof FileConfiguration) {
            try {
                ((FileConfiguration) config).save(new File(plugin.getDataFolder(), file));
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save config changes to " + file, ex);
                return;
            }
        } else if (config instanceof Config) {
            ((Config) config).save();
        } else {
            player.sendMessage(ChatColor.RED + "Unknown configuration type '" + config.getClass().getName() + "' - Please report this error!");
            plugin.getLogger().log(Level.WARNING, "Unknown configuration type '" + config.getClass().getName() + "' - Please report this error!");
            return;
        }
        plugin.reloadConfig();
        player.sendMessage(ChatColor.GREEN + "Config " + file + " saved!");
    }

    private boolean isNumber(Object value) {
        return value != null && (
                value instanceof Long
                        || value instanceof Integer
                        || value instanceof Float
                        || value instanceof Double);
    }

    private boolean isMaterial(Object value) {
        XMaterial m;
        return value instanceof String && value.toString().equals(value.toString().toUpperCase())
                && (m = XMaterial.getMaterial(value.toString())) != null;
    }

    protected ItemStack configItem(XMaterial type, String name, ConfigurationSection node, String path, String def) {
        String[] info = null;
        if (configSection_getCommentString != null) {
            try {
                Object comment = configSection_getCommentString.invoke(node, path);
                if (comment != null) {
                    info = comment.toString().split("\n");
                }
            } catch (Exception ex) {
            }
        }
        return GuiUtils.createButtonItem(type, name, info != null ? info : (def != null ? def.split("\n") : null));
    }

    protected ItemStack configItem(XMaterial type, String name, ConfigurationSection node, String path, String value, String def) {
        if (value == null) value = "";
        String[] info = null;
        if (configSection_getCommentString != null) {
            try {
                Object comment = configSection_getCommentString.invoke(node, path);
                if (comment != null) {
                    info = (value + "\n" + comment.toString()).split("\n");
                }
            } catch (Exception ex) {
            }
        }
        return GuiUtils.createButtonItem(type, name, info != null ? info : (def != null ? (value + "\n" + def).split("\n") : null));
    }
}