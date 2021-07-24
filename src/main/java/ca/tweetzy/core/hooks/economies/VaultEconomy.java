package ca.tweetzy.core.hooks.economies;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * The current file has been created by Kiran Hart
 * Date Created: July 24 2021
 * Time Created: 3:20 a.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class VaultEconomy extends Economy {

    private final net.milkbowl.vault.economy.Economy vault;

    public VaultEconomy() {
        // this returns null if we have Vault with no compatible eco plugin
        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> v = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (v != null) {
            this.vault = v.getProvider();
        } else {
            // whoopsie!
            this.vault = null;
        }
    }

    @Override
    public boolean isEnabled() {
        return vault != null;
    }

    @Override
    public String getName() {
        return "Vault";
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        if (vault == null)
            return 0;
        return vault.getBalance(player);
    }

    @Override
    public boolean hasBalance(OfflinePlayer player, double cost) {
        return vault != null && vault.has(player, cost);
    }

    @Override
    public boolean withdrawBalance(OfflinePlayer player, double cost) {
        return vault != null && vault.withdrawPlayer(player, cost).transactionSuccess();
    }

    @Override
    public boolean deposit(OfflinePlayer player, double amount) {
        return vault != null && vault.depositPlayer(player, amount).transactionSuccess();
    }
}
