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

import org.bukkit.Bukkit;

public class VersionChecker {
	
	public static BukkitVersion getBukkitVersion() {
		if(Bukkit.getVersion().contains("(MC: 1.8)") || Bukkit.getVersion().contains("(MC: 1.8.1)") || Bukkit.getVersion().contains("(MC: 1.8.2)")) {
			return BukkitVersion.v1_8_R1;
		} else if(Bukkit.getVersion().contains("(MC: 1.8.3)")) {
			return BukkitVersion.v1_8_R2;
		} else if(Bukkit.getVersion().contains("(MC: 1.8.4)") || Bukkit.getVersion().contains("(MC: 1.8.5)") || Bukkit.getVersion().contains("(MC: 1.8.6)") || Bukkit.getVersion().contains("(MC: 1.8.7)") || Bukkit.getVersion().contains("(MC: 1.8.8)") || Bukkit.getVersion().contains("(MC: 1.8.9)")) {
			return BukkitVersion.v1_8_R3;
		} else if(Bukkit.getVersion().contains("(MC: 1.9)") || Bukkit.getVersion().contains("(MC: 1.9.1)") || Bukkit.getVersion().contains("(MC: 1.9.2)") || Bukkit.getVersion().contains("(MC: 1.9.3)")) {
			return BukkitVersion.v1_9_R1;
		} else if(Bukkit.getVersion().contains("(MC: 1.9.4)")) {
			return BukkitVersion.v1_9_R2;
		} else if(Bukkit.getVersion().contains("(MC: 1.10)") || Bukkit.getVersion().contains("(MC: 1.10.1)") || Bukkit.getVersion().contains("(MC: 1.10.2)")) {
			return BukkitVersion.v1_10_R1;
		} else if(Bukkit.getVersion().contains("(MC: 1.11)") || Bukkit.getVersion().contains("(MC: 1.11.1)") || Bukkit.getVersion().contains("(MC: 1.11.2)")) {
			return BukkitVersion.v1_11_R1;
		} else if(Bukkit.getVersion().contains("(MC: 1.12)") || Bukkit.getVersion().contains("(MC: 1.12.1)") || Bukkit.getVersion().contains("(MC: 1.12.2)")) {
			return BukkitVersion.v1_12_R1;
		} else if(Bukkit.getVersion().contains("(MC: 1.13)")) {
			return BukkitVersion.v1_13_R1;
		} else if(Bukkit.getVersion().contains("(MC: 1.13.1)") || Bukkit.getVersion().contains("(MC: 1.13.2)")) {
			return BukkitVersion.v1_13_R2;
		} else if(Bukkit.getVersion().contains("(MC: 1.14)") || Bukkit.getVersion().contains("(MC: 1.14.1)") || Bukkit.getVersion().contains("(MC: 1.14.2)") || Bukkit.getVersion().contains("(MC: 1.14.3)")) {
			return BukkitVersion.v1_14_R1;
		} else if(Bukkit.getVersion().contains("(MC: 1.14.4)")) {
			return BukkitVersion.v1_14_R2;
		} else if(Bukkit.getVersion().contains("(MC: 1.15)") || Bukkit.getVersion().contains("(MC: 1.15.1)") || Bukkit.getVersion().contains("(MC: 1.15.2)")) {
			return BukkitVersion.v1_15_R1;
		} else if(Bukkit.getVersion().contains("(MC: 1.16.1)")) {
			return BukkitVersion.v1_16_R1;
		} else if(Bukkit.getVersion().contains("(MC: 1.16.2)") || Bukkit.getVersion().contains("(MC: 1.16.3)")) {
			return BukkitVersion.v1_16_R2;
		} else if(Bukkit.getVersion().contains("(MC: 1.16.4)") || Bukkit.getVersion().contains("(MC: 1.16.5)")) {
			return BukkitVersion.v1_16_R3;
		} else {
			return null;
		}
	}
	public static enum BukkitVersion {
		v1_8_R1, v1_8_R2, v1_8_R3, v1_9_R1, v1_9_R2, v1_10_R1, v1_11_R1, v1_12_R1, v1_13_R1, v1_13_R2, v1_14_R1, v1_14_R2, v1_15_R1, v1_16_R1, v1_16_R2, v1_16_R3;
	}
}
