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

    static Class<?> cb_ItemStack = NMSUtils.getCraftClass("inventory.CraftItemStack");
    static Class<?> mc_ItemStack = NMSUtils.getNMSClass("ItemStack");
    static Class<?> mc_NBTTagCompound = NMSUtils.getNMSClass("NBTTagCompound");
    static Class<?> mc_NBTTagList = NMSUtils.getNMSClass("NBTTagList");
    static Class<?> mc_NBTBase = NMSUtils.getNMSClass("NBTBase");
    static Method mc_ItemStack_getTag;
    static Method mc_ItemStack_setTag;
    static Method mc_NBTTagCompound_set;
    static Method mc_NBTTagCompound_remove;
    static Method mc_NBTTagCompound_setShort;
    static Method mc_NBTTagCompound_setString;
    static Method mc_NBTTagList_add;
    static Method cb_CraftItemStack_asNMSCopy;
    static Method cb_CraftItemStack_asCraftMirror;

    static {
        if (cb_ItemStack != null) {
            try {
                mc_ItemStack_getTag = mc_ItemStack.getDeclaredMethod("getTag");
                mc_ItemStack_setTag = mc_ItemStack.getDeclaredMethod("setTag", mc_NBTTagCompound);
                mc_NBTTagCompound_set = mc_NBTTagCompound.getDeclaredMethod("set", String.class, mc_NBTBase);
                mc_NBTTagCompound_remove = mc_NBTTagCompound.getDeclaredMethod("remove", String.class);
                mc_NBTTagCompound_setShort = mc_NBTTagCompound.getDeclaredMethod("setShort", String.class, short.class);
                mc_NBTTagCompound_setString = mc_NBTTagCompound.getDeclaredMethod("setString", String.class, String.class);
                cb_CraftItemStack_asNMSCopy = cb_ItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
                cb_CraftItemStack_asCraftMirror = cb_ItemStack.getDeclaredMethod("asCraftMirror", mc_ItemStack);
                mc_NBTTagList_add = ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14)
                        ? NMSUtils.getPrivateMethod(mc_NBTTagList, "a", mc_NBTBase)
                        : mc_NBTTagList.getDeclaredMethod("add", mc_NBTBase);
            } catch (Exception ex) {
                Logger.getLogger(ItemUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Make an item glow as if it contained an enchantment. <br>
     * Tested working 1.8-1.14
     *
     * @param item itemstack to create a glowing copy of
     * @return copy of item with a blank enchantment nbt tag
     */
    public static ItemStack addGlow(ItemStack item) {
        // from 1.11 up, fake enchantments don't work without more steps
        // creating a new Enchantment involves some very involved reflection,
        // as the namespace is the same but until 1.12 requires an int, but versions after require a String
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
            item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            // you can at least hide the enchantment, though
            ItemMeta m = item.getItemMeta();
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(m);
            return item;
        } else {
            // hack a fake enchant onto the item
            // Confirmed works on 1.8, 1.9, 1.10
            // Does not work 1.11+ (minecraft ignores the glitched enchantment)
            if (item != null && item.getType() != Material.AIR && cb_CraftItemStack_asCraftMirror != null) {
                try {
                    Object nmsStack = cb_CraftItemStack_asNMSCopy.invoke(null, item);
                    Object tag = mc_ItemStack_getTag.invoke(nmsStack);
                    if (tag == null) {
                        tag = mc_NBTTagCompound.newInstance();
                    }
                    // set to have a fake enchantment
                    Object enchantmentList = mc_NBTTagList.newInstance();
                    mc_NBTTagCompound_set.invoke(tag, "ench", enchantmentList);
                    mc_ItemStack_setTag.invoke(nmsStack, tag);
                    item = (ItemStack) cb_CraftItemStack_asCraftMirror.invoke(null, nmsStack);
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "Failed to set glow enchantment on item: " + item, ex);
                }
            }
        }
        return item;
    }

    /**
     * Remove all enchantments, including hidden enchantments
     *
     * @param item item to clear enchants from
     * @return copy of the item without any enchantment tag
     */
    public static ItemStack removeGlow(ItemStack item) {
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
            item.removeEnchantment(Enchantment.DURABILITY);
            return item;
        } else {
            if (item != null && item.getType() != Material.AIR && cb_CraftItemStack_asCraftMirror != null) {
                try {
                    Object nmsStack = cb_CraftItemStack_asNMSCopy.invoke(null, item);
                    Object tag = mc_ItemStack_getTag.invoke(nmsStack);
                    if (tag != null) {
                        // remove enchantment list
                        mc_NBTTagCompound_remove.invoke(tag, "ench");
                        mc_ItemStack_setTag.invoke(nmsStack, tag);
                        item = (ItemStack) cb_CraftItemStack_asCraftMirror.invoke(null, nmsStack);
                    }
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "Failed to set glow enchantment on item: " + item, ex);
                }
            }
        }
        return item;
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
