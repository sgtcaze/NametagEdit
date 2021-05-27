package com.nametagedit.plugin.utils;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.nametagedit.plugin.packets.VersionChecker;
import com.nametagedit.plugin.packets.VersionChecker.BukkitVersion;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String format(String[] text, int to, int from) {
        return StringUtils.join(text, ' ', to, from).replace("'", "");
    }

    public static String deformat(String input) {
        return ChatColor.stripColor(input);
    }

    public static String format(String input) {
        return format(input, false);
    }

    public static String format(String input, boolean limitChars) {
        String colored = ChatColor.translateAlternateColorCodes('&', input);

        if(VersionChecker.getBukkitVersion() == BukkitVersion.v1_13_R1) {
            return limitChars && colored.length() > 128 ? colored.substring(0, 128) : colored;
        } else if(VersionChecker.getBukkitVersion() == BukkitVersion.v1_14_R1) {
            return limitChars && colored.length() > 128 ? colored.substring(0, 128) : colored;
        } else if(VersionChecker.getBukkitVersion() == BukkitVersion.v1_14_R2) {
            return limitChars && colored.length() > 128 ? colored.substring(0, 128) : colored;
        } else if(VersionChecker.getBukkitVersion() == BukkitVersion.v1_15_R1) {
            return limitChars && colored.length() > 128 ? colored.substring(0, 128) : colored;
        } else if(VersionChecker.getBukkitVersion() == BukkitVersion.v1_16_R1) {
            return limitChars && colored.length() > 128 ? colored.substring(0, 128) : colored;
        } else if(VersionChecker.getBukkitVersion() == BukkitVersion.v1_16_R2) {
            return limitChars && colored.length() > 128 ? colored.substring(0, 128) : colored;
        } else if(VersionChecker.getBukkitVersion() == BukkitVersion.v1_16_R3) {
            return limitChars && colored.length() > 128 ? colored.substring(0, 128) : colored;
        } else {
            return limitChars && colored.length() > 16 ? colored.substring(0, 16) : colored;
        }
    }

    public final static char COLOR_CHAR = ChatColor.COLOR_CHAR;

    // Colorizes messages with preset colorcodes (&) and if using 1.16, applies hex values via "&#hexvalue"
    public static String colorize(String input) {

        // Apply preset colorcodes
        input = ChatColor.translateAlternateColorCodes('&', input);

        // Apply hex values
        input = translateHexColorCodes(input);

        // Return modified input.
        return input;
    }

    public static String translateHexColorCodes(String message) {
        final Pattern hexPattern = Pattern.compile("\\&#" + "([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x" + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1) + COLOR_CHAR
                    + group.charAt(2) + COLOR_CHAR + group.charAt(3) + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5));
        }
        return matcher.appendTail(buffer).toString();
    }

    public static List<Player> getOnline() {
        List<Player> list = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            list.addAll(world.getPlayers());
        }

        return Collections.unmodifiableList(list);
    }

    public static YamlConfiguration getConfig(File file) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public static YamlConfiguration getConfig(File file, String resource, Plugin plugin) {
        try {
            if (!file.exists()) {
                file.createNewFile();
                InputStream inputStream = plugin.getResource(resource);
                OutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                outputStream.flush();
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public static String generateUUID() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            builder.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return builder.toString();
    }

}
