package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.storage.data.GroupData;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@AllArgsConstructor
public class GroupAdd extends BukkitRunnable {

    private GroupData groupData;
    private HikariDataSource hikari;

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            String query = "INSERT INTO `nte_groups` VALUES(?, ?, ?, ?)";
            PreparedStatement insert = connection.prepareStatement(query);
            insert.setString(1, groupData.getGroupName());
            insert.setString(2, groupData.getPermission());
            insert.setString(3, groupData.getPrefix());
            insert.setString(4, groupData.getSuffix());
            insert.execute();
            insert.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}