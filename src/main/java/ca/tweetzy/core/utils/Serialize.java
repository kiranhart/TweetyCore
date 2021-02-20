package ca.tweetzy.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/15/2020
 * Time Created: 11:13 AM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public class Serialize {

    /**
     * A method to serialize an {@link ItemStack} list to Base64 String.
     *
     * @param items to turn into a Base64 String.
     * @return Base64 string of the items.
     */
    public static String toBase64(List<ItemStack> items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(items.size());

            // Save every element in the list
            for (ItemStack item : items)
                dataOutput.writeObject(item);

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Gets a list of ItemStacks from Base64 string.
     *
     * @param data Base64 string to convert to ItemStack list.
     * @return ItemStack array created from the Base64 string.
     */
    public static List<ItemStack> fromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            int length = dataInput.readInt();
            List<ItemStack> items = new ArrayList<>();

            // Read the serialized itemstack list
            for (int i = 0; i < length; i++)
                items.add((ItemStack) dataInput.readObject());

            dataInput.close();
            return items;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String location(Location location) {
        String world = Objects.requireNonNull(location.getWorld()).getName();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        return world + ";" + x + ";" + y + ";" + z;
    }

    public static Location deserializeLocation(String s) {
        String[] x = s.split(";");
        return new Location(Bukkit.getWorld(x[0]), Double.parseDouble(x[1]), Double.parseDouble(x[2]), Double.parseDouble(x[3]));
    }

    public static String potionEffect(PotionEffect potionEffect) {
        String name = potionEffect.getType().getName();
        int amplifier = potionEffect.getAmplifier();
        return name + ";" + amplifier;
    }

    public static List<String> potionEffects(List<PotionEffect> effects) {
        List<String> serialized = new ArrayList<>();
        effects.forEach(line -> serialized.add(potionEffect(line)));
        return serialized;
    }

    public static PotionEffect deserializePotionEffect(String s) {
        String[] x = s.split(";");
        return new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(x[0].toUpperCase())), Integer.MAX_VALUE, Integer.parseInt(x[1]), false, false);
    }
}
