package com.nametagedit.plugin.api;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.NametagManager;
import com.nametagedit.plugin.api.data.FakeTeam;
import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.Nametag;
import com.nametagedit.plugin.api.events.NametagEvent;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Implements the INametagAPI interface. There only
 * exists one instance of this class.
 */
@AllArgsConstructor
public final class NametagAPI implements INametagApi {

    private NametagHandler handler;
    private NametagManager manager;

    @Override
    public FakeTeam getFakeTeam(Player player) {
        return manager.getFakeTeam(player.getName());
    }

    @Override
    public Nametag getNametag(Player player) {
        FakeTeam team = manager.getFakeTeam(player.getName());
        boolean nullTeam = team == null;
        return new Nametag(nullTeam ? "" : team.getPrefix(), nullTeam ? "" : team.getSuffix());
    }

    @Override
    public void clearNametag(Player player) {
        if (shouldFireEvent(player, NametagEvent.ChangeType.CLEAR)) {
            manager.reset(player.getName());
        }
    }

    @Override
    public void reloadNametag(Player player) {
        if (shouldFireEvent(player, NametagEvent.ChangeType.RELOAD)) {
            handler.applyTagToPlayer(player, false);
        }
    }

    @Override
    public void clearNametag(String player) {
        manager.reset(player);
    }

    @Override
    public void setPrefix(Player player, String prefix) {
        FakeTeam fakeTeam = manager.getFakeTeam(player.getName());
        setNametagAlt(player, prefix, fakeTeam == null ? null : fakeTeam.getSuffix());
    }

    @Override
    public void setSuffix(Player player, String suffix) {
        FakeTeam fakeTeam = manager.getFakeTeam(player.getName());
        setNametagAlt(player, fakeTeam == null ? null : fakeTeam.getPrefix(), suffix);
    }

    @Override
    public void setPrefix(String player, String prefix) {
        FakeTeam fakeTeam = manager.getFakeTeam(player);
        manager.setNametag(player, prefix, fakeTeam == null ? null : fakeTeam.getSuffix());
    }

    @Override
    public void setSuffix(String player, String suffix) {
        FakeTeam fakeTeam = manager.getFakeTeam(player);
        manager.setNametag(player, fakeTeam == null ? null : fakeTeam.getPrefix(), suffix);
    }

    @Override
    public void setNametag(Player player, String prefix, String suffix) {
        setNametagAlt(player, prefix, suffix);
    }

    @Override
    public void setNametag(String player, String prefix, String suffix) {
        manager.setNametag(player, prefix, suffix);
    }

    @Override
    public List<GroupData> getGroupData() {
        return handler.getGroupData();
    }

    @Override
    public void saveGroupData(GroupData... groupData) {
        handler.getAbstractConfig().save(groupData);
    }

    @Override
    public void applyTags() {
        handler.applyTags();
    }

    @Override
    public void applyTagToPlayer(Player player, boolean loggedIn) {
        handler.applyTagToPlayer(player,loggedIn);
    }

    /**
     * Private helper function to reduce redundancy
     */
    private boolean shouldFireEvent(Player player, NametagEvent.ChangeType type) {
        NametagEvent event = new NametagEvent(player.getName(), "", getNametag(player), type);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    /**
     * Private helper function to reduce redundancy
     */
    private void setNametagAlt(Player player, String prefix, String suffix) {
        Nametag nametag = new Nametag(
                handler.formatWithPlaceholders(player, prefix, true),
                handler.formatWithPlaceholders(player, suffix, true)
        );

        NametagEvent event = new NametagEvent(player.getName(), prefix, nametag, NametagEvent.ChangeType.UNKNOWN);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        manager.setNametag(player.getName(), nametag.getPrefix(), nametag.getSuffix());
    }

}