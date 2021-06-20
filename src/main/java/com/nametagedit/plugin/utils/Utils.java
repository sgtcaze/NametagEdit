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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final char COLOR_CHAR = '\u00A7';
    private static final Pattern hexPattern = Pattern.compile("&#[A-Fa-f0-9]{6}");

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
        String colored = color(input);

        switch (VersionChecker.getBukkitVersion()) {
            case v1_13_R1: case v1_14_R1: case v1_14_R2: case v1_15_R1: case v1_16_R1:
            case v1_16_R2: case v1_16_R3: case v1_17_R1:
                return limitChars && colored.length() > 128 ? colored.substring(0, 128) : colored;
            default:
                return limitChars && colored.length() > 16 ? colored.substring(0, 16) : colored;
        }
    }

    public static String color(String text) {
        if (text == null) return "";

        switch (VersionChecker.getBukkitVersion()) {
            case v1_16_R1: case v1_16_R2: case v1_16_R3:
                final Matcher matcher = hexPattern.matcher(text);
                while (matcher.find()) {
                    final String color = text.substring(matcher.start(), matcher.end()); // ex: &#1258DA
                    text = text.replace(color, net.md_5.bungee.api.ChatColor.of(color.substring(1)).toString());
                }
                break;
        }

        char[] b = text.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
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