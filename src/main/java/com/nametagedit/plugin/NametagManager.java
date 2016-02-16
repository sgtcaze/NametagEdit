package com.nametagedit.plugin;

import com.nametagedit.plugin.packets.PacketWrapper;
import com.nametagedit.plugin.storage.data.FakeTeam;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class NametagManager {

    private int UNIQUE_KEY = 0;
    private static final String IDENTIFIER = "ID";

    private final HashMap<String, FakeTeam> CACHED_FAKE_TEAMS = new HashMap<>();
    private final HashMap<FakeTeam, List<String>> FAKE_TEAMS = new HashMap<>();

    public void clearFromCache(Player player) {
        CACHED_FAKE_TEAMS.remove(player.getName());
    }

    public FakeTeam getFakeTeam(String prefix, String suffix) {
        for (FakeTeam fakeTeam : FAKE_TEAMS.keySet()) {
            if (fakeTeam.getPrefix().equals(prefix) && fakeTeam.getSuffix().equals(suffix)) {
                return fakeTeam;
            }
        }
        return createFakeTeam(IDENTIFIER + ++UNIQUE_KEY, prefix, suffix);
    }

    public FakeTeam createFakeTeam(String teamName, String prefix, String suffix) {
        FakeTeam fakeTeam = new FakeTeam(teamName, prefix, suffix);
        FAKE_TEAMS.put(fakeTeam, new ArrayList<String>());
        addTeamPackets(fakeTeam);
        return fakeTeam;
    }

    public String getPrefix(String player) {
        FakeTeam fakeTeam = CACHED_FAKE_TEAMS.get(player);
        return fakeTeam == null ? "" : fakeTeam.getPrefix();
    }

    public String getSuffix(String player) {
        FakeTeam fakeTeam = CACHED_FAKE_TEAMS.get(player);
        return fakeTeam == null ? "" : fakeTeam.getSuffix();
    }

    public void reset() {
        Iterator<Map.Entry<FakeTeam, List<String>>> iterator = FAKE_TEAMS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<FakeTeam, List<String>> entry = iterator.next();
            removePlayerFromTeamPackets(entry.getKey(), entry.getValue());
            removeTeamPackets(entry.getKey());
            iterator.remove();
        }
    }

    public void addPlayerToTeam(String player, final FakeTeam fakeTeam) {
        FakeTeam previous = CACHED_FAKE_TEAMS.get(player);
        List<String> members = FAKE_TEAMS.get(fakeTeam);
        if (previous != null && previous.isSimilar(fakeTeam) && members != null && members.contains(player)) {
            return;
        }

        reset(player, CACHED_FAKE_TEAMS.remove(player));
        if (members != null) {
            members.add(player);
            Player adding = Bukkit.getPlayerExact(player);
            if (adding != null) {
                addPlayerToTeamPackets(fakeTeam, adding.getName());
                CACHED_FAKE_TEAMS.put(adding.getName(), fakeTeam);
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
                addPlayerToTeamPackets(fakeTeam, offlinePlayer.getName());
                CACHED_FAKE_TEAMS.put(offlinePlayer.getName(), fakeTeam);
            }
        }
    }

    public FakeTeam reset(String player) {
        return reset(player, CACHED_FAKE_TEAMS.get(player));
    }

    public FakeTeam reset(String player, FakeTeam fakeTeam) {
        List<String> members = FAKE_TEAMS.get(fakeTeam);
        if (members != null && members.remove(player)) {
            boolean delete;
            Player removing = Bukkit.getPlayerExact(player);
            if (removing != null) {
                delete = removePlayerFromTeamPackets(fakeTeam, removing.getName());
            } else {
                OfflinePlayer toRemoveOffline = Bukkit.getOfflinePlayer(player);
                delete = removePlayerFromTeamPackets(fakeTeam, toRemoveOffline.getName());
            }

            if (delete) {
                removeTeamPackets(fakeTeam);
                FAKE_TEAMS.remove(fakeTeam);
            }
        }
        return fakeTeam;
    }

    public void removeTeamPackets(FakeTeam fakeTeam) {
        new PacketWrapper(fakeTeam.getName(), fakeTeam.getPrefix(), fakeTeam.getSuffix(), 1, new ArrayList<>()).send();
    }

    public boolean removePlayerFromTeamPackets(FakeTeam fakeTeam, String... players) {
        return removePlayerFromTeamPackets(fakeTeam, Arrays.asList(players));
    }

    public boolean removePlayerFromTeamPackets(FakeTeam fakeTeam, List<String> players) {
        new PacketWrapper(fakeTeam.getName(), 4, players).send();
        List<String> members = FAKE_TEAMS.get(fakeTeam);
        members.removeAll(players);
        return members.isEmpty();
    }

    public void addTeamPackets(FakeTeam fakeTeam) {
        new PacketWrapper(fakeTeam.getName(), fakeTeam.getPrefix(), fakeTeam.getSuffix(), 0, new ArrayList<>()).send();
    }

    public void addPlayerToTeamPackets(FakeTeam fakeTeam, String player) {
        new PacketWrapper(fakeTeam.getName(), 3, Arrays.asList(player)).send();
    }

    public void sendTeams(Player player) {
        for (Map.Entry<FakeTeam, List<String>> entry : FAKE_TEAMS.entrySet()) {
            FakeTeam fakeTeam = entry.getKey();
            new PacketWrapper(fakeTeam.getName(), fakeTeam.getPrefix(), fakeTeam.getSuffix(), 0, new ArrayList<>()).send(player);
            new PacketWrapper(fakeTeam.getName(), 3, entry.getValue()).send(player);
        }
    }

    public void updateNametag(String player, String prefix, String suffix) {
        prefix = prefix == null || prefix.isEmpty() ? getPrefix(player) : prefix;
        suffix = suffix == null || suffix.isEmpty() ? getSuffix(player) : suffix;
        addPlayerToTeam(player, getFakeTeam(prefix, suffix));
    }

    public void overlapNametag(String player, String prefix, String suffix) {
        prefix = prefix == null ? "" : prefix;
        suffix = suffix == null ? "" : suffix;
        addPlayerToTeam(player, getFakeTeam(prefix, suffix));
    }

}