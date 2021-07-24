package ca.tweetzy.core.hooks.economies;

import net.tnemc.core.Reserve;
import net.tnemc.core.economy.EconomyAPI;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

/**
 * The current file has been created by Kiran Hart
 * Date Created: July 24 2021
 * Time Created: 3:20 a.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class ReserveEconomy extends Economy {

    EconomyAPI economyAPI;

    public ReserveEconomy() {
        if (Reserve.instance().economyProvided())
            economyAPI = Reserve.instance().economy();
    }

    @Override
    public boolean isEnabled() {
        return Reserve.instance().isEnabled();
    }

    @Override
    public String getName() {
        return "Reserve";
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return economyAPI.getBankHoldings(player.getUniqueId()).doubleValue();
    }

    @Override
    public boolean hasBalance(OfflinePlayer player, double cost) {
        return economyAPI.hasHoldings(player.getUniqueId(), new BigDecimal(cost));
    }

    @Override
    public boolean withdrawBalance(OfflinePlayer player, double cost) {
        return economyAPI.removeHoldings(player.getUniqueId(), new BigDecimal(cost));
    }

    @Override
    public boolean deposit(OfflinePlayer player, double amount) {
        return economyAPI.addHoldings(player.getUniqueId(), new BigDecimal(amount));
    }
}
