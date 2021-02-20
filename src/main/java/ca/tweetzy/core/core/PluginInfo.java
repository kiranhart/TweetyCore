package ca.tweetzy.core.core;

import ca.tweetzy.core.compatibility.XMaterial;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/13/2020
 * Time Created: 5:14 PM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public class PluginInfo {

    private final JavaPlugin javaPlugin;

    private String name;
    private String version;
    private String icon;
    private int id;
    private boolean hasUpdate;

    public PluginInfo(JavaPlugin javaPlugin, int id, String icon) {
        this(javaPlugin, javaPlugin.getDescription().getName(), javaPlugin.getDescription().getVersion(), icon);
        this.id = id;
    }

    public PluginInfo(JavaPlugin javaPlugin, String name, String version, String icon) {
        this.javaPlugin = javaPlugin;
        this.name = name;
        this.version = version;
        this.icon = icon;
        this.hasUpdate = false;
    }

    public JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isHasUpdate() {
        return hasUpdate;
    }

    public void setHasUpdate(boolean hasUpdate) {
        this.hasUpdate = hasUpdate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
