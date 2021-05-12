package ca.tweetzy.core.database;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The current file has been created by Kiran Hart
 * Date Created: September 10 2020
 * Time Created: 10:45 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class DataManagerAbstract {

    protected final DatabaseConnector databaseConnector;
    protected final Plugin plugin;

    private static Map<String, LinkedList<Runnable>> queues = new HashMap<>();

    public DataManagerAbstract(DatabaseConnector databaseConnector, Plugin plugin) {
        this.databaseConnector = databaseConnector;
        this.plugin = plugin;
    }

    /**
     * @return the prefix to be used by all table names
     */
    public String getTablePrefix() {
        return this.plugin.getDescription().getName().toLowerCase() + '_';
    }

    protected int lastInsertedId(Connection connection, String table) {
        String select = "SELECT * FROM " + this.getTablePrefix() + table + " ORDER BY id DESC LIMIT 1";
        String query;
        if (this.databaseConnector instanceof SQLiteConnector) {
            query = table == null ? "SELECT last_insert_rowid()" : select;
        } else {
            query = table == null ? "SELECT LAST_INSERT_ID()" : select;
        }

        int id = -1;
        try (Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(query);
            result.next();
            id = result.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * Queue a task to be run asynchronously. <br>
     *
     * @param runnable task to run
     */
    public void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, runnable);
    }

    /**
     * Queue a task to be run synchronously.
     *
     * @param runnable task to run on the next server tick
     */
    public void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(this.plugin, runnable);
    }

    /**
     * Queue tasks to be ran asynchronously.
     *
     * @param runnable task to put into queue.
     * @param queueKey the queue key to add the runnable to.
     */
    public void queueAsync(Runnable runnable, String queueKey) {
        if (queueKey == null) return;
        List<Runnable> queue = queues.computeIfAbsent(queueKey, t -> new LinkedList<>());
        queue.add(runnable);
        if (queue.size() == 1) runQueue(queueKey);
    }

    private void runQueue(String queueKey) {
        doQueue(queueKey, (s) -> {
            if (!queues.get(queueKey).isEmpty())
                runQueue(queueKey);
        });
    }

    private void doQueue(String queueKey, Consumer<Boolean> callback) {
        Runnable runnable = queues.get(queueKey).getFirst();
        async(() -> {
            runnable.run();
            sync(() -> {
                queues.get(queueKey).remove(runnable);
                callback.accept(true);
            });
        });
    }
}
