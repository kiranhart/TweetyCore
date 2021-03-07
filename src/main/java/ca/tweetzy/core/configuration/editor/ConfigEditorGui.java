package ca.tweetzy.core.configuration.editor;

import ca.tweetzy.core.compatibility.XMaterial;
import ca.tweetzy.core.compatibility.XSound;
import ca.tweetzy.core.configuration.Config;
import ca.tweetzy.core.gui.Gui;
import ca.tweetzy.core.gui.GuiUtils;
import ca.tweetzy.core.gui.SimplePagedGui;
import ca.tweetzy.core.input.ChatPrompt;
import ca.tweetzy.core.utils.TextUtils;
import ca.tweetzy.core.utils.items.ItemUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
    public boolean edits = false;
    List<String> sections = new ArrayList<>();
    List<String> settings = new ArrayList<>();

    final String savePrefix;

    public ConfigEditorGui(Player player, JavaPlugin plugin, Gui parent, String file, MemoryConfiguration config, String savePrefix) {
        this(player, plugin, parent, file, config, config, savePrefix);
    }

    public ConfigEditorGui(Player player, JavaPlugin plugin, Gui parent, String file, String title, String savePrefix, MemoryConfiguration config, ConfigurationSection node) {
        this(player, plugin, parent, file, config, node, savePrefix);
        this.setTitle(TextUtils.formatText(title));
    }

    public ConfigEditorGui(Player player, JavaPlugin plugin, Gui parent, String file, MemoryConfiguration config, ConfigurationSection node, String savePrefix) {
        super(parent);
        this.player = player;
        this.plugin = plugin;
        this.file = file;
        this.config = config;
        this.node = node;
        this.blankItem = XMaterial.AIR.parseItem();
        this.savePrefix = savePrefix;


        if (!(parent instanceof ConfigEditorGui)) {
            setOnClose((gui) -> save(this.savePrefix));
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
        this.setItem(4, configItem(XMaterial.OAK_SIGN, !path.isEmpty() ? path : file, config, !path.isEmpty() ? path : null, ChatColor.BLACK.toString()));
        this.setButton(8, GuiUtils.createButtonItem(XMaterial.BARRIER, "&cExit"), (event) -> event.player.closeInventory());

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
            setButton(index++, configItem(XMaterial.WRITABLE_BOOK, ChatColor.YELLOW + sectionKey, node, sectionKey, "&7Click to open this section"),
                    (event) -> event.manager.showGUI(event.player, new ConfigEditorGui(player, plugin, this, file, config, node.getConfigurationSection(sectionKey), this.savePrefix)));
        }

        // now display individual settings
        for (final String settingKey : settings) {
            final Object val = node.get(settingKey);
            if (val == null) continue;
            else if (val instanceof Boolean) {
                // toggle switch
                setButton(index, configItem(XMaterial.RED_DYE, ChatColor.YELLOW + settingKey, node, settingKey, String.valueOf((Boolean) val), "&7Click to toggle this setting"), (event) -> {
                    this.toggle(event.slot, settingKey);
                });

                if ((Boolean) val) {
                    updateItemType(index, XMaterial.LIME_DYE);
                    highlightItem(index);
                }

            } else if (isNumber(val)) {
                // number dial
                this.setButton(index, configItem(XMaterial.CLOCK, ChatColor.YELLOW + settingKey, node, settingKey, String.valueOf((Number) val), "&7Click to edit this setting"),
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
            } else if (val instanceof String) {
                // changing a "string" value (or change to a feather for writing quill)

                if (XMaterial.contains(((String) val).toUpperCase())) {
                    setButton(index, configItem(XMaterial.CRAFTING_TABLE, ChatColor.YELLOW + settingKey, node, settingKey, val.toString(), "&7Click to edit this setting"),
                            (event) -> {
                                SimplePagedGui paged = new SimplePagedGui(this);
                                paged.setTitle(ChatColor.YELLOW + settingKey);
                                paged.setUseLockedCells(true);
                                paged.setHeaderBackItem(headerBackItem).setFooterBackItem(footerBackItem).setDefaultItem(blankItem);
                                paged.setItem(4, configItem(XMaterial.OAK_SIGN, settingKey, node, settingKey, "&7Choose an item to change this value to"));
                                int i = 9;

                                List<Material> supportedVersionItems = new ArrayList<>();
                                for (XMaterial mat : XMaterial.getAllValidItemMaterials()) {
                                    if (mat.isSupported() && mat.parseMaterial() != null) {
                                        supportedVersionItems.add(mat.parseMaterial());
                                    }
                                }
                                supportedVersionItems = supportedVersionItems.stream().distinct().collect(Collectors.toList());

                                for (Material material : supportedVersionItems) {
                                    paged.setButton(i, GuiUtils.createButtonItem(material, material.name().toLowerCase().replace("_", " ")), ClickType.LEFT, (matEvent) -> {
                                        setMaterial(event.slot, settingKey, matEvent.clickedItem);
                                        matEvent.player.closeInventory();
                                    });
                                    i++;
                                }
                                event.manager.showGUI(event.player, paged);
                            });
                } else if (XSound.contains(((String) val).toUpperCase())) {
                    setButton(index, configItem(XMaterial.MUSIC_DISC_13, ChatColor.YELLOW + settingKey, node, settingKey, val.toString(), "&7Click to edit this setting"),
                            (event) -> {
                                SimplePagedGui paged = new SimplePagedGui(this);
                                paged.setTitle(ChatColor.YELLOW + settingKey);
                                paged.setUseLockedCells(true);
                                paged.setHeaderBackItem(headerBackItem).setFooterBackItem(footerBackItem).setDefaultItem(blankItem);
                                paged.setItem(4, configItem(XMaterial.OAK_SIGN, settingKey, node, settingKey, "&7Choose an item to change this value to"));
                                int i = 9;
                                for (XSound sound : XSound.getAllValidSounds()) {
                                    if (sound.isSupported()) {
                                        paged.setButton(i++, GuiUtils.createButtonItem(XMaterial.MUSIC_DISC_CHIRP, WordUtils.capitalize(sound.name().replace('_', ' ').toLowerCase(Locale.ENGLISH))), ClickType.LEFT, (matEvent) -> {
                                            setSound(event.slot, settingKey, sound);
                                            matEvent.player.closeInventory();
                                        });
                                    }
                                }
                                event.manager.showGUI(event.player, paged);
                            });
                } else {
                    setButton(index, configItem(XMaterial.FEATHER, ChatColor.YELLOW + settingKey, node, settingKey, val.toString(), "&7Click to edit this setting"),
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

                }

            } else if (val instanceof List) {
                setButton(index, configItem(XMaterial.COMMAND_BLOCK, ChatColor.YELLOW + settingKey, node, settingKey, String.format("(%d values)", ((List) val).size()), "&7Click to edit this setting"),
                        (event) -> {
                            event.manager.showGUI(event.player, (new ConfigEditorListEditorGui(this, settingKey, (List) val)).setOnClose((gui) -> {
                                if (((ConfigEditorListEditorGui) gui.gui).saveChanges) {
                                    setList(event.slot, settingKey, ((ConfigEditorListEditorGui) gui.gui).values);
                                }
                            }));
                        });
            } else {

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
            updateItemType(clickCell, XMaterial.LIME_DYE);
        } else {
            setItem(clickCell, ItemUtils.removeGlow(inventory.getItem(clickCell)));
            updateItemType(clickCell, XMaterial.RED_DYE);
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
        XMaterial mat = item == null ? XMaterial.PAPER : XMaterial.matchXMaterial(item);
        if (mat == null) {
            node.set(path, XMaterial.STONE.name());
        } else {
            node.set(path, mat.name());
        }
        updateValue(clickCell, path);
    }

    void setSound(int clickCell, String path, XSound sound) {
        if (sound == null) {
            node.set(path, XSound.UI_BUTTON_CLICK.name());
        } else {
            node.set(path, sound.name());
        }
        updateValue(clickCell, path);
    }

    void setList(int clickCell, String path, List<String> list) {
        node.set(path, list);
        updateValue(clickCell, path);
    }

    public void save(String savePrefix) {
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
        player.sendMessage(TextUtils.formatText(savePrefix.length() != 0 ? savePrefix + " " + "&aSaved " + file + "!" : "" + "&aSaved " + file + "!"));
    }

    private boolean isNumber(Object value) {
        return value != null && (
                value instanceof Long
                        || value instanceof Integer
                        || value instanceof Float
                        || value instanceof Double);
    }

    protected ItemStack configItem(XMaterial type, String name, ConfigurationSection node, String path, String def) {
        String[] info = null;
        if (configSection_getCommentString != null) {
            try {
                Object comment = configSection_getCommentString.invoke(node, path);
                if (comment != null) {
                    info = comment.toString().split("\n");
                    for (int i = 0; i < info.length; i++) {
                        info[i] = TextUtils.formatText("&7" + info[i]);
                    }
                }
            } catch (Exception ex) {
            }
        }

        String[] updatedDef = null;
        if (def != null) {
            updatedDef = def.split("\n");
            for (int i = 0; i < updatedDef.length; i++) {
                updatedDef[i] = TextUtils.formatText("&7" + updatedDef[i]);
            }
        }
        return GuiUtils.createButtonItem(type, name, info != null ? info : updatedDef);
    }

    protected ItemStack configItem(XMaterial type, String name, ConfigurationSection node, String path, String value, String def) {
        if (value == null) value = "";
        String[] info = null;
        if (configSection_getCommentString != null) {
            try {
                Object comment = configSection_getCommentString.invoke(node, path);
                if (comment != null) {
                    info = (TextUtils.formatText(TextUtils.containsColorCode(value) ? value : "&e" + value) + "\n\n" + TextUtils.formatText("&7" + comment.toString())).split("\n");
                    for (int i = 0; i < info.length; i++) {
                        info[i] = TextUtils.formatText("&7" + info[i]);
                    }
                }
            } catch (Exception ex) {
            }
        }
        return GuiUtils.createButtonItem(type, name, info != null ? info : (def != null ? (TextUtils.formatText(TextUtils.containsColorCode(value) ? value : "&e" + value) + "\n" + TextUtils.formatText("&7" + def)).split("\n") : null));
    }

}