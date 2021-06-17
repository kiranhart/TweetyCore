package ca.tweetzy.core;

import ca.tweetzy.core.configuration.Config;
import ca.tweetzy.core.locale.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/12/2020
 * Time Created: 6:10 PM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public abstract class TweetyPlugin extends JavaPlugin {

    protected Locale locale;
    protected Config config = new Config(this);

    protected ConsoleCommandSender console = Bukkit.getConsoleSender();
    private boolean emergencyStop = false;

    public abstract void onPluginLoad();

    public abstract void onPluginEnable();

    public abstract void onPluginDisable();

    /**
     * Called after reloadConfig​() is called
     */
    public abstract void onConfigReload();

    /**
     * Any other plugin configuration files used by the plugin.
     *
     * @return a list of Configs that are used in addition to the main config.
     */
    public abstract List<Config> getExtraConfig();

    @Override
    public FileConfiguration getConfig() {
        return config.getFileConfig();
    }

    public Config getCoreConfig() {
        return config;
    }

    @Override
    public void reloadConfig() {
        config.load();
        onConfigReload();
    }

    @Override
    public void saveConfig() {
        config.save();
    }

    @Override
    public final void onLoad() {
        try {
            onPluginLoad();
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Unexpected error while loading " + getDescription().getName()
                            + " v" + getDescription().getVersion()
                            + " c" + TweetyCore.getCoreVersion()
                            + ": Disabling plugin!", t);
            emergencyStop = true;
        }
    }

    @Override
    public final void onEnable() {
        if (emergencyStop) {
            setEnabled(false);
            return;
        }

        console.sendMessage(" "); // blank line to separate chatter
        console.sendMessage(ChatColor.GREEN + "=============================");
        console.sendMessage(String.format("%s%s %s by %s%s", ChatColor.GRAY.toString(),
                getDescription().getName(), getDescription().getVersion(), ChatColor.DARK_AQUA.toString(), getDescription().getAuthors().get(0)));
        console.sendMessage(String.format("%sAction: %s%s%s...", ChatColor.GRAY.toString(),
                ChatColor.GREEN.toString(), "Enabling", ChatColor.GRAY.toString()));

        try {
            locale = new Locale(this, "en_US");
            // plugin setup
            onPluginEnable();
            if (emergencyStop) {
                console.sendMessage(ChatColor.RED + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                console.sendMessage(" ");
                return;
            }

        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Unexpected error while loading " + getDescription().getName()
                            + " v" + getDescription().getVersion()
                            + " c" + TweetyCore.getCoreVersion()
                            + ": Disabling plugin!", t);
            emergencyStop();
            console.sendMessage(ChatColor.RED + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            console.sendMessage(" ");
            return;
        }

        console.sendMessage(ChatColor.GREEN + "=============================");
        console.sendMessage(" "); // blank line to separate chatter
    }

    @Override
    public final void onDisable() {
        if (emergencyStop) {
            return;
        }
        console.sendMessage(" "); // blank line to speparate chatter
        console.sendMessage(ChatColor.GREEN + "=============================");
        console.sendMessage(String.format("%s%s %s by %s%s", ChatColor.GRAY.toString(),
                getDescription().getName(), getDescription().getVersion(), ChatColor.DARK_AQUA.toString(), getDescription().getAuthors().get(0)));
        console.sendMessage(String.format("%sAction: %s%s%s...", ChatColor.GRAY.toString(),
                ChatColor.RED.toString(), "Disabling", ChatColor.GRAY.toString()));
        onPluginDisable();
        console.sendMessage(ChatColor.GREEN + "=============================");
        console.sendMessage(" ");
    }

    protected void emergencyStop() {
        emergencyStop = true;
        Bukkit.getPluginManager().disablePlugin(this);
    }

    public ConsoleCommandSender getConsole() {
        return console;
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * Set the plugin's locale to a specific language
     *
     * @param localeName locale to use, eg "en_US"
     */
    public void setLocale(String localeName) {
        locale = new Locale(this, localeName);
    }
}