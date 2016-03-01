package com.nametagedit.plugin.utils;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Utils {

    private static Method getHandle;
    private static Method sendPacket;
    private static Field playerConnection;

    static {
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> typeNMSPlayer = Class.forName("net.minecraft.server." + version + ".EntityPlayer");
            Class<?> typeCraftPlayer = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
            Class<?> typePlayerConnection = Class.forName("net.minecraft.server." + version + ".PlayerConnection");
            getHandle = typeCraftPlayer.getMethod("getHandle");
            playerConnection = typeNMSPlayer.getField("playerConnection");
            sendPacket = typePlayerConnection.getMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet"));
        } catch (Exception e) {
            Bukkit.getLogger().severe("NametagEdit could not setup reflection! What happened???");
            e.printStackTrace();
        }
    }

    public static void sendPacket(Collection<? extends Player> players, Object packet) {
        for (Player player : players) {
            sendPacket(player, packet);
        }
    }

    public static void sendPacket(Player player, Object packet) {
        try {
            Object nmsPlayer = getHandle.invoke(player);
            Object connection = playerConnection.get(nmsPlayer);
            sendPacket.invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        String colored = ChatColor.translateAlternateColorCodes('&', input);
        return limitChars && colored.length() > 16 ? colored.substring(0, 16) : colored;
    }

    // Workaround for the deprecated getOnlinePlayers()
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
            return YamlConfiguration.loadConfiguration(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

}