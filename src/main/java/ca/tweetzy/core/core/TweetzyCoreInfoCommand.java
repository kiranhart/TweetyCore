package ca.tweetzy.core.core;

import ca.tweetzy.core.TweetyCore;
import ca.tweetzy.core.commands.AbstractCommand;
import ca.tweetzy.core.compatibility.ServerProject;
import ca.tweetzy.core.compatibility.ServerVersion;
import ca.tweetzy.core.utils.nms.NMSUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/13/2020
 * Time Created: 7:12 PM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public class TweetzyCoreInfoCommand extends AbstractCommand {

    private final DecimalFormat format = new DecimalFormat("##.##");
    private Object serverInstance;
    private Field tpsField;

    public TweetzyCoreInfoCommand() {
        super(CommandType.CONSOLE_OK, "info");

        try {
            serverInstance = NMSUtils.getNMSClass("MinecraftServer").getMethod("getServer").invoke(null);
            tpsField = serverInstance.getClass().getField("recentTps");
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        sender.sendMessage("");
        sender.sendMessage("Tweetzy Diagnostics Information");
        sender.sendMessage("");
        sender.sendMessage("Plugins:");
        for (PluginInfo plugin : TweetyCore.getPlugins()) {
            sender.sendMessage(plugin.getJavaPlugin().getName() + " (" + plugin.getJavaPlugin().getDescription().getVersion() + ")");
        }
        sender.sendMessage("");
        sender.sendMessage("Server Version: " + Bukkit.getVersion());
        sender.sendMessage("NMS: " + ServerProject.getServerVersion() + " " + ServerVersion.getServerVersionString());
        sender.sendMessage("Operating System: " + System.getProperty("os.name"));
        sender.sendMessage("Allocated Memory: " + format.format(Runtime.getRuntime().maxMemory() / (1024 * 1024)) + "Mb");
        sender.sendMessage("Online Players: " + Bukkit.getOnlinePlayers().size());
        if (tpsField != null) {
            try {
                double[] tps = ((double[]) tpsField.get(serverInstance));

                sender.sendMessage("TPS from last 1m, 5m, 15m: " + format.format(tps[0]) + ", " + format.format(tps[1]) + ", " + format.format(tps[2]));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "tweetzy.admin";
    }

    @Override
    public String getSyntax() {
        return "/tweetzy info";
    }

    @Override
    public String getDescription() {
        return "Display diagnostics information.";
    }
}
