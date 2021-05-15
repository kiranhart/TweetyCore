package ca.tweetzy.core;

import ca.tweetzy.core.commands.CommandManager;
import ca.tweetzy.core.compatibility.XMaterial;
import ca.tweetzy.core.core.PluginInfo;
import ca.tweetzy.core.core.TweetzyCoreCommand;
import ca.tweetzy.core.core.TweetzyCoreInfoCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/12/2020
 * Time Created: 6:21 PM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public class TweetyCore {

    private final static String prefix = "[TweetyCore]";

    private final static int coreRevision = 2;
    private final static String coreVersion = "2.1.1";

    private CommandManager commandManager;
    private JavaPlugin piggybackedPlugin;

    private ArrayList<BukkitTask> tasks = new ArrayList<>();

    private final static Set<PluginInfo> registeredPlugins = new HashSet<>();
    private static TweetyCore INSTANCE = null;

    public static boolean hasShading() {
        // sneaky hack to check the package name since maven tries to re-shade all references to the package string
        return !TweetyCore.class.getPackage().getName().equals(new String(new char[]{'c', 'a', '.', 't', 'w', 'e', 'e', 't', 'z', 'y', '.', 'c', 'o', 'r', 'e'}));
    }

    TweetyCore() {
        commandManager = null;
    }

    TweetyCore(JavaPlugin plugin) {
        piggybackedPlugin = plugin;
        commandManager = new CommandManager(piggybackedPlugin);
    }

    private void init() {
        commandManager.registerCommandDynamically(new TweetzyCoreCommand()).addSubCommand(new TweetzyCoreInfoCommand());
    }

    private PluginInfo register(JavaPlugin plugin, int pluginID, String icon, String libraryVersion) {
        System.out.println(getPrefix() + "Hooked " + plugin.getName() + ".");
        PluginInfo info = new PluginInfo(plugin, pluginID, icon, libraryVersion);
        registeredPlugins.add(info);
        return info;
    }

    public static void registerPlugin(JavaPlugin plugin, int pluginID, XMaterial icon) {
        registerPlugin(plugin, pluginID, icon == null ? "STONE" : icon.name(), coreVersion);
    }

    public static void registerPlugin(JavaPlugin plugin, int pluginID, String icon) {
        registerPlugin(plugin, pluginID, icon, "?");
    }

    public static void registerPlugin(JavaPlugin plugin, int pluginID, String icon, String coreVersion) {
        if (INSTANCE == null) {
            // First: are there any other instances of TweetyCore active?
            for (Class<?> clazz : Bukkit.getServicesManager().getKnownServices()) {
                if (clazz.getSimpleName().equals("TweetyCore")) {
                    try {
                        // test to see if we're up to date
                        int otherVersion;
                        try {
                            otherVersion = (int) clazz.getMethod("getCoreVersion").invoke(null);
                        } catch (Exception ignore) {
                            otherVersion = -1;
                        }
                        if (otherVersion >= getCoreVersion()) {
                            // use the active service
                            // assuming that the other is greater than R6 if we get here ;)
                            clazz.getMethod("registerPlugin", JavaPlugin.class, int.class, String.class, String.class).invoke(null, plugin, pluginID, icon, coreVersion);

                            if (hasShading()) {
                                (INSTANCE = new TweetyCore()).piggybackedPlugin = plugin;
//                                INSTANCE.shadingListener = new ShadedEventListener();
//                                Bukkit.getPluginManager().registerEvents(INSTANCE.shadingListener, plugin);
                            }
                            return;
                        } else {
                            // we are newer than the registered service: steal all of its registrations
                            // grab the old core's registrations
                            List otherPlugins = (List) clazz.getMethod("getPlugins").invoke(null);
                            // destroy the old core
                            Object oldCore = clazz.getMethod("getInstance").invoke(null);
                            Method destruct = clazz.getDeclaredMethod("destroy");
                            destruct.setAccessible(true);
                            destruct.invoke(oldCore);
                            // register ourselves as the SongodaCore service!
                            INSTANCE = new TweetyCore(plugin);
                            INSTANCE.init();
                            Bukkit.getServicesManager().register(TweetyCore.class, INSTANCE, plugin, ServicePriority.Normal);
                            // we need (JavaPlugin plugin, int pluginID, String icon) for our object
                            if (!otherPlugins.isEmpty()) {
                                Object testSubject = otherPlugins.get(0);
                                Class otherPluginInfo = testSubject.getClass();
                                Method otherPluginInfo_getJavaPlugin = otherPluginInfo.getMethod("getJavaPlugin");
                                Method otherPluginInfo_getSongodaId = otherPluginInfo.getMethod("getPluginId");
                                Method otherPluginInfo_getCoreIcon = otherPluginInfo.getMethod("getCoreIcon");
                                Method otherPluginInfo_getCoreLibraryVersion = otherVersion >= 6 ? otherPluginInfo.getMethod("getCoreLibraryVersion") : null;
                                for (Object other : otherPlugins) {
                                    INSTANCE.register(
                                            (JavaPlugin) otherPluginInfo_getJavaPlugin.invoke(other),
                                            (int) otherPluginInfo_getSongodaId.invoke(other),
                                            (String) otherPluginInfo_getCoreIcon.invoke(other),
                                            otherPluginInfo_getCoreLibraryVersion != null ? (String) otherPluginInfo_getCoreLibraryVersion.invoke(other) : "?");
                                }
                            }
                        }
                        return;
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
                        plugin.getLogger().log(Level.WARNING, "Error registering core service", ignored);
                    }
                }
            }
            // register ourselves as the SongodaCore service!
            INSTANCE = new TweetyCore(plugin);
            INSTANCE.init();
            Bukkit.getServicesManager().register(TweetyCore.class, INSTANCE, plugin, ServicePriority.Normal);
        }
    }

    public static boolean isRegistered(String plugin) {
        return registeredPlugins.stream().anyMatch(p -> p.getJavaPlugin().getName().equalsIgnoreCase(plugin));
    }

    public static int getCoreVersion() {
        return coreRevision;
    }

    public static String getPrefix() {
        return prefix + " ";
    }

    public static List<PluginInfo> getPlugins() {
        return new ArrayList<>(registeredPlugins);
    }

    /**
     * Used to yield this core to a newer core
     */
    private void destroy() {
        Bukkit.getServicesManager().unregister(TweetyCore.class, INSTANCE);
        tasks.stream().filter(Objects::nonNull)
                .forEach(task -> Bukkit.getScheduler().cancelTask(task.getTaskId()));
        registeredPlugins.clear();
        commandManager = null;
    }

    public static JavaPlugin getHijackedPlugin() {
        return INSTANCE == null ? null : INSTANCE.piggybackedPlugin;
    }

    public static TweetyCore getInstance() {
        return INSTANCE;
    }

}
