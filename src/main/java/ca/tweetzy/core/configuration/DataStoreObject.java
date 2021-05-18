package ca.tweetzy.core.configuration;

import org.bukkit.configuration.ConfigurationSection;

public interface DataStoreObject<T> {

    /**
     * @return a unique hashable instance of T to store this value under
     */
    T getKey();

    /**
     * @return a unique identifier for saving this value with
     */
    String getConfigKey();

    /**
     * Save this data to a ConfigurationSection
     *
     * @param sec
     */
    void saveToSection(ConfigurationSection sec);

    /**
     * @return true if this data has changed from the state saved to file
     */
    boolean hasChanged();

    /**
     * Mark this data as needing a save or not
     *
     * @param isChanged
     */
    void setChanged(boolean isChanged);
}
