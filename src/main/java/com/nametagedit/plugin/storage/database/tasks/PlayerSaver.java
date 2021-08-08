package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.nametagedit.plugin.utils.Utils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@AllArgsConstructor
public class PlayerSaver extends BukkitRunnable {

    private final PlayerData[] playerData;
    private final HikariDataSource hikari;

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            final String QUERY = "INSERT INTO " + DatabaseConfig.TABLE_PLAYERS + " VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `prefix`=?, `suffix`=?, `priority`=?";
            PreparedStatement insertOrUpdate = connection.prepareStatement(QUERY);

            for (PlayerData playerData : this.playerData) {
                insertOrUpdate.setString(1, playerData.getUuid().toString());
                insertOrUpdate.setString(2, playerData.getName());
                insertOrUpdate.setString(3, Utils.deformat(playerData.getPrefix()));
                insertOrUpdate.setString(4, Utils.deformat(playerData.getSuffix()));
                insertOrUpdate.setInt(5, -1);
                insertOrUpdate.setString(6, Utils.deformat(playerData.getPrefix()));
                insertOrUpdate.setString(7, Utils.deformat(playerData.getSuffix()));
                insertOrUpdate.setInt(8, playerData.getSortPriority());
                insertOrUpdate.addBatch();
            }

            insertOrUpdate.executeBatch();
            insertOrUpdate.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}