package com.nametagedit.plugin.storage.database;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.storage.AbstractConfig;
import com.nametagedit.plugin.storage.database.tasks.*;
import com.nametagedit.plugin.utils.UUIDFetcher;
import com.nametagedit.plugin.utils.Utils;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DatabaseConfig implements AbstractConfig {

    private NametagEdit plugin;
    private NametagHandler handler;
    private HikariDataSource hikari;

    public DatabaseConfig(NametagEdit plugin, NametagHandler handler) {
        this.plugin = plugin;
        this.handler = handler;
    }

    @Override
    public void load() {
        FileConfiguration config = handler.getConfig();
        shutdown();
        hikari = new HikariDataSource();
        hikari.setMaximumPoolSize(10);
        hikari.setPoolName("NametagEdit Pool");
        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikari.addDataSourceProperty("serverName", config.getString("MySQL.Hostname"));
        hikari.addDataSourceProperty("port", config.getString("MySQL.Port"));
        hikari.addDataSourceProperty("databaseName", config.getString("MySQL.Database"));
        hikari.addDataSourceProperty("user", config.getString("MySQL.Username"));
        hikari.addDataSourceProperty("password", config.getString("MySQL.Password"));
        if (config.getInt("DatabaseVersion", 1) < handler.getDatabaseVersion()) {
            new DatabaseUpdater(handler.getConfig().getInt("DatabaseVersion", 1), handler, hikari, plugin).runTaskAsynchronously(plugin);
        } else {
            new DatabaseUpdater(handler.getDatabaseVersion(), handler, hikari, plugin).runTaskAsynchronously(plugin);
        }
    }

    @Override
    public void reload() {
        new DataDownloader(handler, hikari).runTaskAsynchronously(plugin);
    }

    @Override
    public void shutdown() {
        if (hikari != null) {
            hikari.close();
        }
    }

    @Override
    public void load(Player player) {
        new PlayerLoader(player.getUniqueId(), plugin, handler, hikari).runTaskAsynchronously(plugin);
    }

    @Override
    public void save(PlayerData playerData) {
        new PlayerSaver(playerData, hikari).runTaskAsynchronously(plugin);
    }

    @Override
    public void save(GroupData groupData) {
        new GroupSaver(groupData, hikari).runTaskAsynchronously(plugin);
    }

    @Override
    public void savePriority(boolean playerTag, String key, final int priority) {
        if (playerTag) {
            UUIDFetcher.lookupUUID(key, plugin, new UUIDFetcher.UUIDLookup() {
                @Override
                public void response(UUID uuid) {
                    if (uuid != null) {
                        new PlayerPriority(uuid, priority, hikari).runTaskAsynchronously(plugin);
                    } else {
                        // TODO: Send error
                    }
                }
            });
        } else {
            new GroupPriority(key, priority, hikari).runTaskAsynchronously(plugin);
        }
    }

    @Override
    public void delete(GroupData groupData) {
        new GroupDeleter(groupData.getGroupName(), hikari).runTaskAsynchronously(plugin);
    }

    @Override
    public void add(GroupData groupData) {
        new GroupAdd(groupData, hikari).runTaskAsynchronously(plugin);
    }

    @Override
    public void clear(UUID uuid, String targetName) {
        new PlayerDeleter(uuid, hikari).runTaskAsynchronously(plugin);
    }

    @Override
    public void orderGroups(CommandSender commandSender, String[] args) {
        List<String> order = new ArrayList<>(Arrays.asList(args).subList(2, args.length));
        String formatted = Arrays.toString(order.toArray());
        formatted = formatted.substring(1, formatted.length() - 1).replace(",", "");
        commandSender.sendMessage(Utils.format("&c&lNametagEdit Group Order:"));
        commandSender.sendMessage(formatted);
        commandSender.sendMessage(Utils.format("&cType /ne reload for these changes to take effect"));
        new GroupConfigUpdater("order", formatted, hikari).runTaskAsynchronously(handler.getPlugin());
    }

}
