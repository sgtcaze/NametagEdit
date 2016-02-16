package com.nametagedit.plugin.storage.database.tasks;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@AllArgsConstructor
public class PlayerDeleter extends BukkitRunnable {

    private UUID uuid;
    private HikariDataSource hikari;

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            String query = "DELETE FROM nte_players WHERE uuid=?";
            PreparedStatement delete = connection.prepareStatement(query);
            delete.setString(1, uuid.toString());
            delete.execute();
            delete.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}