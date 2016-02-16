package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.storage.data.GroupData;
import com.nametagedit.plugin.storage.data.PlayerData;
import com.nametagedit.plugin.utils.Utils;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DataDownloader extends BukkitRunnable {

    private NametagHandler handler;
    private HikariDataSource hikari;
    private final List<UUID> players = new ArrayList<>();

    private static final String GROUP_QUERY = "SELECT name, prefix, suffix, permission FROM nte_groups";
    private static final String PLAYER_QUERY = "SELECT uuid, prefix, suffix FROM nte_players WHERE uuid=?";

    public DataDownloader(NametagHandler handler, HikariDataSource hikari) {
        this.handler = handler;
        this.hikari = hikari;
        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(player.getUniqueId());
        }
    }

    @Override
    public void run() {
        final List<GroupData> groupData = new ArrayList<>();
        final Map<UUID, PlayerData> playerData = new HashMap<>();

        try (Connection connection = hikari.getConnection()) {
            ResultSet results = connection.prepareStatement(GROUP_QUERY).executeQuery();

            while (results.next()) {
                groupData.add(new GroupData(results.getString("name"), results.getString("prefix"), results.getString("suffix"),
                        results.getString("permission"), new Permission(results.getString("permission"), PermissionDefault.FALSE)));
            }

            for (UUID uuid : players) {
                PreparedStatement preparedStatement = connection.prepareStatement(PLAYER_QUERY);
                preparedStatement.setString(1, uuid.toString());
                results = preparedStatement.executeQuery();
                if (results.next()) {
                    playerData.put(uuid, new PlayerData("", uuid, Utils.format(results.getString("prefix"), true), Utils.format(results.getString("suffix"), true)));
                }
            }

            results.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            new BukkitRunnable() {
                @Override
                public void run() {
                    handler.setGroupData(groupData);
                    handler.setPlayerData(playerData);

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        PlayerData data = playerData.get(player.getUniqueId());
                        if (data != null) {
                            data.setName(player.getName());
                        }
                    }

                    handler.applyTags();
                }
            }.runTask(handler.getPlugin());
        }
    }

}