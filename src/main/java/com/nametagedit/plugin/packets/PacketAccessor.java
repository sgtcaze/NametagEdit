package com.nametagedit.plugin.packets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import net.minecraft.server.v1_7_R4.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import net.minecraft.server.v1_7_R4.Packet;

class PacketAccessor {
    
    private static boolean cauldron = false;

    static Field MEMBERS;
    static Field PREFIX;
    static Field SUFFIX;
    static Field TEAM_NAME;
    static Field PARAM_INT;
    static Field PACK_OPTION;
    static Field DISPLAY_NAME;
    static Field PUSH;
    static Field VISIBILITY;

    private static Method getHandle;
    private static Method sendPacket;
    private static Field playerConnection;

    private static Class<?> packetClass;

    static {
        try {
            Class.forName("cpw.mods.fml.common.Mod");
            cauldron = true;
        } catch (ClassNotFoundException e) {
            ;
        }
        
        try {
            if (cauldron) {
                String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                packetClass = net.minecraft.server.v1_7_R4.PacketPlayOutScoreboardTeam.class;

                Class<?> typeNMSPlayer = net.minecraft.server.v1_7_R4.EntityPlayer.class;
                Class<?> typeCraftPlayer = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
                Class<?> typePlayerConnection =  net.minecraft.server.v1_7_R4.PlayerConnection.class;
                getHandle = typeCraftPlayer.getMethod("getHandle");
                playerConnection = typeNMSPlayer.getField("field_71135_a");
                sendPacket = typePlayerConnection.getMethod("func_147359_a", net.minecraft.server.v1_7_R4.Packet.class);
            } else {
                String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                packetClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutScoreboardTeam");

                Class<?> typeNMSPlayer = Class.forName("net.minecraft.server." + version + ".EntityPlayer");
                Class<?> typeCraftPlayer = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
                Class<?> typePlayerConnection = Class.forName("net.minecraft.server." + version + ".PlayerConnection");
                getHandle = typeCraftPlayer.getMethod("getHandle");
                playerConnection = typeNMSPlayer.getField("playerConnection");
                sendPacket = typePlayerConnection.getMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet"));
            }
            

            PacketData currentVersion = null;
            for (PacketData packetData : PacketData.values()) {
                if (version.contains(packetData.name())) {
                    currentVersion = packetData;
                }
            }
            if (cauldron) 
                currentVersion = PacketData.cauldron;

            if (currentVersion != null) {
                PREFIX = getNMS(currentVersion.getPrefix());
                SUFFIX = getNMS(currentVersion.getSuffix());
                MEMBERS = getNMS(currentVersion.getMembers());
                TEAM_NAME = getNMS(currentVersion.getTeamName());
                PARAM_INT = getNMS(currentVersion.getParamInt());
                PACK_OPTION = getNMS(currentVersion.getPackOption());
                DISPLAY_NAME = getNMS(currentVersion.getDisplayName());

                if (isPushVersion(version)) {
                    PUSH = getNMS(currentVersion.getPush());
                }

                if (isVisibilityVersion(version)) {
                    VISIBILITY = getNMS(currentVersion.getVisibility());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isPushVersion(String version) {
        return Integer.parseInt(version.split("_")[1]) >= 9;
    }

    private static boolean isVisibilityVersion(String version) {
        return Integer.parseInt(version.split("_")[1]) >= 8;
    }

    private static Field getNMS(String path) throws Exception {
        Field field = packetClass.getDeclaredField(path);
        field.setAccessible(true);
        return field;
    }

    static Object createPacket() {
        try {
            return packetClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static void sendPacket(Collection<? extends Player> players, Object packet) {
        for (Player player : players) {
            sendPacket(player, packet);
        }
    }

    static void sendPacket(Player player, Object packet) {
        try {
            Object nmsPlayer = getHandle.invoke(player);
            Object connection = playerConnection.get(nmsPlayer);
            sendPacket.invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
