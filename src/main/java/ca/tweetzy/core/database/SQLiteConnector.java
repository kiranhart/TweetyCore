package ca.tweetzy.core.database;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The current file has been created by Kiran Hart
 * Date Created: April 27 2021
 * Time Created: 2:02 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class SQLiteConnector implements DatabaseConnector {

    private final Plugin plugin;
    private final String connectionString;
    private Connection connection;

    public SQLiteConnector(Plugin plugin) {
        this(plugin, plugin.getDescription().getName().toLowerCase());
    }

    public SQLiteConnector(Plugin plugin, String fileName) {
        this.plugin = plugin;
        this.connectionString = "jdbc:sqlite:" + plugin.getDataFolder() + File.separator + fileName + ".db";

        try {
            Class.forName("org.sqlite.JDBC"); // This is required to put here for Spigot 1.10 and below to force class load
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isInitialized() {
        return true; // Always available
    }

    @Override
    public void closeConnection() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException ex) {
            this.plugin.getLogger().severe("An error occurred closing the SQLite database connection: " + ex.getMessage());
        }
    }

    @Override
    public void connect(ConnectionCallback callback) {
        if (this.connection == null) {
            try {
                this.connection = DriverManager.getConnection(this.connectionString);
            } catch (SQLException ex) {
                this.plugin.getLogger().severe("An error occurred retrieving the SQLite database connection: " + ex.getMessage());
            }
        }

        try {
            callback.accept(this.connection);
        } catch (Exception ex) {
            this.plugin.getLogger().severe("An error occurred executing an SQLite query: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
