package com.nametagedit.plugin;

import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.api.events.NametagEvent;
import com.nametagedit.plugin.storage.AbstractConfig;
import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.nametagedit.plugin.storage.flatfile.FlatFileConfig;
import com.nametagedit.plugin.utils.Configuration;
import com.nametagedit.plugin.utils.UUIDFetcher;
import com.nametagedit.plugin.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

@Getter
@Setter
public class NametagHandler implements Listener {

    public static boolean DISABLE_PUSH_ALL_TAGS = false;
    private boolean debug;
    private boolean tabListDisabled;
    // This should only be changed in the code
    private int databaseVersion = 3;

    private BukkitTask clearEmptyTeamTask;
    private AbstractConfig abstractConfig;

    private Configuration config;

    private List<GroupData> groupData = new ArrayList<>();
    private Map<UUID, PlayerData> playerData = new HashMap<>();

    private NametagEdit plugin;
    private NametagManager nametagManager;

    public NametagHandler(Configuration config, NametagEdit plugin, NametagManager nametagManager) {
        this.config = config;
        this.plugin = plugin;
        this.nametagManager = nametagManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.debug = config.getBoolean("Debug");
        this.tabListDisabled = config.getBoolean("TabListDisabled");
        DISABLE_PUSH_ALL_TAGS = config.getBoolean("DisablePush");
        clearEmptyTeamTask = clearTeamInterval();

        if (config.getBoolean("MySQL.Enabled")) {
            abstractConfig = new DatabaseConfig(plugin, this, config);
        } else {
            abstractConfig = new FlatFileConfig(plugin, groupData, playerData, this);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                abstractConfig.load();
            }
        }.runTaskAsynchronously(plugin);
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
        }.runTaskLaterAsynchronously(plugin, 1);
    }

    private void handleClear(UUID uuid, String player) {
        playerData.remove(uuid);
        nametagManager.reset(player);
        abstractConfig.clear(uuid, player);
    }

    // ==========================================
    // Below are methods used by the API/Commands
    // ==========================================
    boolean debug() {
        return debug;
    }

    void toggleDebug() {
        debug = !debug;
        config.set("Debug", debug);
        config.save();
    }

    /**
     * Replaces placeholders when a player tag is created.
     * Maxim and Clip's plugins are searched for, and input
     * is replaced. We use direct imports to avoid any problems!
     * (So don't change that)
     */
    public String formatWithPlaceholders(Player player, String input) {
        if (input == null) return "";
        if (player == null) return input;

        if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            plugin.debug("Trying to use MVdWPlaceholderAPI for placeholders");
            input = be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(player, input);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            plugin.debug("Trying to use PlaceholderAPI for placeholders");
            input = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, input);
        }

        return Utils.format(input, true);
    }

    public PlayerData getPlayerData(Player player) {
        return player == null ? null : playerData.get(player.getUniqueId());
    }

    public GroupData getGroupData(String key) {
        for (GroupData groupData : getGroupData()) {
            if (groupData.getGroupName().equalsIgnoreCase(key)) {
                return groupData;
            }
        }

        return null;
    }

    void save(GroupData data) {
        abstractConfig.save(data);
    }

    void addGroup(GroupData data) {
        groupData.add(data);
        abstractConfig.add(data);
    }

    void deleteGroup(GroupData data) {
        groupData.remove(data);
        abstractConfig.delete(data);
    }

    private BukkitTask clearTeamInterval() {
        int clearInterval = config.getInt("ClearEmptyTeamsInterval", -1);
        if (clearInterval > 0) {
            return Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nte teams clear");
                }
            }, 0, 20 * clearInterval);
        }
        return null;
    }

    void reload() {
        config.reload(true);
        this.debug = config.getBoolean("Debug");
        this.tabListDisabled = config.getBoolean("TabListDisabled");
        DISABLE_PUSH_ALL_TAGS = config.getBoolean("DisablePush");
        nametagManager.reset();
        abstractConfig.reload();

        if (clearEmptyTeamTask != null) {
            clearEmptyTeamTask.cancel();
        }

        clearEmptyTeamTask = clearTeamInterval();
    }

    /**
     * This method normalizes the SortPriority defined by the user.
     * By adjusting the SortPriority, we can control for abnormal
     * input that would affect the Team Names (which allows us to
     * sort nametags in the tab to begin with)
     */
    public void adjustSortPriority() {
        List<GroupData> copyOfGroups = new ArrayList<>(groupData);
        Collections.sort(copyOfGroups, new Comparator<GroupData>() {
            @Override
            public int compare(GroupData group1, GroupData group2) {
                return group1.getSortPriority() - group2.getSortPriority();
            }
        });

        int adjustedSortPriority = 1;

        for (GroupData groupData : copyOfGroups) {
            groupData.setSortPriority(groupData.getSortPriority() < 1 ? -1 : adjustedSortPriority++);
        }

        for (GroupData groupData : copyOfGroups) {
            save(groupData);
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

    public void applyTagToPlayer(final Player player) {
        if (!Bukkit.isPrimaryThread()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    applyTagToPlayer(player);
                }
            }.runTask(plugin);
            return;
        }

        UUID uuid = player.getUniqueId();
        PlayerData data = playerData.get(uuid);

        if (data != null) {
            nametagManager.setNametag(player.getName(), formatWithPlaceholders(player, data.getPrefix()), formatWithPlaceholders(player, data.getSuffix()), data.getSortPriority(), true);
            plugin.debug("Applying PlayerTag to " + player.getName());
        } else {
            // This may seem strange, but the hasPermission operation can cause major
            // slowdown depending on server version and plugins. This will select the
            // group async, and apply the nametag sync. Perhaps in the future we will
            // consider using Locks in order to prevent any concurrency issues.
            //
            // TODO: Cleanup this context switching and use locks + reduce clutter
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (GroupData group : groupData) {
                        if (player.hasPermission(group.getBukkitPermission())) {
                            handleSync(player, group);
                            break;
                        }
                    }
                }
            }.runTaskAsynchronously(plugin);
        }

        player.setPlayerListName(tabListDisabled ? player.getPlayerListName() : null);
    }

    private void handleSync(final Player player, final GroupData group) {
        new BukkitRunnable() {
            @Override
            public void run() {
                nametagManager.setNametag(player.getName(), formatWithPlaceholders(player, group.getPrefix()), formatWithPlaceholders(player, group.getSuffix()), group.getSortPriority());
                plugin.debug("Applying GroupTag '" + group.getGroupName() + "' to " + player.getName());
            }
        }.runTask(plugin);
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

    void save(CommandSender sender, boolean playerTag, String key, int priority) {
        if (playerTag) {
            Player player = Bukkit.getPlayerExact(key);

            PlayerData data = getPlayerData(player);
            if (data == null) {
                abstractConfig.savePriority(true, key, priority);
                return;
            }

            data.setSortPriority(priority);
            abstractConfig.save(data);
        } else {
            GroupData groupData = getGroupData(key);

            if (groupData == null) {
                sender.sendMessage(ChatColor.RED + "Group " + key + " does not exist!");
                return;
            }

            groupData.setSortPriority(priority);
            abstractConfig.save(groupData);
        }
    }

    void save(final CommandSender sender, String targetName, NametagEvent.ChangeType changeType, String value) {
        Player player = Bukkit.getPlayerExact(targetName);

        PlayerData data = getPlayerData(player);
        if (data == null) {
            data = new PlayerData(targetName, null, "", "", -1);
            if (player != null) {
                playerData.put(player.getUniqueId(), data);
            }
        }

        if (changeType == NametagEvent.ChangeType.PREFIX) {
            data.setPrefix(value);
        } else {
            data.setSuffix(value);
        }

        if (player != null) {
            applyTagToPlayer(player);
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

}