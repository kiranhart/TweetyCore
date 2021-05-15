package ca.tweetzy.core.configuration;

import org.bukkit.configuration.ConfigurationSection;

public interface DataStoreObject<T> {

    /**
     * @return a unique hashable instance of T to store this value under
     */
    public abstract T getKey();

    /**
     * @return a unique identifier for saving this value with
     */
    public abstract String getConfigKey();

    /**
     * Save this data to a ConfigurationSection
     *
     * @param sec
     */
    public abstract void saveToSection(ConfigurationSection sec);

    /**
     * @return true if this data has changed from the state saved to file
     */
    public boolean hasChanged();

    /**
     * Mark this data as needing a save or not
     *
     * @param isChanged
     */
    public void setChanged(boolean isChanged);
}
