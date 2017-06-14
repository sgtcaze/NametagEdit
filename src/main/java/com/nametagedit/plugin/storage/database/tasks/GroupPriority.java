package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@AllArgsConstructor
public class GroupPriority extends BukkitRunnable {

    private String group;
    private int priority;
    private HikariDataSource hikari;

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + DatabaseConfig.TABLE_GROUPS + " SET `priority`=? WHERE `name`=?");
            preparedStatement.setInt(1, priority);
            preparedStatement.setString(2, group);
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}