/*
 * All rights by DomeDD (2018)
 * You are allowed to modify this code
 * You are allowed to use this code in your plugins for private projects
 * You are allowed to publish your plugin including this code as long as your plugin is for free and as long as you mention me (DomeDD)
 * You are NOT allowed to claim this plugin (BetterNick) as your own
 * You are NOT allowed to publish this plugin (BetterNick) or your modified version of this plugin (BetterNick)
 *
 */
package com.nametagedit.plugin.packets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

public class VersionChecker {

    private static BukkitVersion bukkitVersion;

    static {
        try{
            bukkitVersion = BukkitVersion.valueOf(Bukkit.getServer().getClass().getPackage().getName().replace("org.bukkit.craftbukkit.", ""));
        }catch (Exception ex){
            bukkitVersion = null;
        }
    }

    public static BukkitVersion getBukkitVersion() {
        return bukkitVersion;
    }

    public static boolean canHex() {
        return bukkitVersion.getProtocolNumber() >= BukkitVersion.v1_16_R1.getProtocolNumber();
    }

    @RequiredArgsConstructor
    @Getter
    public enum BukkitVersion {
        v1_8_R1(47),
        v1_8_R2(47),
        v1_8_R3(47),
        v1_9_R1(107),
        v1_9_R2(108),
        v1_10_R1(210),
        v1_11_R1(316),
        v1_12_R1(340),
        v1_13_R1(393),
        v1_13_R2(404),
        v1_14_R1(477),
        v1_14_R2(498),
        v1_15_R1(578),
        v1_16_R1(736),
        v1_16_R2(754),
        v1_17_R1(756),
        v1_18_R1(758),
        v1_19_R1(760);

        private final int protocolNumber;
    }

}
