package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.utils.Utils;
import com.zaxxer.hikari.HikariDataSource;
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

    private final List<UUID> players = new ArrayList<>();
    private NametagHandler handler;
    private HikariDataSource hikari;

    public DataDownloader(NametagHandler handler, HikariDataSource hikari) {
        this.handler = handler;
        this.hikari = hikari;
        for (Player player : Utils.getOnline()) {
            players.add(player.getUniqueId());
        }
    }

    @Override
    public void run() {
        final HashMap<String, String> settings = new HashMap<>();
        final List<GroupData> groupData = new ArrayList<>();
        final Map<UUID, PlayerData> playerData = new HashMap<>();

        try (Connection connection = hikari.getConnection()) {
            ResultSet results = connection.prepareStatement("SELECT `name`, `prefix`, `suffix`, `permission`, `priority` FROM `nte_groups`").executeQuery();

            while (results.next()) {
                groupData.add(new GroupData(
                        results.getString("name"),
                        results.getString("prefix"),
                        results.getString("suffix"),
                        results.getString("permission"),
                        new Permission(results.getString("permission"), PermissionDefault.FALSE),
                        results.getInt("priority")
                ));
            }

            PreparedStatement select = connection.prepareStatement("SELECT `uuid`, `prefix`, `suffix`, `priority` FROM `nte_players` WHERE uuid=?");
            for (UUID uuid : players) {
                select.setString(1, uuid.toString());
                results = select.executeQuery();
                if (results.next()) {
                    playerData.put(uuid, new PlayerData(
                            "",
                            uuid,
                            Utils.format(results.getString("prefix"), true),
                            Utils.format(results.getString("suffix"), true),
                            results.getInt("priority")
                    ));
                }
            }

            results = connection.prepareStatement("SELECT `setting`,`value` FROM `nte_config`").executeQuery();
            while (results.next()) {
                settings.put(results.getString("setting"), results.getString("value"));
            }

            select.close();
            results.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            new BukkitRunnable() {
                @Override
                public void run() {
                    handler.setGroupData(groupData);
                    handler.setPlayerData(playerData);
                    loadDatabaseSettings(handler, settings);
                    for (Player player : Utils.getOnline()) {
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

    private void loadDatabaseSettings(NametagHandler handler, HashMap<String, String> settings) {
        String orderSetting = settings.get("order");
        if (orderSetting != null) {
            String[] order = orderSetting.split(" ");
            List<GroupData> current = new ArrayList<>();
            // Determine order for current loaded groups
            for (String group : order) {
                Iterator<GroupData> itr = handler.getGroupData().iterator();
                while (itr.hasNext()) {
                    GroupData groupData = itr.next();
                    if (groupData.getGroupName().equalsIgnoreCase(group)) {
                        current.add(groupData);
                        itr.remove();
                        break;
                    }
                }
            }

            current.addAll(handler.getGroupData()); // Add remaining entries (bad order, wasn't specified)
            handler.setGroupData(current);
        }
    }

}