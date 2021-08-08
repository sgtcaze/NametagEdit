package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@AllArgsConstructor
public class PlayerDeleter extends BukkitRunnable {

    private final UUID uuid;
    private final HikariDataSource hikari;

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            final String QUERY = "DELETE FROM " + DatabaseConfig.TABLE_PLAYERS + " WHERE `uuid`=?";
            PreparedStatement delete = connection.prepareStatement(QUERY);
            delete.setString(1, uuid.toString());
            delete.execute();
            delete.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}