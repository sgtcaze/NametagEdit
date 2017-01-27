package com.nametagedit.plugin.converter;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagMessages;
import com.nametagedit.plugin.utils.Utils;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
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

    private boolean databaseToFile;
    private CommandSender sender;
    private NametagEdit plugin;

    @Override
    public void run() {
        boolean failed = false;
        FileConfiguration config = plugin.getHandler().getConfig();
        String connectionString = "jdbc:mysql://" + config.getString("MySQL.Hostname") + ":"
                + config.getInt("MySQL.Port") + "/" + config.getString("MySQL.Database");
        try (Connection connection = DriverManager.getConnection(connectionString, config.getString("MySQL.Username"), config.getString("MySQL.Password"))) {
            if (databaseToFile) {
                try {
                    failed = convertDatabaseToFile(connection);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                failed = convertFilesToDatabase(connection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            final boolean finalFailed = failed;
            new BukkitRunnable() {
                @Override
                public void run() {
                    NametagMessages.OPERATION_COMPLETED.send(sender, finalFailed ? "failed" : "succeeded");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ne reload");
                }
            }.runTask(plugin);
        }
    }

    private boolean convertDatabaseToFile(Connection connection) throws SQLException, IOException {
        final String GROUP_QUERY = "SELECT name, prefix, suffix, permission, priority FROM nte_groups";
        final String PLAYER_QUERY = "SELECT name, uuid, prefix, suffix, priority FROM nte_players";

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
        return false;
    }

    private boolean convertFilesToDatabase(Connection connection) throws SQLException {
        final File groupsFile = new File(plugin.getDataFolder(), "groups.yml");
        final File playersFile = new File(plugin.getDataFolder(), "players.yml");

        final YamlConfiguration groups = Utils.getConfig(groupsFile);
        final YamlConfiguration players = Utils.getConfig(playersFile);

        PreparedStatement insertOrUpdate = connection.prepareStatement("INSERT INTO `nte_players` (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE prefix=?, suffix=?");
        if (players != null) {
            if (checkValid(groups, "Players")) {
                for (String key : players.getConfigurationSection("Players").getKeys(false)) {
                    insertOrUpdate.setString(1, key);
                    insertOrUpdate.setString(2, players.getString("Players." + key + ".Name"));
                    insertOrUpdate.setString(3, Utils.deformat(players.getString("Players." + key + ".Prefix", "")));
                    insertOrUpdate.setString(4, Utils.deformat(players.getString("Players." + key + ".Suffix", "")));
                    insertOrUpdate.setString(5, players.getString("Players." + key + ".SortPriority"));
                    insertOrUpdate.setString(6, Utils.deformat(players.getString("Players." + key + ".Prefix", "")));
                    insertOrUpdate.setString(7, Utils.deformat(players.getString("Players." + key + ".Suffix", "")));
                    insertOrUpdate.addBatch();
                }
            }
        }

        insertOrUpdate.executeBatch();
        insertOrUpdate = connection.prepareStatement("INSERT INTO `nte_groups` (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE prefix=?, suffix=?, permission=?");
        if (groups != null) {
            if (checkValid(groups, "Groups")) {
                for (String key : groups.getConfigurationSection("Groups").getKeys(false)) {
                    insertOrUpdate.setString(1, key);
                    insertOrUpdate.setString(2, groups.getString("Groups." + key + ".Permission"));
                    insertOrUpdate.setString(3, Utils.deformat(groups.getString("Groups." + key + ".Prefix", "")));
                    insertOrUpdate.setString(4, Utils.deformat(groups.getString("Groups." + key + ".Suffix", "")));
                    insertOrUpdate.setString(5, groups.getString("Groups." + key + ".SortPriority"));
                    insertOrUpdate.setString(6, Utils.deformat(groups.getString("Groups." + key + ".Prefix", "")));
                    insertOrUpdate.setString(7, Utils.deformat(groups.getString("Groups." + key + ".Suffix", "")));
                    insertOrUpdate.setString(8, groups.getString("Groups." + key + ".Permission"));
                    insertOrUpdate.addBatch();
                }
            }
        }

        insertOrUpdate.executeBatch();
        insertOrUpdate.close();
        return false;
    }

    private boolean checkValid(FileConfiguration configuration, String section) {
        if (!configuration.contains(section)) {
            NametagMessages.FILE_MISCONFIGURED.send(sender, section + ".yml");
            return false;
        }

        return true;
    }

}