package com.nametagedit.plugin.storage.database;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.storage.AbstractConfig;
import com.nametagedit.plugin.storage.data.GroupData;
import com.nametagedit.plugin.storage.data.PlayerData;
import com.nametagedit.plugin.storage.database.tasks.*;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

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
        FileConfiguration config = plugin.getConfig();
        hikari = new HikariDataSource();
        hikari.setMaximumPoolSize(10);
        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikari.addDataSourceProperty("serverName", config.getString("MySQL.Hostname"));
        hikari.addDataSourceProperty("port", "3306");
        hikari.addDataSourceProperty("databaseName", config.getString("MySQL.Database"));
        hikari.addDataSourceProperty("user", config.getString("MySQL.Username"));
        hikari.addDataSourceProperty("password", config.getString("MySQL.Password"));
        new TableCreator(hikari).runTask(plugin);
        new DataDownloader(handler, hikari).runTask(plugin);
    }

    @Override
    public void reload() {
        new DataDownloader(handler, hikari).runTask(plugin);
    }

    @Override
    public void shutdown() {
        if (hikari != null) {
            hikari.shutdown();
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

}