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

    protected final JavaPlugin javaPlugin;
    protected final int pluginId;
    protected final String coreIcon;
    protected final String coreLibraryVersion;
    protected final XMaterial icon;
    private boolean hasUpdate = false;
    private String latestVersion;

    public PluginInfo(JavaPlugin javaPlugin, int pluginId, String icon, String coreLibraryVersion) {
        this.javaPlugin = javaPlugin;
        this.pluginId = pluginId;
        this.coreIcon = icon;
        this.icon = XMaterial.getMaterial(icon);
        this.coreLibraryVersion = coreLibraryVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
        hasUpdate = latestVersion != null && !latestVersion.isEmpty() && !javaPlugin.getDescription().getVersion().equalsIgnoreCase(latestVersion);
    }

    public boolean hasUpdate() {
        return hasUpdate;
    }

    public void setHasUpdate(boolean hasUpdate) {
        this.hasUpdate = hasUpdate;
    }


    public JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }

    public String getCoreIcon() {
        return coreIcon;
    }

    public String getCoreLibraryVersion() {
        return coreLibraryVersion;
    }

    public int getPluginId() {
        return pluginId;
    }
}
