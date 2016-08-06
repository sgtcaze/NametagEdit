package com.nametagedit.plugin;

import com.nametagedit.plugin.api.events.NametagEvent;
import com.nametagedit.plugin.storage.AbstractConfig;
import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.nametagedit.plugin.storage.flatfile.FlatFileConfig;
import com.nametagedit.plugin.utils.UUIDFetcher;
import com.nametagedit.plugin.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@Getter
@Setter
public class NametagHandler implements Listener {

    private AbstractConfig abstractConfig;
    private boolean debug;
    private boolean tabListDisabled;

    private List<GroupData> groupData = new ArrayList<>();
    private Map<UUID, PlayerData> playerData = new HashMap<>();

    private NametagEdit plugin;
    private NametagManager nametagManager;

    public NametagHandler(NametagEdit plugin, NametagManager nametagManager) {
        this.plugin = plugin;
        this.nametagManager = nametagManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.debug = plugin.getConfig().getBoolean("Debug");
        this.tabListDisabled = plugin.getConfig().getBoolean("TabListDisabled");
        if (plugin.getConfig().getBoolean("MySQL.Enabled")) {
            abstractConfig = new DatabaseConfig(plugin, this);
        } else {
            abstractConfig = new FlatFileConfig(plugin, groupData, playerData, this);
        }

        abstractConfig.load();
    }

    boolean debug() {
        return debug;
    }

    void toggleDebug() {
        debug = !debug;
        plugin.getConfig().set("Debug", debug);
        plugin.saveConfig();
    }

    public PlayerData getPlayerData(Player player) {
        if (player == null) return null;
        return playerData.get(player.getUniqueId());
    }

    void save(GroupData data) {
        abstractConfig.save(data);
    }

    void deleteGroup(GroupData data) {
        groupData.remove(data);
        abstractConfig.delete(data);
    }

    void addGroup(GroupData data) {
        groupData.add(data);
        abstractConfig.add(data);
    }

    void reload() {
        plugin.reloadConfig();
        this.debug = plugin.getConfig().getBoolean("Debug");
        this.tabListDisabled = plugin.getConfig().getBoolean("TabListDisabled");
        nametagManager.reset();
        abstractConfig.reload();
    }

    void clear(final CommandSender sender, final String player) {
        Player target = Bukkit.getPlayerExact(player);
        if (target != null) {
            handleClear(target.getUniqueId(), player);
            return;
        }

        UUIDFetcher.lookupUUID(player, plugin, new UUIDFetcher.UUIDLookup() {
            @Override
            public void response(UUID uuid) {
                if (uuid == null) {
                    NametagMessages.UUID_LOOKUP_FAILED.send(sender);
                } else {
                    handleClear(uuid, player);
                }
            }
        });
    }

    void save(final CommandSender sender, String targetName, NametagEvent.ChangeType changeType, String value) {
        Player player = Bukkit.getPlayerExact(targetName);

        PlayerData data = getPlayerData(player);
        if (data == null) {
            data = new PlayerData(targetName, null, "", "");
            if (player != null) {
                playerData.put(player.getUniqueId(), data);
            }
        }

        if (changeType == NametagEvent.ChangeType.PREFIX) {
            data.setPrefix(value);
            nametagManager.setNametag(targetName, Utils.format(value, true), Utils.format(data.getSuffix(), true));
        } else {
            data.setSuffix(value);
            nametagManager.setNametag(targetName, Utils.format(data.getPrefix(), true), Utils.format(value, true));
        }

        if (player != null) {
            data.setUuid(player.getUniqueId());
            abstractConfig.save(data);
            return;
        }

        final PlayerData finalData = data;
        UUIDFetcher.lookupUUID(targetName, plugin, new UUIDFetcher.UUIDLookup() {
            @Override
            public void response(UUID uuid) {
                if (uuid == null) {
                    NametagMessages.UUID_LOOKUP_FAILED.send(sender);
                } else {
                    playerData.put(uuid, finalData);
                    finalData.setUuid(uuid);
                    abstractConfig.save(finalData);
                }
            }
        });
    }

    /**
     * Cleans up any nametag data on the server to prevent memory leaks
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        nametagManager.reset(event.getPlayer().getName());
    }

    /**
     * Applies tags to a player
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        nametagManager.sendTeams(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                abstractConfig.load(player);
            }
        }.runTaskLater(plugin, 1);
    }

    private void handleClear(UUID uuid, String player) {
        playerData.remove(uuid);
        nametagManager.reset(player);
        abstractConfig.clear(uuid, player);
    }

    public void loadDatabaseSettings(HashMap<String, String> settings) {
        String orderSetting = settings.get("order");
        if (orderSetting != null) {
            String[] order = orderSetting.split(" ");
            List<GroupData> current = new ArrayList<>();
            // Determine order for current loaded groups
            for (String group : order) {
                Iterator<GroupData> itr = groupData.iterator();
                while (itr.hasNext()) {
                    GroupData groupData = itr.next();
                    if (groupData.getGroupName().equalsIgnoreCase(group)) {
                        current.add(groupData);
                        itr.remove();
                        break;
                    }
                }
            }
            current.addAll(groupData); // Add remaining entries (bad order, wasn't specified)
            this.groupData = current;
        }
    }

    public void applyTags() {
        for (Player online : Utils.getOnline()) {
            if (online != null) {
                applyTagToPlayer(online);
            }
        }

        plugin.debug("Applied tags to all online players.");
    }

    public void applyTagToPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = playerData.get(uuid);
        if (data != null) {
            nametagManager.setNametag(player.getName(), Utils.format(data.getPrefix(), true), Utils.format(data.getSuffix(), true));
            plugin.debug("Applying PlayerTag to " + player.getName());
        } else {
            for (GroupData group : groupData) {
                if (player.hasPermission(group.getBukkitPermission())) {
                    nametagManager.setNametag(player.getName(), Utils.format(group.getPrefix(), true), Utils.format(group.getSuffix(), true));
                    plugin.debug("Applying GroupTag '" + group.getGroupName() + "' to " + player.getName());
                    break;
                }
            }
        }

        if (tabListDisabled) {
            player.setPlayerListName(Utils.format("&f" + player.getName(), true));
        } else {
            player.setPlayerListName(null);
        }
    }

}
