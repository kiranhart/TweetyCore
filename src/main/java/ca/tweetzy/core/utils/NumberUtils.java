package ca.tweetzy.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/15/2020
 * Time Created: 11:11 AM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public class NumberUtils {

    /**
     * Checks if the provided string is an integer
     *
     * @param s is the string to be checked
     * @return whether the string is a valid integer
     */
    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the provided string is a double
     *
     * @param s is the string to be checked
     * @return whether the string is a valid double
     */
    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
