package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@AllArgsConstructor
public class DatabaseUpdater extends BukkitRunnable {

    private int currentVersion;
    private NametagHandler handler;
    private HikariDataSource hikari;
    private NametagEdit plugin;

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            createTablesIfNotExists(connection);

            while (currentVersion < handler.getDatabaseVersion()) {
                switch (currentVersion) {
                    case 1:
                        handleUpdate1(connection);
                        break;
                    case 2:
                        handleUpdate2(connection);
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            new DataDownloader(handler, hikari).runTaskAsynchronously(plugin);
        }
    }

    private void createTablesIfNotExists(Connection connection) {
        execute(connection, "CREATE TABLE IF NOT EXISTS " + DatabaseConfig.TABLE_CONFIG + " (`setting` varchar(16) NOT NULL, `value` varchar(200) NOT NULL, PRIMARY KEY (`setting`)) ENGINE=InnoDB DEFAULT CHARSET=latin1");
        execute(connection, "CREATE TABLE IF NOT EXISTS " + DatabaseConfig.TABLE_GROUPS + " (`name` varchar(64) NOT NULL, `permission` varchar(64) DEFAULT NULL, `prefix` varchar(64) NOT NULL, `suffix` varchar(64) NOT NULL, `priority` int(11) NOT NULL, PRIMARY KEY (`name`)) ENGINE=MyISAM DEFAULT CHARSET=latin1");
        execute(connection, "CREATE TABLE IF NOT EXISTS " + DatabaseConfig.TABLE_PLAYERS + " (`uuid` varchar(64) NOT NULL, `name` varchar(16) NOT NULL, `prefix` varchar(64) NOT NULL, `suffix` varchar(64) NOT NULL, `priority` int(11) NOT NULL, PRIMARY KEY (`uuid`)) ENGINE=MyISAM DEFAULT CHARSET=latin1");
    }

    private void handleUpdate1(Connection connection) {
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_PLAYERS + " ADD `priority` INT NOT NULL");
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_GROUPS + " ADD `priority` INT NOT NULL");
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_GROUPS + " MODIFY `permission` VARCHAR(64)");
        currentVersion++;
        handler.getConfig().set("DatabaseVersion", currentVersion);
        handler.getConfig().save();
    }

    private void handleUpdate2(Connection connection) {
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_GROUPS + " CHANGE `prefix` `prefix` VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL;");
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_GROUPS + " CHANGE `suffix` `suffix` VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL;");
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_PLAYERS + " CHANGE `prefix` `prefix` VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL;");
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_PLAYERS + " CHANGE `suffix` `suffix` VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL;");
        currentVersion++;
        handler.getConfig().set("DatabaseVersion", currentVersion);
        handler.getConfig().save();
    }

    private void execute(Connection connection, String query) {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}