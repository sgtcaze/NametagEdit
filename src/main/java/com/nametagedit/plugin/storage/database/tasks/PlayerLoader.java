package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.api.data.PlayerData;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@AllArgsConstructor
public class PlayerLoader extends BukkitRunnable {

    private UUID uuid;
    private Plugin plugin;
    private NametagHandler handler;
    private HikariDataSource hikari;

    @Override
    public void run() {
        String tempPrefix = null;
        String tempSuffix = null;
        boolean found = false;

        try (Connection connection = hikari.getConnection()) {
            String query = "SELECT prefix,suffix FROM nte_players WHERE uuid=?";

            PreparedStatement select = connection.prepareStatement(query);
            select.setString(1, uuid.toString());

            ResultSet resultSet = select.executeQuery();
            if (resultSet.next()) {
                tempPrefix = resultSet.getString("prefix");
                tempSuffix = resultSet.getString("suffix");
                found = true;
            }

            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            final String prefix = tempPrefix == null ? "" : tempPrefix;
            final String suffix = tempSuffix == null ? "" : tempSuffix;
            final boolean finalFound = found;

            new BukkitRunnable() {
                @Override
                public void run() {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        if (finalFound) {
                            PlayerData data = handler.getPlayerData(player);
                            if (data == null) {
                                data = new PlayerData(player.getName(), player.getUniqueId(), prefix, suffix, -1); // TODO: Sort priority
                                handler.getPlayerData().put(player.getUniqueId(), data);
                            } else {
                                data.setPrefix(prefix);
                                data.setSuffix(suffix);
                            }
                        }

                        handler.applyTagToPlayer(player);
                    }
                }
            }.runTask(plugin);
        }
    }

}