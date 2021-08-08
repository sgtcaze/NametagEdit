package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.nametagedit.plugin.utils.Utils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@AllArgsConstructor
public class GroupSaver extends BukkitRunnable {

    private final GroupData[] groupData;
    private final HikariDataSource hikari;

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            final String QUERY = "UPDATE " + DatabaseConfig.TABLE_GROUPS + " SET `prefix`=?, `suffix`=?, `permission`=?, `priority`=? WHERE `name`=?";
            PreparedStatement update = connection.prepareStatement(QUERY);

            for (GroupData groupData : this.groupData) {
                update.setString(1, Utils.deformat(groupData.getPrefix()));
                update.setString(2, Utils.deformat(groupData.getSuffix()));
                update.setString(3, groupData.getPermission());
                update.setInt(4, groupData.getSortPriority());
                update.setString(5, groupData.getGroupName());
                update.addBatch();
            }

            update.executeBatch();
            update.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}