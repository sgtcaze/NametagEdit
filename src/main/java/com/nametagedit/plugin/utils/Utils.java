package com.nametagedit.plugin.utils;

import com.nametagedit.plugin.packets.VersionChecker;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String format(String[] text, int to, int from) {
        return StringUtils.join(text, ' ', to, from).replace("'", "");
    }

    public static String deformat(String input) {
        return input.replace("§", "&");
    }

    public static String format(String input) {
        return format(input, false);
    }

    public static String format(String input, boolean limitChars) {
        String colored = color(input);

        switch (VersionChecker.getBukkitVersion()) {
            case v1_8_R1: case v1_8_R2: case v1_8_R3: case v1_9_R1: case v1_9_R2:
            case v1_10_R1: case v1_11_R1: case v1_12_R1:
                return limitChars && colored.length() > 16 ? colored.substring(0, 16) : colored;
            default:
                return limitChars && colored.length() > 256 ? colored.substring(0, 256) : colored;
        }
    }

    public static String color(String text) {
        if (text == null) return "";

        text = ChatColor.translateAlternateColorCodes('&', text);

        if (VersionChecker.canHex()) {
            final char colorChar = ChatColor.COLOR_CHAR;

            final Matcher matcher = hexPattern.matcher(text);
            final StringBuffer buffer = new StringBuffer(text.length() + 4 * 8);

            while (matcher.find()) {
                final String group = matcher.group(1);

                matcher.appendReplacement(buffer, colorChar + "x"
                        + colorChar + group.charAt(0) + colorChar + group.charAt(1)
                        + colorChar + group.charAt(2) + colorChar + group.charAt(3)
                        + colorChar + group.charAt(4) + colorChar + group.charAt(5));
            }

            text = matcher.appendTail(buffer).toString();
        }

        return text;
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
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}