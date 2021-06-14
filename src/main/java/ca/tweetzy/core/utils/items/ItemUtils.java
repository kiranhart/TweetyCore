/**
 * This class uses some Minecraft code and also Paper API
 */
package ca.tweetzy.core.utils.items;

import ca.tweetzy.core.compatibility.ClassMapping;
import ca.tweetzy.core.compatibility.ServerVersion;
import ca.tweetzy.core.compatibility.XMaterial;
import ca.tweetzy.core.utils.nms.NMSUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class ItemUtils {

    static boolean can_getI18NDisplayName = true;

    static {
        try {
            ItemStack.class.getMethod("getI18NDisplayName");
        } catch (NoSuchMethodException | SecurityException ex) {
            can_getI18NDisplayName = false;
        }
    }

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

    private static Method methodAsBukkitCopy, methodAsNMSCopy, methodA;

    static {
        try {
            Class<?> clazzEnchantmentManager = ClassMapping.ENCHANTMENT_MANAGER.getClazz();
            Class<?> clazzItemStack = ClassMapping.ITEM_STACK.getClazz();
            Class<?> clazzCraftItemStack = ClassMapping.CRAFT_ITEM_STACK.getClazz();

            methodAsBukkitCopy = clazzCraftItemStack.getMethod("asBukkitCopy", clazzItemStack);
            methodAsNMSCopy = clazzCraftItemStack.getMethod("asNMSCopy", ItemStack.class);

            if (ServerVersion.isServerVersion(ServerVersion.V1_8))
                methodA = clazzEnchantmentManager.getMethod("a", Random.class, clazzItemStack, int.class);
            else
                methodA = clazzEnchantmentManager.getMethod("a", Random.class, clazzItemStack, int.class, boolean.class);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static ItemStack applyRandomEnchants(ItemStack item, int level) {
        try {
            Object nmsItemStack = methodAsNMSCopy.invoke(null, item);

            if (ServerVersion.isServerVersion(ServerVersion.V1_8))
                nmsItemStack = methodA.invoke(null, new Random(), nmsItemStack, level);
            else
                nmsItemStack = methodA.invoke(null, new Random(), nmsItemStack, level, false);

            item = (ItemStack) methodAsBukkitCopy.invoke(null, nmsItemStack);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return item;
    }

    public static String itemStackArrayToBase64(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) dataOutput.writeObject(item);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ItemStack[] itemStackArrayFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];
            for (int i = 0; i < items.length; i++)
                items[i] = (ItemStack) dataInput.readObject();
            dataInput.close();
            return items;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Clone of org.bukkit.inventory.ItemStack.asQuantity, since it is a paper-only function
     *
     * @param item item to copy
     * @param qty  amount the new ItemStack should have
     * @return a copy of the original item
     */
    public static ItemStack getAsCopy(ItemStack item, int qty) {
        ItemStack clone = item.clone();
        clone.setAmount(qty);
        return clone;
    }

    public static boolean hasEnoughDurability(ItemStack tool, int requiredAmount) {
        if (tool.getType().getMaxDurability() <= 1)
            return true;
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
            if (!tool.hasItemMeta() || !(tool.getItemMeta() instanceof Damageable))
                return true;

            Damageable damageable = (Damageable) tool.getItemMeta();
            int durabilityRemaining = tool.getType().getMaxDurability() - damageable.getDamage();
            return durabilityRemaining > requiredAmount;
        } else {
            return tool.getDurability() + requiredAmount <= tool.getType().getMaxDurability();
        }
    }

    static Class cb_ItemStack = NMSUtils.getCraftClass("inventory.CraftItemStack");
    static Class mc_ItemStack = ClassMapping.ITEM_STACK.getClazz();
    static Class mc_NBTTagCompound = ClassMapping.NBT_TAG_COMPOUND.getClazz();
    static Class mc_NBTTagList = ClassMapping.NBT_TAG_LIST.getClazz();
    static Class mc_NBTBase = ClassMapping.NBT_BASE.getClazz();
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
                    /*
                    if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
                        // Servers from 1.13 and up change the id to a string
                        Object fakeEnchantment = mc_NBTTagCompound.newInstance();
                        mc_NBTTagCompound_setString.invoke(fakeEnchantment, "id", "glow:glow");
                        mc_NBTTagCompound_setShort.invoke(fakeEnchantment, "lvl", (short) 0);
                        mc_NBTTagList_add.invoke(enchantmentList, fakeEnchantment);
                    } else if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
                        // Servers from 1.11 and up require *something* in the enchantment field
                        Object fakeEnchantment = mc_NBTTagCompound.newInstance();
                        mc_NBTTagCompound_setShort.invoke(fakeEnchantment, "id", (short) 245);
                        mc_NBTTagCompound_setShort.invoke(fakeEnchantment, "lvl", (short) 1);
                        mc_NBTTagList_add.invoke(enchantmentList, fakeEnchantment);
                    }//*/
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

    private static Class<?> mc_Item = ClassMapping.ITEM.getClazz();
    private static Method mc_Item_getItem;
    private static Field mc_Item_maxStackSize;

    static {
        if (mc_ItemStack != null) {
            try {
                mc_Item_getItem = mc_ItemStack.getDeclaredMethod("getItem");
                mc_Item_maxStackSize = mc_Item.getDeclaredField("maxStackSize");
                mc_Item_maxStackSize.setAccessible(true);
            } catch (Exception ex) {
            }
        }
    }

    public static ItemStack setMaxStack(ItemStack item, int max) {
        if (item != null && mc_Item_maxStackSize != null) {
            try {
                Object objItemStack = mc_Item_getItem.invoke(cb_CraftItemStack_asNMSCopy.invoke(null, item));
                mc_Item_maxStackSize.set(objItemStack, max);
            } catch (ReflectiveOperationException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to set max stack size on item " + item, e);
            }
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
        if (ServerVersion.isServerVersionBelow(ServerVersion.V1_8) || head == null || head.getType() != XMaterial.PLAYER_HEAD.parseMaterial()) {
            return;
        }
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
            meta.setOwningPlayer(player);
        } else {
            meta.setOwner(player.getName());
        }
    }

    public static ItemStack getCustomHead(String texture) {
        return getCustomHead(null, texture);
    }

    public static ItemStack getCustomHead(String signature, String texture) {
        ItemStack skullItem = XMaterial.PLAYER_HEAD.parseItem();
        if (ServerVersion.isServerVersionBelow(ServerVersion.V1_8))
            return skullItem;

        SkullMeta sm = (SkullMeta) skullItem.getItemMeta();
        GameProfile gm;
        if (texture.endsWith("=")) {
            gm = new GameProfile(UUID.nameUUIDFromBytes(texture.getBytes()), "CustomHead");
            if (signature == null)
                gm.getProperties().put("textures", new Property("texture", texture.replaceAll("=", "")));
            else
                gm.getProperties().put("textures", new Property("textures", texture, signature));
        } else {
            gm = new GameProfile(UUID.nameUUIDFromBytes(texture.getBytes()), "CustomHead");
            byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"http://textures.minecraft.net/texture/%s\"}}}", texture).getBytes());
            gm.getProperties().put("textures", new Property("textures", new String(encodedData)));
        }

        try {
            Field profileField;
            profileField = sm.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(sm, gm);
            skullItem.setItemMeta(sm);
            return skullItem;
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException ex) {
            throw new RuntimeException("Reflection error while setting head texture", ex);
        }
    }

    static Class cb_CraftPlayer = NMSUtils.getCraftClass("entity.CraftPlayer");
    static Method cb_CraftPlayer_getProfile;

    static {
        try {
            cb_CraftPlayer_getProfile = cb_CraftPlayer.getMethod("getProfile");
        } catch (Exception ex) {
        }
    }

    public static String getSkullTexture(Player player) {
        if (player == null || ServerVersion.isServerVersionBelow(ServerVersion.V1_8)) {
            return null;
        }
        try {

            Object craftPlayer = cb_CraftPlayer.cast(player);

            Iterator<Property> iterator = ((GameProfile) cb_CraftPlayer_getProfile.invoke(craftPlayer)).getProperties().get("textures").iterator();

            return iterator.hasNext() ? iterator.next().getValue() : null;

        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getSkullTexture(ItemStack item) {
        if (item.getType() != XMaterial.PLAYER_HEAD.parseMaterial() || ServerVersion.isServerVersionBelow(ServerVersion.V1_8)) {
            return null;
        }
        try {
            SkullMeta localSkullMeta = (SkullMeta) item.getItemMeta();
            Field cb_SkullMeta_profile = localSkullMeta.getClass().getDeclaredField("profile");
            if (cb_SkullMeta_profile == null) return null;
            cb_SkullMeta_profile.setAccessible(true);

            GameProfile profile = (GameProfile) cb_SkullMeta_profile.get(localSkullMeta);
            Iterator<Property> iterator = profile.getProperties().get("textures").iterator();

            return iterator.hasNext() ? iterator.next().getValue() : null;
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
        }
        return null;
    }

    public static String getDecodedTexture(String encoded) {
        return encoded != null ? StringUtils.substringBetween(new String(Base64.getDecoder().decode(encoded)), "texture/", "\"") : null;
    }


    /**
     * Quickly check to see if the two items use the same material. <br />
     * NOTE: Does not check meta data; only checks the item material.
     *
     * @param is1 first item to compare
     * @param is2 item to compare against
     * @return true if both items are of the same material
     */
    public static boolean isSimilarMaterial(ItemStack is1, ItemStack is2) {
        XMaterial mat1 = XMaterial.matchXMaterial(is1);
        return mat1 != null && mat1 == XMaterial.matchXMaterial(is2);
    }

    /**
     * Check to see if this item can be moved into a single slot in this
     * inventory. <br>
     * This returns true if there is a free slot or a slot with a matching item
     * where adding this item's amount to that item's amount will not violate
     * the maximum stack size for that item.
     *
     * @param inventory inventory to check
     * @param item      item to check against
     * @return true if a free slot or single receiver slot is available
     */
    public static boolean canMove(Inventory inventory, ItemStack item) {
        if (inventory.firstEmpty() != -1) {
            return true;
        }

        final ItemMeta itemMeta = item.getItemMeta();
        for (ItemStack stack : inventory) {
            final ItemMeta stackMeta;
            if (isSimilarMaterial(stack, item) && (stack.getAmount() + item.getAmount()) < stack.getMaxStackSize()
                    && ((itemMeta == null) == ((stackMeta = stack.getItemMeta()) == null))
                    && (itemMeta == null || Bukkit.getItemFactory().equals(itemMeta, stackMeta))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check to see if this item can be moved into a single slot in this
     * inventory. <br>
     * This returns true if there is a free slot or a slot with a matching item
     * where adding this item's amount to that item's amount will not violate
     * the maximum stack size for that item.
     *
     * @param contents inventory to check
     * @param item     item to check against
     * @return true if a free slot or single receiver slot is available
     */
    public static boolean canMove(ItemStack[] contents, ItemStack item) {
        final ItemMeta itemMeta = item.getItemMeta();
        for (int i = 0; i < contents.length; i++) {
            final ItemStack stack = contents[i];
            if (stack == null || stack.getAmount() == 0) {
                return true;
            }
            final ItemMeta stackMeta;
            if (isSimilarMaterial(stack, item) && (stack.getAmount() + item.getAmount()) < stack.getMaxStackSize()
                    && ((itemMeta == null) == ((stackMeta = stack.getItemMeta()) == null))
                    && (itemMeta == null || Bukkit.getItemFactory().equals(itemMeta, stackMeta))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check to see if this item can be moved into a single slot in this
     * inventory while also reserving one of the slots.<br>
     * This returns true if there is a free slot or a slot with a matching item
     * where adding this item's amount to that item's amount will not violate
     * the maximum stack size for that item.
     *
     * @param inventory inventory to check
     * @param item      item to check against
     * @param reserved  which slot should be reserved
     * @return true if a free slot or single receiver slot is available
     */
    public static boolean canMoveReserved(Inventory inventory, ItemStack item, int reserved) {
        final ItemMeta itemMeta = item.getItemMeta();
        final ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (i == reserved) {
                continue;
            }
            final ItemStack stack = contents[i];
            final ItemMeta stackMeta;
            if (stack == null || stack.getAmount() == 0
                    || (isSimilarMaterial(stack, item) && (stack.getAmount() + item.getAmount()) < stack.getMaxStackSize()
                    && ((itemMeta == null) == ((stackMeta = stack.getItemMeta()) == null))
                    && (itemMeta == null || Bukkit.getItemFactory().equals(itemMeta, stackMeta)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check to see if this item can be moved into a single slot in this
     * inventory while also reserving one of the slots.<br>
     * This returns true if there is a free slot or a slot with a matching item
     * where adding this item's amount to that item's amount will not violate
     * the maximum stack size for that item.
     *
     * @param contents inventory to check
     * @param item     item to check against
     * @param reserved which slot should be reserved
     * @return true if a free slot or single receiver slot is available
     */
    public static boolean canMoveReserved(ItemStack[] contents, ItemStack item, int reserved) {
        final ItemMeta itemMeta = item.getItemMeta();
        for (int i = 0; i < contents.length; i++) {
            if (i == reserved) {
                continue;
            }
            final ItemStack stack = contents[i];
            if (stack == null || stack.getAmount() == 0) {
                return true;
            }
            final ItemMeta stackMeta;
            if (isSimilarMaterial(stack, item) && (stack.getAmount() + item.getAmount()) < stack.getMaxStackSize()
                    && ((itemMeta == null) == ((stackMeta = stack.getItemMeta()) == null))
                    && (itemMeta == null || Bukkit.getItemFactory().equals(itemMeta, stackMeta))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add up to a number of items to this inventory.
     *
     * @param item        item to add
     * @param amountToAdd how many of this item to attempt to add
     * @param inventory   a list that represents the inventory
     * @param maxSize     maximum number of different items this container can hold
     * @return how many items were added
     */
    public static int addAny(ItemStack item, int amountToAdd, List<ItemStack> inventory, int maxSize) {
        return addAny(item, amountToAdd, inventory, maxSize, -1);
    }

    /**
     * Add up to a number of items to this inventory.
     *
     * @param item        item to add
     * @param amountToAdd how many of this item to attempt to add
     * @param inventory   a list that represents the inventory
     * @param maxSize     maximum number of different items this container can hold
     * @param reserved    slot to reserve - will not fill this slot
     * @return how many items were added
     */
    public static int addAny(ItemStack item, int amountToAdd, List<ItemStack> inventory, int maxSize, int reserved) {
        int totalAdded = 0;
        if (inventory != null && item != null && amountToAdd > 0) {
            final int maxStack = item.getMaxStackSize();
            for (int i = 0; amountToAdd > 0 && i < maxSize; i++) {
                if (i == reserved) {
                    continue;
                }
                final ItemStack cacheItem = i >= inventory.size() ? null : inventory.get(i);
                if (cacheItem == null || cacheItem.getAmount() == 0) {
                    // free slot!
                    int toAdd = Math.min(maxStack, amountToAdd);
                    ItemStack item2 = item.clone();
                    item2.setAmount(toAdd);
                    if (i >= inventory.size()) {
                        inventory.add(item2);
                    } else {
                        inventory.set(i, item2);
                    }
                    totalAdded += toAdd;
                    amountToAdd -= toAdd;
                } else if (maxStack > cacheItem.getAmount() && item.isSimilar(cacheItem)) {
                    // free space!
                    int toAdd = Math.min(maxStack - cacheItem.getAmount(), amountToAdd);
                    inventory.get(i).setAmount(toAdd + cacheItem.getAmount());
                    totalAdded += toAdd;
                    amountToAdd -= toAdd;
                }
            }
        }
        return totalAdded;
    }

    /**
     * Add an item to this inventory, but only if it can be added completely.
     *
     * @param item          item to add
     * @param inventory     a list that represents the inventory
     * @param containerSize maximum number of different items this container can
     *                      hold
     * @return true if the item was added
     */
    public static boolean addItem(ItemStack item, List<ItemStack> inventory, int containerSize) {
        if (inventory == null || item == null || item.getAmount() <= 0 || containerSize <= 0) {
            return false;
        }
        return addItem(item, item.getAmount(), inventory, containerSize);
    }

    /**
     * Add an item to this inventory, but only if it can be added completely.
     *
     * @param item          item to add
     * @param inventory     a list that represents the inventory
     * @param containerSize maximum number of different items this container can
     *                      hold
     * @param reserved      slot to reserve - will not fill this slot
     * @return true if the item was added
     */
    public static boolean addItem(ItemStack item, List<ItemStack> inventory, int containerSize, int reserved) {
        if (inventory == null || item == null || item.getAmount() <= 0 || containerSize <= 0) {
            return false;
        }
        return addItem(item, item.getAmount(), inventory, containerSize, reserved);
    }

    /**
     * Add an item to this inventory.
     *
     * @param item          item to add
     * @param amount        how many of this item should be added
     * @param inventory     a list that represents the inventory
     * @param containerSize maximum number of different items this container can
     * @param reserved      slot to reserve - will not fill this slot hold
     * @return true if the item was added
     */
    public static boolean addItem(ItemStack item, int amount, List<ItemStack> inventory, int containerSize, int reserved) {
        return addItem(item, amount, inventory, containerSize, reserved, null);
    }

    /**
     * Add an item to this inventory, but only if it can be added completely.
     *
     * @param item            item to add
     * @param amount          how many of this item should be added
     * @param inventory       a list that represents the inventory
     * @param containerSize   maximum number of different items this container can
     *                        hold
     * @param reserved        slot to reserve - will not fill this slot
     * @param inventorySource Material of the container
     * @return true if the item was added
     */
    public static boolean addItem(ItemStack item, int amount, List<ItemStack> inventory, int containerSize, int reserved, Material inventorySource) {
        if (inventory == null || item == null || amount <= 0 || inventorySource == null) {
            return false;
        }
        boolean[] check = null;

        if (inventorySource != null && inventorySource != Material.AIR) {
            // Don't transfer shulker boxes into other shulker boxes, that's a bad idea.
            if (inventorySource.name().contains("SHULKER_BOX") && item.getType().name().contains("SHULKER_BOX")) {
                return false;
            }

            // some destination containers have special conditions
            switch (inventorySource.name()) {
                case "BREWING_STAND": {

                    // first compile a list of what slots to check
                    check = new boolean[5];
                    String typeStr = item.getType().name().toUpperCase();
                    if (typeStr.contains("POTION") || typeStr.contains("BOTTLE")) {
                        // potion bottles are the first three slots
                        check[0] = check[1] = check[2] = true;
                    }
                    // fuel in 5th position, input in 4th
                    if (item.getType() == Material.BLAZE_POWDER) {
                        check[4] = true;
                    } else {
                        check[3] = true;
                    }

                }
                case "SMOKER":
                case "BLAST_FURNACE":
                case "BURNING_FURNACE":
                case "FURNACE": {

                    check = new boolean[3];

                    boolean isFuel = !item.getType().name().contains("LOG") && XMaterial.matchXMaterial(item.getType()).isFuel();
                    // fuel is 2nd slot, input is first
                    if (isFuel) {

                        check[1] = true;
                    } else {
                        check[0] = true;
                    }

                }
            }
        }

        // grab the amount to move and the max item stack size
        int toAdd = item.getAmount();
        final int maxStack = item.getMaxStackSize();

        // we can reduce calls to ItemStack.isSimilar() by caching what cells to look at
        if (check == null) {
            check = new boolean[containerSize];
            for (int i = 0; toAdd > 0 && i < check.length; i++) {
                check[i] = true;
            }
        }
        if (reserved >= 0 && check.length < reserved) {
            check[reserved] = false;
        }

        // first verify that we can add this item
        for (int i = 0; toAdd > 0 && i < containerSize; i++) {
            if (check[i]) {
                final ItemStack cacheItem = i >= inventory.size() ? null : inventory.get(i);
                if (cacheItem == null || cacheItem.getAmount() == 0) {
                    // free slot!
                    toAdd -= Math.min(maxStack, toAdd);
                    check[i] = true;
                } else if (maxStack > cacheItem.getAmount() && item.isSimilar(cacheItem)) {
                    // free space!
                    toAdd -= Math.min(maxStack - cacheItem.getAmount(), toAdd);
                    check[i] = true;
                } else {
                    check[i] = false;
                }
            }
        }
        if (toAdd <= 0) {
            // all good to add!
            toAdd = item.getAmount();
            for (int i = 0; toAdd > 0 && i < containerSize; i++) {
                if (!check[i]) {
                    continue;
                }
                final ItemStack cacheItem = i >= inventory.size() ? null : inventory.get(i);
                if (cacheItem == null || cacheItem.getAmount() == 0) {
                    // free slot!
                    int adding = Math.min(maxStack, toAdd);
                    ItemStack item2 = item.clone();
                    item2.setAmount(adding);
                    if (i >= inventory.size()) {
                        inventory.add(item2);
                    } else {
                        inventory.set(i, item2);
                    }
                    toAdd -= adding;
                } else if (maxStack > cacheItem.getAmount()) {
                    // free space!
                    // (no need to check item.isSimilar(cacheItem), since we have that cached in check[])
                    int adding = Math.min(maxStack - cacheItem.getAmount(), toAdd);
                    inventory.get(i).setAmount(adding + cacheItem.getAmount());
                    toAdd -= adding;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Add up to a number of items to this inventory.
     *
     * @param item        item to add
     * @param amountToAdd how many of this item to attempt to add
     * @param inventory   a list that represents the inventory
     * @return how many items were added
     */
    public static int addAny(ItemStack item, int amountToAdd, Inventory inventory) {
        int totalAdded = 0;
        if (inventory != null && item != null && amountToAdd > 0) {
            final int containerSize = inventory.getSize();
            final int maxStack = item.getMaxStackSize();
            for (int i = 0; amountToAdd > 0 && i < containerSize; i++) {
                final ItemStack cacheItem = inventory.getItem(i);
                if (cacheItem == null || cacheItem.getAmount() == 0) {
                    // free slot!
                    int toAdd = Math.min(maxStack, amountToAdd);
                    ItemStack item2 = item.clone();
                    item2.setAmount(toAdd);
                    inventory.setItem(i, item2);
                    totalAdded += toAdd;
                    amountToAdd -= toAdd;
                } else if (maxStack > cacheItem.getAmount() && item.isSimilar(cacheItem)) {
                    // free space!
                    int toAdd = Math.min(maxStack - cacheItem.getAmount(), amountToAdd);
                    cacheItem.setAmount(toAdd + cacheItem.getAmount());
                    totalAdded += toAdd;
                    amountToAdd -= toAdd;
                }
            }
        }
        return totalAdded;
    }

    /**
     * Add an item to this inventory, but only if it can be added completely.
     *
     * @param item      item to add
     * @param inventory a list that represents the inventory hold
     * @return true if the item was added
     */
    public static boolean addItem(ItemStack item, Inventory inventory) {
        if (inventory == null || item == null || item.getAmount() <= 0) {
            return false;
        }
        return addItem(item, item.getAmount(), inventory, -1, null);
    }

    /**
     * Add an item to this inventory.
     *
     * @param item      item to add
     * @param amount    how many of this item should be added
     * @param inventory a list that represents the inventory
     * @param reserved  slot to reserve - will not fill this slot
     * @return true if the item was added
     */
    public static boolean addItem(ItemStack item, int amount, Inventory inventory, int reserved) {
        return addItem(item, amount, inventory, reserved, null);
    }

    /**
     * Add an item to this inventory, but only if it can be added completely.
     *
     * @param item            item to add
     * @param amount          how many of this item should be added
     * @param inventory       a list that represents the inventory
     * @param reserved        slot to reserve - will not fill this slot
     * @param inventorySource Material of the container
     * @return true if the item was added
     */
    public static boolean addItem(ItemStack item, int amount, Inventory inventory, int reserved, Material inventorySource) {
        if (inventory == null || item == null || amount <= 0 || inventorySource == null) {
            return false;
        }
        boolean[] check = null;

        if (inventorySource != null && inventorySource != Material.AIR) {

            // Don't transfer shulker boxes into other shulker boxes, that's a bad idea.
            if (inventorySource.name().contains("SHULKER_BOX") && item.getType().name().contains("SHULKER_BOX")) {
                return false;
            }

            // some destination containers have special conditions
            switch (inventorySource.name()) {
                case "BREWING_STAND": {

                    // first compile a list of what slots to check
                    check = new boolean[5];
                    String typeStr = item.getType().name().toUpperCase();
                    if (typeStr.contains("POTION") || typeStr.contains("BOTTLE")) {
                        // potion bottles are the first three slots
                        check[0] = check[1] = check[2] = true;
                    }
                    // fuel in 5th position, input in 4th
                    if (item.getType() == Material.BLAZE_POWDER) {
                        check[4] = true;
                    } else {
                        check[3] = true;
                    }

                }
                case "SMOKER":
                case "BLAST_FURNACE":
                case "BURNING_FURNACE":
                case "FURNACE": {

                    check = new boolean[3];

                    boolean isFuel = !item.getType().name().contains("LOG") && XMaterial.matchXMaterial(item.getType()).isFuel();
                    // fuel is 2nd slot, input is first
                    if (isFuel) {

                        check[1] = true;
                    } else {
                        check[0] = true;
                    }

                }
            }
        }
        // grab the amount to move and the max item stack size
        int toAdd = item.getAmount();
        final int maxStack = item.getMaxStackSize();
        final int containerSize = inventory.getSize();

        // we can reduce calls to ItemStack.isSimilar() by caching what cells to look at
        if (check == null) {
            check = new boolean[containerSize];
            for (int i = 0; toAdd > 0 && i < check.length; i++) {
                check[i] = true;
            }
        }

        // first verify that we can add this item
        for (int i = 0; toAdd > 0 && i < containerSize; i++) {
            if (check[i]) {
                final ItemStack cacheItem = inventory.getItem(i);
                if (cacheItem == null || cacheItem.getAmount() == 0) {
                    // free slot!
                    toAdd -= Math.min(maxStack, toAdd);
                    check[i] = true;
                } else if (maxStack > cacheItem.getAmount() && item.isSimilar(cacheItem)) {
                    // free space!
                    toAdd -= Math.min(maxStack - cacheItem.getAmount(), toAdd);
                    check[i] = true;
                } else {
                    check[i] = false;
                }
            }
        }
        if (toAdd <= 0) {
            // all good to add!
            toAdd = item.getAmount();
            for (int i = 0; toAdd > 0 && i < containerSize; i++) {
                if (!check[i]) {
                    continue;
                }
                final ItemStack cacheItem = inventory.getItem(i);
                if (cacheItem == null || cacheItem.getAmount() == 0) {
                    // free slot!
                    int adding = Math.min(maxStack, toAdd);
                    ItemStack item2 = item.clone();
                    item2.setAmount(adding);
                    inventory.setItem(i, item2);
                    toAdd -= adding;
                } else if (maxStack > cacheItem.getAmount()) {
                    // free space!
                    // (no need to check item.isSimilar(cacheItem), since we have that cached in check[])
                    int adding = Math.min(maxStack - cacheItem.getAmount(), toAdd);
                    cacheItem.setAmount(adding + cacheItem.getAmount());
                    toAdd -= adding;
                }
            }
            return true;
        }
        return false;
    }

    public static XMaterial getDyeColor(char color) {
        switch (color) {
            case '0':
                return XMaterial.BLACK_DYE;
            case '1':
                return XMaterial.BLUE_DYE;
            case '2':
            case '8':
                return XMaterial.GREEN_DYE;
            case '3':
                return XMaterial.CYAN_DYE;
            case '4':
                return XMaterial.BROWN_DYE;
            case '5':
                return XMaterial.PURPLE_DYE;
            case '6':
                return XMaterial.ORANGE_DYE;
            case '7':
                return XMaterial.LIGHT_GRAY_DYE;
            case 'a':
                return XMaterial.LIME_DYE;
            case 'b':
                return XMaterial.LIGHT_BLUE_DYE;
            case 'c':
                return XMaterial.RED_DYE;
            case 'd':
                return XMaterial.MAGENTA_DYE;
            case 'e':
                return XMaterial.YELLOW_DYE;
            case 'f':
                return XMaterial.WHITE_DYE;
        }
        return XMaterial.STONE;
    }

    /**
     * Add an item to this inventory.
     *
     * @param item          item to add
     * @param amount        how many of this item should be added
     * @param inventory     a list that represents the inventory
     * @param containerSize maximum number of different items this container can
     *                      hold
     * @return true if the item was added
     */
    public static boolean addItem(ItemStack item, int amount, List<ItemStack> inventory, int containerSize) {
        if (inventory == null || item == null || amount <= 0 || containerSize <= 0) {
            return false;
        }

        // grab the amount to move and the max item stack size
        int toAdd = amount;
        final int maxStack = item.getMaxStackSize();
        boolean[] check = null;

        // we can reduce calls to ItemStack.isSimilar() by caching what cells to look at
        if (check == null) {
            check = new boolean[containerSize];
            for (int i = 0; toAdd > 0 && i < check.length; i++) {
                check[i] = true;
            }
        }

        // first verify that we can add this item
        for (int i = 0; toAdd > 0 && i < containerSize; i++) {
            if (check[i]) {
                final ItemStack cacheItem = i >= inventory.size() ? null : inventory.get(i);
                if (cacheItem == null || cacheItem.getAmount() == 0) {
                    // free slot!
                    toAdd -= Math.min(maxStack, toAdd);
                    check[i] = true;
                } else if (maxStack > cacheItem.getAmount() && item.isSimilar(cacheItem)) {
                    // free space!
                    toAdd -= Math.min(maxStack - cacheItem.getAmount(), toAdd);
                    check[i] = true;
                } else {
                    check[i] = false;
                }
            }
        }

        if (toAdd <= 0) {
            // all good to add!
            toAdd = item.getAmount();
            for (int i = 0; toAdd > 0 && i < containerSize; i++) {
                if (!check[i]) {
                    continue;
                }
                final ItemStack cacheItem = i >= inventory.size() ? null : inventory.get(i);
                if (cacheItem == null || cacheItem.getAmount() == 0) {
                    // free slot!
                    int adding = Math.min(maxStack, toAdd);
                    ItemStack item2 = item.clone();
                    item2.setAmount(adding);
                    if (i >= inventory.size()) {
                        inventory.add(item2);
                    } else {
                        inventory.set(i, item2);
                    }
                    toAdd -= adding;
                } else if (maxStack > cacheItem.getAmount()) {
                    // free space!
                    // (no need to check item.isSimilar(cacheItem), since we have that cached in check[])
                    int adding = Math.min(maxStack - cacheItem.getAmount(), toAdd);
                    inventory.get(i).setAmount(adding + cacheItem.getAmount());
                    toAdd -= adding;
                }
            }
            return true;
        }
        return false;
    }
}