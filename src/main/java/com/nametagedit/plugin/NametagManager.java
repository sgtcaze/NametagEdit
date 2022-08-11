package com.nametagedit.plugin;

import com.nametagedit.plugin.api.data.FakeTeam;
import com.nametagedit.plugin.packets.PacketWrapper;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class NametagManager {

    private final Map<String, FakeTeam> TEAMS = new ConcurrentHashMap<>();
    private final Map<String, FakeTeam> CACHED_FAKE_TEAMS = new ConcurrentHashMap<>();
    private final NametagEdit plugin;

    /**
     * Gets the current team given a prefix and suffix
     * If there is no team similar to this, then a new
     * team is created.
     */
    private FakeTeam getFakeTeam(String prefix, String suffix, boolean visible) {
        return TEAMS.values().stream().filter(fakeTeam -> fakeTeam.isSimilar(prefix, suffix, visible)).findFirst().orElse(null);
    }

    /**
     * Adds a player to a FakeTeam. If they are already on this team,
     * we do NOT change that.
     */
    private void addPlayerToTeam(String player, String prefix, String suffix, int sortPriority, boolean playerTag, boolean visible) {
        FakeTeam previous = getFakeTeam(player);

        if (previous != null && previous.isSimilar(prefix, suffix, visible)) {
            plugin.debug(player + " already belongs to a similar team (" + previous.getName() + ")");
            return;
        }

        reset(player);

        FakeTeam joining = getFakeTeam(prefix, suffix, visible);
        if (joining != null) {
            joining.addMember(player);
            plugin.debug("Using existing team for " + player);
        } else {
            joining = new FakeTeam(prefix, suffix, sortPriority, playerTag);
            joining.setVisible(visible);
            joining.addMember(player);
            TEAMS.put(joining.getName(), joining);
            addTeamPackets(joining);
            plugin.debug("Created FakeTeam " + joining.getName() + ". Size: " + TEAMS.size());
        }

        Player adding = Bukkit.getPlayerExact(player);
        if (adding != null) {
            addPlayerToTeamPackets(joining, adding.getName());
            cache(adding.getName(), joining);
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
            addPlayerToTeamPackets(joining, offlinePlayer.getName());
            cache(offlinePlayer.getName(), joining);
        }

        plugin.debug(player + " has been added to team " + joining.getName());
    }

    public FakeTeam reset(String player) {
        return reset(player, decache(player));
    }

    private FakeTeam reset(String player, FakeTeam fakeTeam) {
        if (fakeTeam != null && fakeTeam.getMembers().remove(player)) {
            boolean delete;
            Player removing = Bukkit.getPlayerExact(player);
            if (removing != null) {
                delete = removePlayerFromTeamPackets(fakeTeam, removing.getName());
            } else {
                OfflinePlayer toRemoveOffline = Bukkit.getOfflinePlayer(player);
                delete = removePlayerFromTeamPackets(fakeTeam, toRemoveOffline.getName());
            }

            plugin.debug(player + " was removed from " + fakeTeam.getName());
            if (delete) {
                removeTeamPackets(fakeTeam);
                TEAMS.remove(fakeTeam.getName());
                plugin.debug("FakeTeam " + fakeTeam.getName() + " has been deleted. Size: " + TEAMS.size());
            }
        }

        return fakeTeam;
    }

    // ==============================================================
    // Below are public methods to modify the cache
    // ==============================================================
    private FakeTeam decache(String player) {
        return CACHED_FAKE_TEAMS.remove(player);
    }

    public FakeTeam getFakeTeam(String player) {
        return CACHED_FAKE_TEAMS.get(player);
    }

    private void cache(String player, FakeTeam fakeTeam) {
        CACHED_FAKE_TEAMS.put(player, fakeTeam);
    }

    // ==============================================================
    // Below are public methods to modify certain data
    // ==============================================================
    public void setNametag(String player, String prefix, String suffix) {
        setNametag(player, prefix, suffix, -1);
    }

    public void setNametag(String player, String prefix, String suffix, boolean visible) {
        setNametag(player, prefix, suffix, -1, false, visible);
    }

    void setNametag(String player, String prefix, String suffix, int sortPriority) {
        setNametag(player, prefix, suffix, sortPriority, false, true);
    }

    void setNametag(String player, String prefix, String suffix, int sortPriority, boolean playerTag, boolean visible) {
        addPlayerToTeam(player, prefix != null ? prefix : "", suffix != null ? suffix : "", sortPriority, playerTag, visible);
    }

    void sendTeams(Player player) {
        for (FakeTeam fakeTeam : TEAMS.values()) {
            new PacketWrapper(fakeTeam.getName(), fakeTeam.getPrefix(), fakeTeam.getSuffix(), 0, fakeTeam.getMembers(), fakeTeam.isVisible()).send(player);
        }
    }

    void reset() {
        for (FakeTeam fakeTeam : TEAMS.values()) {
            removePlayerFromTeamPackets(fakeTeam, fakeTeam.getMembers());
            removeTeamPackets(fakeTeam);
        }
        CACHED_FAKE_TEAMS.clear();
        TEAMS.clear();
    }

    // ==============================================================
    // Below are private methods to construct a new Scoreboard packet
    // ==============================================================
    private void removeTeamPackets(FakeTeam fakeTeam) {
        new PacketWrapper(fakeTeam.getName(), fakeTeam.getPrefix(), fakeTeam.getSuffix(), 1, new ArrayList<>(), fakeTeam.isVisible()).send();
    }

    private boolean removePlayerFromTeamPackets(FakeTeam fakeTeam, String... players) {
        return removePlayerFromTeamPackets(fakeTeam, Arrays.asList(players));
    }

    private boolean removePlayerFromTeamPackets(FakeTeam fakeTeam, List<String> players) {
        new PacketWrapper(fakeTeam.getName(), 4, players).send();
        fakeTeam.getMembers().removeAll(players);
        return fakeTeam.getMembers().isEmpty();
    }

    private void addTeamPackets(FakeTeam fakeTeam) {
        new PacketWrapper(fakeTeam.getName(), fakeTeam.getPrefix(), fakeTeam.getSuffix(), 0, fakeTeam.getMembers(), fakeTeam.isVisible()).send();
    }

    private void addPlayerToTeamPackets(FakeTeam fakeTeam, String player) {
        new PacketWrapper(fakeTeam.getName(), 3, Collections.singletonList(player)).send();
    }

}