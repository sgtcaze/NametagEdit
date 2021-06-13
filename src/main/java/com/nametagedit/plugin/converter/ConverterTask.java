package com.nametagedit.plugin.converter;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagMessages;
import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.nametagedit.plugin.utils.Utils;
import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.*;

/**
 * This class converts to and from Flatfile and MySQL
 */
@AllArgsConstructor
public class ConverterTask extends BukkitRunnable {

    private final boolean databaseToFile;
    private final CommandSender sender;
    private final NametagEdit plugin;

    @Override
    public void run() {
        FileConfiguration config = plugin.getHandler().getConfig();
        String connectionString = "jdbc:mysql://" + config.getString("MySQL.Hostname") + ":" + config.getInt("MySQL.Port") + "/" + config.getString("MySQL.Database");
        try (Connection connection = DriverManager.getConnection(connectionString, config.getString("MySQL.Username"), config.getString("MySQL.Password"))) {
            if (databaseToFile) {
                convertDatabaseToFile(connection);
            } else {
                convertFilesToDatabase(connection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getHandler().reload();
                }
            }.runTask(plugin);
        }
    }

    private void convertDatabaseToFile(Connection connection) {
        try {
            final String GROUP_QUERY = "SELECT name, prefix, suffix, permission, priority FROM " + DatabaseConfig.TABLE_GROUPS;
            final String PLAYER_QUERY = "SELECT name, uuid, prefix, suffix, priority FROM " + DatabaseConfig.TABLE_PLAYERS;

            final File groupsFile = new File(plugin.getDataFolder(), "groups_CONVERTED.yml");
            final File playersFile = new File(plugin.getDataFolder(), "players_CONVERTED.yml");

            final YamlConfiguration groups = Utils.getConfig(groupsFile);
            final YamlConfiguration players = Utils.getConfig(playersFile);

            ResultSet results = connection.prepareStatement(GROUP_QUERY).executeQuery();
            while (results.next()) {
                groups.set("Groups." + results.getString("name") + ".Permission", results.getString("permission"));
                groups.set("Groups." + results.getString("name") + ".Prefix", results.getString("prefix"));
                groups.set("Groups." + results.getString("name") + ".Suffix", results.getString("suffix"));
                groups.set("Groups." + results.getString("name") + ".SortPriority", results.getInt("priority"));
            }

            results = connection.prepareStatement(PLAYER_QUERY).executeQuery();
            while (results.next()) {
                players.set("Players." + results.getString("uuid") + ".Name", results.getString("name"));
                players.set("Players." + results.getString("uuid") + ".Prefix", results.getString("prefix"));
                players.set("Players." + results.getString("uuid") + ".Suffix", results.getString("suffix"));
                players.set("Players." + results.getString("uuid") + ".SortPriority", results.getInt("priority"));
            }

            results.close();
            groups.save(groupsFile);
            players.save(playersFile);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void convertFilesToDatabase(Connection connection) {
        final File groupsFile = new File(plugin.getDataFolder(), "groups.yml");
        final File playersFile = new File(plugin.getDataFolder(), "players.yml");

        final YamlConfiguration groups = Utils.getConfig(groupsFile);
        final YamlConfiguration players = Utils.getConfig(playersFile);

        if (players != null && checkValid(players, "Players")) {
            // Import the player entries from the file
            try (PreparedStatement playerInsert = connection.prepareStatement("INSERT INTO " + DatabaseConfig.TABLE_PLAYERS + " VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `prefix`=?, `suffix`=?")) {
                for (String key : players.getConfigurationSection("Players").getKeys(false)) {
                    playerInsert.setString(1, key);
                    playerInsert.setString(2, players.getString("Players." + key + ".Name"));
                    playerInsert.setString(3, Utils.deformat(players.getString("Players." + key + ".Prefix", "")));
                    playerInsert.setString(4, Utils.deformat(players.getString("Players." + key + ".Suffix", "")));
                    playerInsert.setString(5, players.getString("Players." + key + ".SortPriority"));
                    playerInsert.setString(6, Utils.deformat(players.getString("Players." + key + ".Prefix", "")));
                    playerInsert.setString(7, Utils.deformat(players.getString("Players." + key + ".Suffix", "")));
                    playerInsert.addBatch();
                }

                playerInsert.executeBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (groups != null && checkValid(groups, "Groups")) {
            // Import the player entries from the file
            try (PreparedStatement groupInsert = connection.prepareStatement("INSERT INTO " + DatabaseConfig.TABLE_GROUPS + " VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `prefix`=?, `suffix`=?, `permission`=?")) {
                for (String key : groups.getConfigurationSection("Groups").getKeys(false)) {
                    groupInsert.setString(1, key);
                    groupInsert.setString(2, groups.getString("Groups." + key + ".Permission"));
                    groupInsert.setString(3, Utils.deformat(groups.getString("Groups." + key + ".Prefix", "")));
                    groupInsert.setString(4, Utils.deformat(groups.getString("Groups." + key + ".Suffix", "")));
                    groupInsert.setString(5, groups.getString("Groups." + key + ".SortPriority"));
                    groupInsert.setString(6, Utils.deformat(groups.getString("Groups." + key + ".Prefix", "")));
                    groupInsert.setString(7, Utils.deformat(groups.getString("Groups." + key + ".Suffix", "")));
                    groupInsert.setString(8, groups.getString("Groups." + key + ".Permission"));
                    groupInsert.addBatch();
                }

                groupInsert.executeBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkValid(FileConfiguration configuration, String section) {
        if (!configuration.contains(section)) {
            NametagMessages.FILE_MISCONFIGURED.send(sender, section + ".yml");
            return false;
        }

        return true;
    }

}