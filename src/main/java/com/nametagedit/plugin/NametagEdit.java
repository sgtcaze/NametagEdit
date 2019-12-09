package com.nametagedit.plugin;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nametagedit.plugin.api.INametagApi;
import com.nametagedit.plugin.api.NametagAPI;
import com.nametagedit.plugin.hooks.HookGroupManager;
import com.nametagedit.plugin.hooks.HookGuilds;
import com.nametagedit.plugin.hooks.HookLibsDisguise;
import com.nametagedit.plugin.hooks.HookLuckPerms;
import com.nametagedit.plugin.hooks.HookPermissionsEX;
import com.nametagedit.plugin.hooks.HookZPermissions;
import com.nametagedit.plugin.packets.PacketWrapper;
import lombok.Getter;

/**
 * TODO:
 * - Better uniform message format + more messages
 * - Code cleanup
 * - Add language support
 */
@Getter
public class NametagEdit extends JavaPlugin {

    private static INametagApi api;

    private NametagHandler handler;
    private NametagManager manager;

    public static INametagApi getApi() {
        return api;
    }

    @Override
    public void onEnable() {
        testCompat();
        if (!isEnabled()) return;

        manager = new NametagManager(this);
        handler = new NametagHandler(this, manager);

        PluginManager pluginManager = Bukkit.getPluginManager();
        if (checkShouldRegister("zPermissions")) {
            pluginManager.registerEvents(new HookZPermissions(handler), this);
        } else if (checkShouldRegister("PermissionsEx")) {
            pluginManager.registerEvents(new HookPermissionsEX(handler), this);
        } else if (checkShouldRegister("GroupManager")) {
            pluginManager.registerEvents(new HookGroupManager(handler), this);
        } else if (checkShouldRegister("LuckPerms")) {
            pluginManager.registerEvents(new HookLuckPerms(handler), this);
        }

        if (pluginManager.getPlugin("LibsDisguises") != null) {
            pluginManager.registerEvents(new HookLibsDisguise(this), this);
        }
        if (pluginManager.getPlugin("Guilds") != null) {
            pluginManager.registerEvents(new HookGuilds(handler), this);
        }

        getCommand("ne").setExecutor(new NametagCommand(handler));

        if (api == null) {
            api = new NametagAPI(handler, manager);
        }
    }

    @Override
    public void onDisable() {
        manager.reset();
        handler.getAbstractConfig().shutdown();
    }

    void debug(String message) {
        if (handler != null && handler.debug()) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    private boolean checkShouldRegister(String plugin) {
        if (Bukkit.getPluginManager().getPlugin(plugin) == null) return false;
        getLogger().info("Found " + plugin + "! Hooking in.");
        return true;
    }

    private void testCompat() {
        PacketWrapper wrapper = new PacketWrapper("TEST", "&f", "", 0, new ArrayList<>());
        wrapper.send();
        if (wrapper.error == null) return;
        Bukkit.getPluginManager().disablePlugin(this);
        getLogger().severe(new StringBuilder()
                .append("\n------------------------------------------------------\n")
                .append("[WARNING] NametagEdit v").append(getDescription().getVersion()).append(" Failed to load! [WARNING]")
                .append("\n------------------------------------------------------")
                .append("\nThis might be an issue with reflection. REPORT this:\n> ")
                .append(wrapper.error)
                .append("\nThe plugin will now self destruct.\n------------------------------------------------------")
                .toString());
    }

}