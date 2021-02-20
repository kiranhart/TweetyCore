package ca.tweetzy.core.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The current file has been created by Kiran Hart
 * Date Created: September 10 2020
 * Time Created: 10:47 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public abstract class DataMigration {

    private final int revision;

    public DataMigration(int revision) {
        this.revision = revision;
    }

    public abstract void migrate(Connection connection, String tablePrefix) throws SQLException;

    /**
     * @return the revision number of this migration
     */
    public int getRevision() {
        return this.revision;
    }
}
