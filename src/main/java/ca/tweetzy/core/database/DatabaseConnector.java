package ca.tweetzy.core.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The current file has been created by Kiran Hart
 * Date Created: September 10 2020
 * Time Created: 10:41 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public interface DatabaseConnector {

    boolean isInitialized();

    void closeConnection();

    void connect(ConnectionCallback callback);

    interface ConnectionCallback {
        void accept(Connection connection) throws SQLException;
    }
}
