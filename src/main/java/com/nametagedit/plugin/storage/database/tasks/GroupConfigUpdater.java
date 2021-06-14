package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@AllArgsConstructor
public class GroupConfigUpdater extends BukkitRunnable {

    private final String setting;
    private final String value;
    private final HikariDataSource hikari;

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            final String QUERY = "INSERT INTO " + DatabaseConfig.TABLE_GROUPS + " VALUES(?, ?) ON DUPLICATE KEY UPDATE `value`=?";
            PreparedStatement update = connection.prepareStatement(QUERY);
            update.setString(1, setting);
            update.setString(2, value);
            update.setString(3, value);
            update.execute();
            update.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}