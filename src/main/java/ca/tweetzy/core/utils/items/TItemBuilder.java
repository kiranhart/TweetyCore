package ca.tweetzy.core.utils.items;

import ca.tweetzy.core.utils.TextUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/13/2020
 * Time Created: 7:31 PM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public class TItemBuilder {

    private ItemStack itemStack;

    public TItemBuilder(Material material) {
        this(material, 1);
    }

    public TItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public TItemBuilder(Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
    }

    public TItemBuilder clone() {
        return new TItemBuilder(itemStack);
    }

    public TItemBuilder setName(String name) {
        ItemMeta im = itemStack.getItemMeta();
        im.setDisplayName(TextUtils.formatText(name));
        itemStack.setItemMeta(im);
        return this;
    }

    public TItemBuilder addUnsafeEnchantment(Enchantment ench, int level) {
        itemStack.addUnsafeEnchantment(ench, level);
        return this;
    }

    public TItemBuilder addEnchant(Enchantment ench, int level) {
        ItemMeta im = itemStack.getItemMeta();
        im.addEnchant(ench, level, true);
        itemStack.setItemMeta(im);
        return this;
    }

    public TItemBuilder addEnchantments(Map<Enchantment, Integer> enchantments) {
        itemStack.addEnchantments(enchantments);
        return this;
    }

    public TItemBuilder setLore(String... lore) {
        ItemMeta im = itemStack.getItemMeta();
        im.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(im);
        return this;
    }

    public TItemBuilder setLore(List<String> lore) {
        ItemMeta im = itemStack.getItemMeta();
        im.setLore(lore.stream().map(e -> ChatColor.translateAlternateColorCodes('&', e)).collect(Collectors.toList()));
        itemStack.setItemMeta(im);
        return this;
    }

    public ItemStack toItemStack() {
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
