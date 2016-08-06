package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.utils.Utils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@AllArgsConstructor
public class GroupSaver extends BukkitRunnable {

    private GroupData groupData;
    private HikariDataSource hikari;

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            String query = "UPDATE nte_groups SET prefix=?, suffix=?, permission=? WHERE name=?";
            PreparedStatement update = connection.prepareStatement(query);
            update.setString(1, Utils.deformat(groupData.getPrefix()));
            update.setString(2, Utils.deformat(groupData.getSuffix()));
            update.setString(3, groupData.getPermission());
            update.setString(4, groupData.getGroupName());
            update.execute();
            update.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}