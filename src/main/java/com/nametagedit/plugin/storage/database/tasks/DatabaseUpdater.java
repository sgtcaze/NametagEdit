package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagHandler;
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
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            new DataDownloader(handler, hikari).runTaskAsynchronously(plugin);
        }
    }

    private void createTablesIfNotExists(Connection connection) {
        execute(connection, "CREATE TABLE IF NOT EXISTS `nte_config` (`setting` varchar(16) NOT NULL, `value` varchar(200) NOT NULL, PRIMARY KEY (`setting`)) ENGINE=InnoDB DEFAULT CHARSET=latin1");
        execute(connection, "CREATE TABLE IF NOT EXISTS `nte_groups` (`name` varchar(64) NOT NULL, `permission` varchar(64) DEFAULT NULL, `prefix` varchar(16) NOT NULL, `suffix` varchar(16) NOT NULL, `priority` int(11) NOT NULL, PRIMARY KEY (`name`)) ENGINE=MyISAM DEFAULT CHARSET=latin1");
        execute(connection, "CREATE TABLE IF NOT EXISTS `nte_players` (`uuid` varchar(64) NOT NULL, `name` varchar(16) NOT NULL, `prefix` varchar(16) NOT NULL, `suffix` varchar(16) NOT NULL, `priority` int(11) NOT NULL, PRIMARY KEY (`uuid`)) ENGINE=MyISAM DEFAULT CHARSET=latin1");
    }

    private void handleUpdate1(Connection connection) {
        execute(connection, "ALTER TABLE `nte_players` ADD `priority` INT NOT NULL");
        execute(connection, "ALTER TABLE `nte_groups` ADD `priority` INT NOT NULL");
        execute(connection, "ALTER TABLE `nte_groups` MODIFY `permission` VARCHAR(64)");
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