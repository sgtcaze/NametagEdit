package com.nametagedit.plugin;

import com.nametagedit.plugin.api.events.NametagEvent;
import com.nametagedit.plugin.storage.AbstractConfig;
import com.nametagedit.plugin.storage.data.GroupData;
import com.nametagedit.plugin.storage.data.PlayerData;
import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.nametagedit.plugin.storage.database.tasks.GroupConfigUpdater;
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
    private boolean tabListDisabled;
    private boolean fancyMessageCompatible;

    private List<GroupData> groupData = new ArrayList<>();
    private Map<UUID, PlayerData> playerData = new HashMap<>();

    private NametagEdit plugin;

    public NametagHandler(NametagEdit plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.tabListDisabled = plugin.getConfig().getBoolean("TabListDisabled");
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        this.fancyMessageCompatible = version.startsWith("v1_8");
        if (plugin.getConfig().getBoolean("MySQL.Enabled")) {
            abstractConfig = new DatabaseConfig(plugin, this);
        } else {
            abstractConfig = new FlatFileConfig(plugin, groupData, playerData);
        }
        abstractConfig.load();
    }

    public void save(GroupData data) {
        abstractConfig.save(data);
    }

    public void deleteGroup(GroupData data) {
        groupData.remove(data);
        abstractConfig.delete(data);
    }

    public void addGroup(GroupData data) {
        groupData.add(data);
        abstractConfig.add(data);
    }

    public PlayerData getPlayerData(Player player) {
        if (player == null) return null;
        return playerData.get(player.getUniqueId());
    }

    public void reload() {
        plugin.reloadConfig();
        this.tabListDisabled = plugin.getConfig().getBoolean("TabListDisabled");
        plugin.getManager().reset();
        abstractConfig.reload();
    }

    public void clear(final CommandSender sender, final String player) {
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

    public void save(final CommandSender sender, String targetName, NametagEvent.ChangeType changeType, String value) {
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
            plugin.getManager().overlapNametag(targetName, Utils.format(value, true), Utils.format(data.getSuffix(), true));
        } else {
            data.setSuffix(value);
            plugin.getManager().overlapNametag(targetName, Utils.format(data.getPrefix(), true), Utils.format(value, true));
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
        plugin.getManager().reset(event.getPlayer().getName());
        plugin.getManager().clearFromCache(event.getPlayer());
    }

    /**
     * Applies tags to a player
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        plugin.getManager().sendTeams(player);
        plugin.getManager().reset(player.getName());

        new BukkitRunnable() {
            @Override
            public void run() {
                abstractConfig.load(player);
            }
        }.runTaskLater(plugin, 1);
    }

    private void handleClear(UUID uuid, String player) {
        playerData.remove(uuid);
        plugin.getManager().reset(player);
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
    }

    public void applyTagToPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = playerData.get(uuid);
        if (data != null) {
            plugin.getManager().updateNametag(player.getName(), Utils.format(data.getPrefix(), true), Utils.format(data.getSuffix(), true));
        } else {
            for (GroupData group : groupData) {
                System.out.println("Group order: " + group.getGroupName());
                if (player.hasPermission(group.getBukkitPermission())) {
                    plugin.getManager().updateNametag(player.getName(), Utils.format(group.getPrefix(), true), Utils.format(group.getSuffix(), true));
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