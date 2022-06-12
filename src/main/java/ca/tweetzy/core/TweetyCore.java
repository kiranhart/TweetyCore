package ca.tweetzy.core;

import ca.tweetzy.core.commands.CommandManager;
import ca.tweetzy.core.core.PluginInfo;
import ca.tweetzy.core.core.TweetzyCoreCommand;
import ca.tweetzy.core.core.TweetzyCoreInfoCommand;
import ca.tweetzy.core.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/12/2020
 * Time Created: 6:21 PM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public class TweetyCore {

    private final static String prefix = "[TweetyCore]";
    private final static String cPrefix = "&8[&3TweetyCore&8]";
    private static String pluginPrefix = "[TweetyCore]";
    private final static int coreRevision = 3;

    private CommandManager commandManager;
    private JavaPlugin piggybackedPlugin;
    //private EventListener eventListener;

    private final static Set<PluginInfo> registeredPlugins = new HashSet<>();
    private static TweetyCore INSTANCE = null;


    public static boolean hasShading() {
        // sneaky hack to check the package name since maven tries to re-shade all references to the package string
        return !TweetyCore.class.getPackage().getName().equals(new String(new char[]{'c', 'a', '.', 't', 'w', 'e', 'e', 't', 'z', 'y', '.', 'c', 'o', 'r', 'e'}));
    }

    private void init() {
        commandManager.registerCommandDynamically(new TweetzyCoreCommand()).addSubCommand(new TweetzyCoreInfoCommand());
        //Bukkit.getServer().getPluginManager().registerEvents(eventListener, piggybackedPlugin);
    }

    public static void registerPlugin(JavaPlugin plugin, int pluginID, String icon) {
        if (INSTANCE == null) {

            for (Class<?> clazz : Bukkit.getServicesManager().getKnownServices()) {
                if (clazz.getSimpleName().equalsIgnoreCase("TweetyCore")) {
                    try {

                        int otherCoreVersion;
                        try {
                            otherCoreVersion = (int) clazz.getMethod("getCoreVersion").invoke(null);
                        } catch (Exception ignore) {
                            otherCoreVersion = -1;
                        }

                        if (otherCoreVersion >= getCoreVersion()) {
                            clazz.getMethod("registerPlugin", JavaPlugin.class, int.class, String.class).invoke(null, plugin, pluginID, icon);
                        } else {
                            List otherPlugins = (List) clazz.getMethod("getPlugins").invoke(null);
                            // destroy the old core
                            Object oldCore = clazz.getMethod("getInstance").invoke(null);
                            Method destruct = clazz.getDeclaredMethod("destroy");
                            destruct.setAccessible(true);
                            destruct.invoke(oldCore);

                            INSTANCE = new TweetyCore(plugin);
                            INSTANCE.init();
                            INSTANCE.register(plugin, pluginID, icon);
                            Bukkit.getServicesManager().register(TweetyCore.class, INSTANCE, plugin, ServicePriority.Normal);

                            if (!otherPlugins.isEmpty()) {
                                Object testSubject = otherPlugins.get(0);
                                Class otherPluginInfo = testSubject.getClass();
                                Method otherPluginInfo_getJavaPlugin = otherPluginInfo.getMethod("getJavaPlugin");
                                Method otherPluginInfo_getId = otherPluginInfo.getMethod("getId");
                                Method otherPluginInfo_getIcon = otherPluginInfo.getMethod("getIcon");

                                for (Object other : otherPlugins) {
                                    INSTANCE.register(
                                            (JavaPlugin) otherPluginInfo_getJavaPlugin.invoke(other),
                                            (int) otherPluginInfo_getId.invoke(other),
                                            (String) otherPluginInfo_getIcon.invoke(other)
                                    );
                                }
                            }
                        }

                        return;
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
                        plugin.getLogger().log(Level.WARNING, "Error registering core service", ignored);
                    }
                }
            }

            INSTANCE = new TweetyCore(plugin);
            INSTANCE.init();
            Bukkit.getServicesManager().register(TweetyCore.class, INSTANCE, plugin, ServicePriority.Normal);
        }
        INSTANCE.register(plugin, pluginID, icon);
    }

    public static boolean isRegistered(String plugin) {
        return registeredPlugins.stream().anyMatch(p -> p.getJavaPlugin().getName().equalsIgnoreCase(plugin));
    }

    TweetyCore() {
        commandManager = null;
    }

    TweetyCore(JavaPlugin plugin) {
        piggybackedPlugin = plugin;
        commandManager = new CommandManager(piggybackedPlugin);
        //eventListener = new EventListener();
    }

    private void register(JavaPlugin plugin, int id, String icon) {
        Bukkit.getConsoleSender().sendMessage(TextUtils.formatText(getcPrefix() + "&FHooked into &e" + plugin.getName() + "."));
        PluginInfo info = new PluginInfo(plugin, id, icon);
        registeredPlugins.add(info);
    }

    public static int getCoreVersion() {
        return coreRevision;
    }

    public static String getPrefix() {
        return prefix + " ";
    }

    public static String getcPrefix() {
        return cPrefix + " ";
    }

    public static List<PluginInfo> getPlugins() {
        return new ArrayList<>(registeredPlugins);
    }

    public static String getPluginPrefix() {
        return pluginPrefix;
    }

    public static void setPluginPrefix(String pluginPrefix) {
        TweetyCore.pluginPrefix = pluginPrefix;
    }

    /**
     * Used to yield this core to a newer core
     */
    private void destroy() {
        Bukkit.getServicesManager().unregister(TweetyCore.class, INSTANCE);
        registeredPlugins.clear();
        commandManager = null;
        //eventListener = null;
    }

    public static JavaPlugin getHijackedPlugin() {
        return INSTANCE == null ? null : INSTANCE.piggybackedPlugin;
    }

    public static TweetyCore getInstance() {
        return INSTANCE;
    }

}
