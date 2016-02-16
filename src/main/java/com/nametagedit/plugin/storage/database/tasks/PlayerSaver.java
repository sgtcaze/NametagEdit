package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.storage.data.PlayerData;
import com.nametagedit.plugin.utils.Utils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@AllArgsConstructor
public class PlayerSaver extends BukkitRunnable {

    private PlayerData playerData;
    private HikariDataSource hikari;

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            String query = "INSERT INTO `nte_players` VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE `prefix`=?, `suffix`=?";
            PreparedStatement insertOrUpdate = connection.prepareStatement(query);
            insertOrUpdate.setString(1, playerData.getUuid().toString());
            insertOrUpdate.setString(2, playerData.getName());
            insertOrUpdate.setString(3, Utils.deformat(playerData.getPrefix()));
            insertOrUpdate.setString(4, Utils.deformat(playerData.getSuffix()));
            insertOrUpdate.setString(5, Utils.deformat(playerData.getPrefix()));
            insertOrUpdate.setString(6, Utils.deformat(playerData.getSuffix()));
            insertOrUpdate.execute();
            insertOrUpdate.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}