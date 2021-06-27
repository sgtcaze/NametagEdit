package com.nametagedit.plugin.storage.database;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.storage.AbstractConfig;
import com.nametagedit.plugin.storage.database.tasks.*;
import com.nametagedit.plugin.utils.Configuration;
import com.nametagedit.plugin.utils.UUIDFetcher;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DatabaseConfig implements AbstractConfig {

    private NametagEdit plugin;
    private NametagHandler handler;
    private HikariDataSource hikari;

    // These are used if the user wants to customize the
    // schema structure. Perhaps more cosmetic.
    public static String TABLE_GROUPS;
    public static String TABLE_PLAYERS;
    public static String TABLE_CONFIG;

    public DatabaseConfig(NametagEdit plugin, NametagHandler handler, Configuration config) {
        this.plugin = plugin;
        this.handler = handler;
        TABLE_GROUPS = "`" + config.getString("MySQL.GroupsTable", "nte_groups") + "`";
        TABLE_PLAYERS = "`" + config.getString("MySQL.PlayersTable", "nte_players") + "`";
        TABLE_CONFIG = "`" + config.getString("MySQL.ConfigTable", "nte_config") + "`";
    }

    @Override
    public void load() {
        FileConfiguration config = handler.getConfig();
        shutdown();
        hikari = new HikariDataSource();
        hikari.setMaximumPoolSize(config.getInt("MinimumPoolSize", 10));
        hikari.setPoolName("NametagEdit Pool");

        String port = "3306";

        if (config.isSet("MySQL.Port")) {
            port = config.getString("MySQL.Port");
        }

        hikari.setJdbcUrl("jdbc:mysql://" + config.getString("MySQL.Hostname") + ":" + port + "/" + config.getString("MySQL.Database"));
        hikari.addDataSourceProperty("useSSL", false);
        hikari.addDataSourceProperty("requireSSL", false);
        hikari.addDataSourceProperty("verifyServerCertificate", false);
        hikari.addDataSourceProperty("user", config.getString("MySQL.Username"));
        hikari.addDataSourceProperty("password", config.getString("MySQL.Password"));

        hikari.setUsername(config.getString("MySQL.Username"));
        hikari.setPassword(config.getString("MySQL.Password"));

        new DatabaseUpdater(handler, hikari, plugin).runTaskAsynchronously(plugin);
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
    public void load(Player player, boolean loggedIn) {
        new PlayerLoader(player.getUniqueId(), plugin, handler, hikari, loggedIn).runTaskAsynchronously(plugin);
    }

    @Override
    public void save(PlayerData... playerData) {
        new PlayerSaver(playerData, hikari).runTaskAsynchronously(plugin);
    }

    @Override
    public void save(GroupData... groupData) {
        new GroupSaver(groupData, hikari).runTaskAsynchronously(plugin);
    }

    @Override
    public void savePriority(boolean playerTag, String key, final int priority) {
        if (playerTag) {
            UUIDFetcher.lookupUUID(key, plugin, uuid -> {
                if (uuid != null) {
                    new PlayerPriority(uuid, priority, hikari).runTaskAsynchronously(plugin);
                } else {
                    plugin.getLogger().severe("An error has occurred while looking for UUID.");
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
    public void orderGroups(CommandSender commandSender, List<String> order) {
        String formatted = Arrays.toString(order.toArray());
        formatted = formatted.substring(1, formatted.length() - 1).replace(",", "");
        new GroupConfigUpdater("order", formatted, hikari).runTaskAsynchronously(handler.getPlugin());
    }

}
