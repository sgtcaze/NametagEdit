package com.nametagedit.plugin.converter;

import com.nametagedit.plugin.NametagMessages;
import com.nametagedit.plugin.utils.Utils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts from v2.4 of NametagEdit to
 * use the new storage method introduced
 * in v3.0.
 */
public class Converter {

    private List<String> getLines(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private List<String> getLines(CommandSender commandSender, Plugin plugin, String oldFileName) {
        File oldFile = new File(plugin.getDataFolder(), oldFileName);
        if (!oldFile.exists()) {
            NametagMessages.FILE_DOESNT_EXIST.send(commandSender, oldFileName);
            return new ArrayList<>();
        }

        return getLines(oldFile);
    }

    public void legacyConversion(CommandSender sender, Plugin plugin) {
        try {
            handleFile(plugin, sender, "groups");
            handleFile(plugin, sender, "players");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleFile(Plugin plugin, CommandSender sender, String fileType) throws IOException {
        final boolean GROUP = fileType.equals("groups");
        File nametagConfigFile = new File(plugin.getDataFolder(), fileType + ".yml");
        YamlConfiguration nametagConfig = Utils.getConfig(nametagConfigFile);
        for (String line : getLines(sender, plugin, fileType + ".txt")) {
            if (!line.contains("=")) continue; // If the special token is missing, skip. Malformed line.
            if (GROUP) {
                handleGroup(nametagConfig, line);
            } else {
                handlePlayer(nametagConfig, line);
            }
        }

        nametagConfig.save(nametagConfigFile);
    }

    private void handleGroup(YamlConfiguration config, String line) {
        String[] lineContents = line.replace("=", "").split(" ");
        String[] permissionSplit = lineContents[0].split("\\.");
        String group = WordUtils.capitalizeFully(permissionSplit[permissionSplit.length - 1]);
        String permission = lineContents[0];
        String type = lineContents[1];
        String value = line.substring(line.indexOf("\"") + 1);
        value = value.substring(0, value.indexOf("\""));
        config.set("Groups." + group + ".Permission", permission);
        config.set("Groups." + group + ".SortPriority", -1);
        if (type.equals("prefix")) {
            config.set("Groups." + group + ".Prefix", value);
        } else {
            config.set("Groups." + group + ".Suffix", value);
        }
    }

    private void handlePlayer(YamlConfiguration config, String line) {
        String[] initialSplit = line.split("=");
        String prefix = initialSplit[1].trim().split("\"")[1];
        String[] whiteSpaces = initialSplit[0].trim().split(" ");
        String playerName = whiteSpaces[0];
        String type = whiteSpaces[1];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        String uuid = offlinePlayer.getUniqueId().toString();
        config.set("Players." + uuid + ".Name", playerName);
        config.set("Players." + uuid + "." + type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase(), prefix);
        config.set("Players." + uuid + ".SortPriority", -1);
    }

}