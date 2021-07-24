package ca.tweetzy.core.hooks.economies;

import ca.tweetzy.core.hooks.Hook;
import org.bukkit.OfflinePlayer;

/**
 * The current file has been created by Kiran Hart
 * Date Created: July 24 2021
 * Time Created: 3:18 a.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public abstract class Economy implements Hook {

    /**
     * Get the players available balance
     *
     * @param player player
     * @return the amount of available balance
     */
    public abstract double getBalance(OfflinePlayer player);

    /**
     * Check to see if a player has at least some balance available
     *
     * @param player player to check
     * @param cost   minimum amount this player should have
     * @return true if this player can have this amount withdrawn
     */
    public abstract boolean hasBalance(OfflinePlayer player, double cost);

    /**
     * Try to withdraw an amount from a player's balance
     *
     * @param player player to check
     * @param cost   amount to remove from this player
     * @return true if the total amount was withdrawn successfully
     */
    public abstract boolean withdrawBalance(OfflinePlayer player, double cost);

    /**
     * Try to add an amount to a player's balance
     *
     * @param player player to check
     * @param amount amount to add to this player
     * @return true if the total amount was added successfully
     */
    public abstract boolean deposit(OfflinePlayer player, double amount);
}
