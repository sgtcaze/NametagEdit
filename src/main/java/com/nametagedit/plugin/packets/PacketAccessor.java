package com.nametagedit.plugin.packets;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;

public class PacketAccessor {

    static Field MEMBERS;
    static Field PREFIX;
    static Field SUFFIX;
    static Field TEAM_NAME;
    static Field PARAM_INT;
    static Field PACK_OPTION;
    static Field DISPLAY_NAME;

    private static Class<?> packetClass;

    static {
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            String className = version.startsWith("v1_5") ? "Packet209SetScoreboardTeam" : "PacketPlayOutScoreboardTeam";
            packetClass = Class.forName("net.minecraft.server." + version + "." + className);

            PacketData currentVersion = null;
            for (PacketData packetData : PacketData.values()) {
                if (version.contains(packetData.name())) {
                    currentVersion = packetData;
                }
            }

            PREFIX = getNMS(currentVersion.getPrefix());
            SUFFIX = getNMS(currentVersion.getSuffix());
            MEMBERS = getNMS(currentVersion.getMembers());
            TEAM_NAME = getNMS(currentVersion.getTeamName());
            PARAM_INT = getNMS(currentVersion.getParamInt());
            PACK_OPTION = getNMS(currentVersion.getPackOption());
            DISPLAY_NAME = getNMS(currentVersion.getDisplayName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Field getNMS(String path) throws Exception {
        Field field = packetClass.getDeclaredField(path);
        field.setAccessible(true);
        return field;
    }

    public static Object createPacket() {
        try {
            return packetClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}