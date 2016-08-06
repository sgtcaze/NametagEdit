package com.nametagedit.plugin.storage.flatfile;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.storage.AbstractConfig;
import com.nametagedit.plugin.storage.data.GroupData;
import com.nametagedit.plugin.storage.data.PlayerData;
import com.nametagedit.plugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FlatFileConfig implements AbstractConfig {

    private File groupsFile;
    private File playersFile;

    private YamlConfiguration groups;
    private YamlConfiguration players;

    private NametagEdit plugin;
    private NametagHandler handler;

    private List<GroupData> groupData = new ArrayList<>();
    private Map<UUID, PlayerData> playerData = new HashMap<>();

    public FlatFileConfig(NametagEdit plugin, List<GroupData> groupData, Map<UUID, PlayerData> playerData, NametagHandler handler) {
        this.plugin = plugin;
        this.handler = handler;
        this.groupData = groupData;
        this.playerData = playerData;
    }

    @Override
    public void load() {
        this.groupsFile = new File(plugin.getDataFolder(), "groups.yml");
        this.groups = Utils.getConfig(groupsFile, "groups.yml", plugin);
        this.playersFile = new File(plugin.getDataFolder(), "players.yml");
        this.players = Utils.getConfig(playersFile, "players.yml", plugin);
        this.loadGroups();
        this.loadPlayers();
        handler.applyTags();
    }

    @Override
    public void reload() {
        groupData.clear();
        playerData.clear();
        load();
        handler.applyTags();
    }

    @Override
    public void shutdown() {
        // NOTE: Nothing to do
    }

    @Override
    public void load(Player player) {
        storeData(player);
        plugin.getHandler().applyTagToPlayer(player);
    }

    @Override
    public void save(PlayerData playerData) {
        UUID uuid = playerData.getUuid();
        String name = playerData.getName();
        players.set("Players." + uuid + ".Name", name);
        players.set("Players." + uuid + ".Prefix", Utils.deformat(playerData.getPrefix()));
        players.set("Players." + uuid + ".Suffix", Utils.deformat(playerData.getSuffix()));
        save(players, playersFile);
    }

    @Override
    public void save(GroupData groupData) {
        groups.set("Groups." + groupData.getGroupName() + ".Permission", groupData.getPermission());
        groups.set("Groups." + groupData.getGroupName() + ".Prefix", Utils.deformat(groupData.getPrefix()));
        groups.set("Groups." + groupData.getGroupName() + ".Suffix", Utils.deformat(groupData.getSuffix()));
        save(groups, groupsFile);
    }

    @Override
    public void delete(GroupData groupData) {
        groups.set("Groups." + groupData.getGroupName(), null);
        save(groups, groupsFile);
    }

    @Override
    public void add(GroupData groupData) {
        // NOTE: Nothing to do
    }

    @Override
    public void clear(UUID uuid, String targetName) {
        playerData.remove(uuid);
        players.set("Players." + uuid.toString(), null);
        save(players, playersFile);
    }

    @Override
    public void orderGroups(CommandSender commandSender, String[] args) {
        // TODO: Not implemented yet
    }

    private void save(YamlConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void storeData(Player player) {
        PlayerData data = PlayerData.fromFile(player.getUniqueId().toString(), players);
        if (data != null) {
            data.setName(player.getName());
            this.playerData.put(player.getUniqueId(), data);
        }
    }

    private void loadPlayers() {
        for (Player player : Utils.getOnline()) {
            storeData(player);
        }
    }

    private void loadGroups() {
        for (String groupName : groups.getConfigurationSection("Groups").getKeys(false)) {
            GroupData data = new GroupData();
            data.setGroupName(groupName);
            data.setPermission(groups.getString("Groups." + groupName + ".Permission", "nte.default"));
            data.setPrefix(groups.getString("Groups." + groupName + ".Prefix", ""));
            data.setSuffix(groups.getString("Groups." + groupName + ".Suffix", ""));
            data.setSortPriority(groups.getInt("Groups." + groupName + ".SortPriority", -1));

            if (data.getSortPriority() < 1) {
                data.setSortPriority(-1); // Quick protection
            }

            data.refresh();
            groupData.add(data);
        }
    }

}