package ca.tweetzy.core.hooks.economies;

import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 * The current file has been created by Kiran Hart
 * Date Created: July 24 2021
 * Time Created: 3:19 a.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class PlayerPointsEconomy extends Economy {

    private final PlayerPoints playerPoints;

    public PlayerPointsEconomy() {
        this.playerPoints = (PlayerPoints) Bukkit.getServer().getPluginManager().getPlugin("PlayerPoints");
    }

    private int convertAmount(double amount) {
        return (int) Math.ceil(amount);
    }

    @Override
    public boolean isEnabled() {
        return playerPoints.isEnabled();
    }

    @Override
    public String getName() {
        return "PlayerPoints";
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return playerPoints.getAPI().look(player.getUniqueId());
    }

    @Override
    public boolean hasBalance(OfflinePlayer player, double cost) {
        int amount = convertAmount(cost);
        return playerPoints.getAPI().look(player.getUniqueId()) >= amount;
    }

    @Override
    public boolean withdrawBalance(OfflinePlayer player, double cost) {
        int amount = convertAmount(cost);
        return playerPoints.getAPI().take(player.getUniqueId(), amount);
    }

    @Override
    public boolean deposit(OfflinePlayer player, double amount) {
        int amt = convertAmount(amount);
        return playerPoints.getAPI().give(player.getUniqueId(), amt);
    }
}
