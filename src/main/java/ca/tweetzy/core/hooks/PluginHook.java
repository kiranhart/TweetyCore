package ca.tweetzy.core.hooks;

import ca.tweetzy.core.hooks.economies.Economy;
import ca.tweetzy.core.hooks.economies.PlayerPointsEconomy;
import ca.tweetzy.core.hooks.economies.ReserveEconomy;
import ca.tweetzy.core.hooks.economies.VaultEconomy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * The current file has been created by Kiran Hart
 * Date Created: July 24 2021
 * Time Created: 3:15 a.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public final class PluginHook<T extends Class> {

    public static final PluginHook ECO_VAULT = new PluginHook(Economy.class, "Vault", VaultEconomy.class);
    public static final PluginHook ECO_PLAYER_POINTS = new PluginHook(Economy.class, "PlayerPoints", PlayerPointsEconomy.class);
    public static final PluginHook ECO_RESERVE = new PluginHook(Economy.class, "Reserve", ReserveEconomy.class);

    protected final T hookGeneric;
    protected final String plugin;
    protected final Class managerClass;
    protected static Map<Class, PluginHook> hooks;
    protected Constructor pluginConstructor; // for passing the plugin loading the hook to the plugin hook

    private PluginHook(T type, String pluginName, Class handler) {
        if (!Hook.class.isAssignableFrom(handler)) {
            throw new RuntimeException("Tried to register a non-Hook plugin hook! " + pluginName + " -> " + handler.getName());
        }
        this.hookGeneric = type;
        this.plugin = pluginName;
        this.managerClass = handler;
        if (hooks == null) {
            hooks = new LinkedHashMap();
        }
        hooks.put(handler, this);
        // Does this class have a plugin constructor?
        try {
            pluginConstructor = handler.getDeclaredConstructor(Plugin.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            // nope!
        } catch (Throwable t) {
            // (can also reach here if there is a class loader exception)
            hooks.remove(handler);
        }
    }

    /**
     * Add a hook handler for us to use later. <br>
     * NOTE: The class passed MUST extend Hook. <br>
     * Permissible constructors are empty () or (org.bukkit.plugin.Plugin) <br>
     * Each plugin defined must use a different handler class.
     *
     * @param <T>
     * @param type       Generic hook type for this plugin
     * @param pluginName Plugin name
     * @param handler    Specific class that will handle this plugin, if enabled.
     * @return instance of the PluginHook that was added
     */
    public static <T extends Class> PluginHook addHook(T type, String pluginName, Class handler) {
        return new PluginHook(type, pluginName, handler);
    }

    protected static Map<PluginHook, Hook> loadHooks(Class type, Plugin plugin) {
        Map<PluginHook, Hook> loaded = new LinkedHashMap<>();
        PluginManager pluginManager = Bukkit.getPluginManager();

        for (PluginHook hook : getHooks(type)) {
            if (pluginManager.isPluginEnabled(hook.plugin)) {
                Hook handler = (Hook) (plugin != null ? hook.load(plugin) : hook.load());
                if (handler != null && handler.isEnabled()) {
                    loaded.put(hook, handler);
                }
            }
        }

        return loaded;
    }

    protected static List<PluginHook> getHooks(Class type) {
        return hooks.entrySet().parallelStream()
                .filter(e -> e.getKey() == type || e.getValue().managerClass == type || type.isAssignableFrom(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public String getPluginName() {
        return plugin;
    }

    protected Object load() {
        try {
            return managerClass.cast(
                    pluginConstructor != null
                            ? pluginConstructor.newInstance((Plugin) null)
                            : managerClass.getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Unexpected Error while creating a new Hook Manager for " + plugin, ex);
        }
        return null;
    }

    protected Object load(Plugin hookingPlugin) {
        try {
            return managerClass.cast(
                    pluginConstructor != null
                            ? pluginConstructor.newInstance(hookingPlugin)
                            : managerClass.getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Unexpected Error while creating a new Hook Manager for " + plugin, ex);
        }
        return null;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.plugin);
        hash = 37 * hash + Objects.hashCode(this.managerClass);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PluginHook<?> other = (PluginHook<?>) obj;
        return Objects.equals(this.plugin, other.plugin)
                && Objects.equals(this.managerClass, other.managerClass);
    }
}
