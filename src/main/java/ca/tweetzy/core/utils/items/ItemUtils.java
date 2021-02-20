package ca.tweetzy.core.utils.items;

import ca.tweetzy.core.compatibility.ServerVersion;
import ca.tweetzy.core.compatibility.XMaterial;
import ca.tweetzy.core.utils.nms.NBTEditor;
import ca.tweetzy.core.utils.TextUtils;
import ca.tweetzy.core.utils.nms.NMSUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/13/2020
 * Time Created: 7:03 PM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public class ItemUtils {

    public static String getItemName(ItemStack it) {
        if (it == null) {
            return null;
        } else {
            return itemName(it.getType());
        }
    }

    static String itemName(Material mat) {
        String matName = mat.name().replace("_", " ");
        StringBuilder titleCase = new StringBuilder(matName.length());

        Stream.of(matName.split(" ")).forEach(s -> {
            s = s.toLowerCase();
            if (s.equals("of")) {
                titleCase.append(s).append(" ");
            } else {
                char[] str = s.toCharArray();
                str[0] = Character.toUpperCase(str[0]);
                titleCase.append(new String(str)).append(" ");
            }
        });

        return titleCase.toString().trim();
    }

    /**
     * @param item item to copy
     * @param qty  amount the new ItemStack should have
     * @return a copy of the original item
     */
    public static ItemStack getAsCopy(ItemStack item, int qty) {
        ItemStack clone = item.clone();
        clone.setAmount(qty);
        return clone;
    }

    public static ItemStack addDamage(ItemStack item, int damage) {
        if (item == null) {
            return null;
        } else if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
            // ItemStack.setDurability(short) still works in 1.13-1.14, but use these methods now
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof Damageable) {
                ((Damageable) meta).setDamage(((Damageable) meta).getDamage() + damage);
                item.setItemMeta(meta);
            }
        } else {
            item.setDurability((short) Math.max(0, item.getDurability() + damage));
        }
        return item;
    }

    public static ItemStack getPlayerSkull(OfflinePlayer player) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        if (ServerVersion.isServerVersionBelow(ServerVersion.V1_8)) {
            return head;
        }

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
            meta.setOwningPlayer(player);
        } else {
            meta.setOwner(player.getName());
        }
        head.setItemMeta(meta);
        return head;
    }

    public static void setHeadOwner(ItemStack head, OfflinePlayer player) {
        if (ServerVersion.isServerVersionBelow(ServerVersion.V1_8) || head == null || !XMaterial.PLAYER_HEAD.isSimilar(head)) {
            return;
        }
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
            meta.setOwningPlayer(player);
        } else {
            meta.setOwner(player.getName());
        }
    }

    public static class NBTOption {
        private String key;
        private Object val;

        public NBTOption(String key, Object val) {
            this.key = key;
            this.val = val;
        }

        public Object getVal() {
            return val;
        }

        public String getKey() {
            return key;
        }
    }

    public static class ReplaceOption {

        private String toReplace;
        private String replacement;

        public ReplaceOption(String toReplace, String replacement) {
            this.toReplace = toReplace;
            this.replacement = replacement;
        }

        public String getReplacement() {
            return replacement;
        }

        public String getToReplace() {
            return toReplace;
        }
    }


    public static ItemStack createConfigItem(FileConfiguration file, String path, ReplaceOption... replacements) {
        ItemStack stack = XMaterial.matchXMaterial(Objects.requireNonNull(file.getString(path + ".item")).toUpperCase()).orElse(XMaterial.RED_STAINED_GLASS_PANE).parseItem();
        ItemMeta meta = stack.getItemMeta();

        String displayName = file.getString(path + ".name");
        for (ReplaceOption option : replacements) {
            displayName = displayName.replace(option.getToReplace(), option.getReplacement());
        }

        meta.setDisplayName(TextUtils.formatText(displayName));
        List<String> lore = new ArrayList<>();

        file.getStringList(path + ".lore").forEach(line -> {
            for (ReplaceOption option : replacements) {
                line = line.replace(option.getToReplace(), option.getReplacement());
            }
            lore.add(TextUtils.formatText(line));
        });

        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack createNBTItem(ItemStack stack, NBTOption... nbt) {
        for (NBTOption option : nbt) {
            stack = NBTEditor.set(stack, option.getVal(), option.getKey());
        }
        return stack;
    }
}
