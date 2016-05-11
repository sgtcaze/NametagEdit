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

    private List<String> getLines(CommandSender commandSender, Plugin plugin, String oldFileName) throws IOException {
        File oldFile = new File(plugin.getDataFolder(), oldFileName);
        if (!oldFile.exists()) {
            NametagMessages.FILE_DOESNT_EXIST.send(commandSender, oldFileName);
            return new ArrayList<>();
        }

        return getLines(oldFile);
    }

    public void legacyConversion(CommandSender sender, Plugin plugin) {
        try {
            File groupsFile = new File(plugin.getDataFolder(), "groups.yml");
            File playersFile = new File(plugin.getDataFolder(), "players.yml");
            YamlConfiguration groups = Utils.getConfig(groupsFile);
            YamlConfiguration players = Utils.getConfig(playersFile);

            for (String line : getLines(sender, plugin, "groups.txt")) {
                if (line.contains("=")) {
                    handleGroup(groups, line);
                }
            }

            for (String line : getLines(sender, plugin, "players.txt")) {
                if (line.contains("=")) {
                    handlePlayer(players, line);
                }
            }

            players.save(playersFile);
            groups.save(groupsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if (type.equals("prefix")) {
            config.set("Groups." + group + ".Prefix", value);
        } else {
            config.set("Groups." + group + ".Suffix", value);
        }
    }

    private void handlePlayer(YamlConfiguration config, String line) {
        String[] split = line.split("=");
        String prefix = split[1].trim().split("\"")[1];
        String[] splot = split[0].trim().split(" ");
        String playername = splot[0];
        String type = splot[1];
        OfflinePlayer op = Bukkit.getOfflinePlayer(playername);
        String uuid = op.getUniqueId().toString();
        config.set("Players." + uuid + ".Name", playername);
        config.set("Players." + uuid + "." + type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase(), prefix);
    }

}