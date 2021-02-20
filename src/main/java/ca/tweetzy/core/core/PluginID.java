package ca.tweetzy.core.core;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/13/2020
 * Time Created: 4:56 PM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public enum PluginID {

    AUCTION_HOUSE("Auction House", 60325, 6806, 1),
    SHOPS("Shops", 75600, 6807, 2),
    COSMIC_VAULTS("Cosmic Vaults", 45463, 6789, 3),
    MONTHLY_CRATES("Monthly Crates", 58390, 7549, 4),
    ITEM_TAGS("Item Tags", 29641, 7550, 5),
    SHOWCASES("Showcases", null, 7548, 6),
    KINGDOMS("Kingdoms", null, 7585, 7),
    MARKETS("Markets", null, 7689, 8),
    ;


    private Object pluginName;
    private Object spigotID;
    private Object bStatsID;
    private Object tweetzyID;

    PluginID(Object pluginName, Object spigotID, Object bStatsID, Object tweetzyID) {
        this.pluginName = pluginName;
        this.spigotID = spigotID;
        this.bStatsID = bStatsID;
        this.tweetzyID = tweetzyID;
    }

    public Object getPluginName() {
        return pluginName;
    }

    public Object getSpigotID() {
        return spigotID;
    }

    public Object getbStatsID() {
        return bStatsID;
    }

    public Object getTweetzyID() {
        return tweetzyID;
    }
}
