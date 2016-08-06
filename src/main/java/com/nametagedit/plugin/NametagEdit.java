package com.nametagedit.plugin;

import com.nametagedit.plugin.api.INametagApi;
import com.nametagedit.plugin.api.NametagAPI;
import com.nametagedit.plugin.hooks.HookGroupManager;
import com.nametagedit.plugin.hooks.HookPermissionsEX;
import com.nametagedit.plugin.hooks.HookZPermissions;
import com.nametagedit.plugin.packets.PacketWrapper;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;
import java.util.ArrayList;

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
        saveDefaultConfig();
        manager = new NametagManager(this);
        handler = new NametagHandler(this, manager);

        if (getConfig().getBoolean("MetricsEnabled")) {
            try {
                new Metrics(this).start();
            } catch (IOException e) {
                getLogger().severe("Couldn't start Metrics!");
            }
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        if (checkShouldRegister("zPermissions")) {
            pluginManager.registerEvents(new HookZPermissions(handler), this);
        } else if (checkShouldRegister("PermissionsEx")) {
            pluginManager.registerEvents(new HookPermissionsEX(handler), this);
        } else if (checkShouldRegister("GroupManager")) {
            pluginManager.registerEvents(new HookGroupManager(handler), this);
        }

        getCommand("ne").setExecutor(new NametagCommand(handler));

        if (api == null) {
            api = new NametagAPI(manager);
        }

        testCompat();
    }

    @Override
    public void onDisable() {
        manager.reset();
        handler.getAbstractConfig().shutdown();
    }

    public void debug(String message) {
        if (handler != null && handler.debug()) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    private boolean checkShouldRegister(String plugin) {
        if (Bukkit.getPluginManager().getPlugin(plugin) == null) {
            return false;
        }
        getLogger().info("Found " + plugin + "! Hooking in.");
        return true;
    }

    private void testCompat() {
        PacketWrapper wrapper = new PacketWrapper("TEST", "&f", "", 0, new ArrayList<>());
        wrapper.send();
        if (wrapper.error == null) return;
        getLogger().severe("\n------------------------------------------------------\n" +
                "[WARNING] NametagEdit v" + getDescription().getVersion() + " Failed to load! [WARNING]" +
                "\n------------------------------------------------------" +
                "\nThis might be an issue with reflection. REPORT this:" +
                "\n> " + wrapper.error +
                "\nThe plugin will now self destruct.\n------------------------------------------------------");
        Bukkit.getPluginManager().disablePlugin(this);
    }

}