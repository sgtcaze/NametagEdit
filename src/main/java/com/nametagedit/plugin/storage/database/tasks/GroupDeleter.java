package com.nametagedit.plugin.storage.database.tasks;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@AllArgsConstructor
public class GroupDeleter extends BukkitRunnable {

    private String groupName;
    private HikariDataSource hikari;

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            String query = "DELETE FROM nte_groups WHERE name=?";
            PreparedStatement delete = connection.prepareStatement(query);
            delete.setString(1, groupName);
            delete.execute();
            delete.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}