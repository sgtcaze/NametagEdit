package com.nametagedit.plugin.api;

import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.NametagManager;
import com.nametagedit.plugin.api.data.FakeTeam;
import com.nametagedit.plugin.api.events.NametagEvent;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Implements the INametagAPI interface. There only
 * exists one instance of this class.
 */
@AllArgsConstructor
public final class NametagAPI implements INametagApi {

    private NametagHandler handler;
    private NametagManager manager;

    @Override
    public void clearNametag(Player player) {
        manager.reset(player.getName());
    }

    @Override
    public void clearNametag(String player) {
        manager.reset(player);
    }

    @Override
    public void setPrefix(Player player, String prefix) {
        setPrefix(player.getName(), handler.formatWithPlaceholders(player, prefix));
    }

    @Override
    public void setSuffix(Player player, String suffix) {
        setSuffix(player.getName(), handler.formatWithPlaceholders(player, suffix));
    }

    @Override
    public void setPrefix(String player, String prefix) {
        NametagEvent event = new NametagEvent(player, prefix, NametagEvent.ChangeType.PREFIX, NametagEvent.ChangeReason.API);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            FakeTeam fakeTeam = manager.getFakeTeam(player);
            manager.setNametag(player, prefix, fakeTeam == null ? null : fakeTeam.getSuffix());
        }
    }

    @Override
    public void setSuffix(String player, String suffix) {
        NametagEvent event = new NametagEvent(player, suffix, NametagEvent.ChangeType.SUFFIX, NametagEvent.ChangeReason.API);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            FakeTeam fakeTeam = manager.getFakeTeam(player);
            manager.setNametag(player, fakeTeam == null ? null : fakeTeam.getPrefix(), suffix);
        }
    }

    @Override
    public void setNametag(Player player, String prefix, String suffix) {
        manager.setNametag(player.getName(), handler.formatWithPlaceholders(player, prefix), handler.formatWithPlaceholders(player, suffix));
    }

    @Override
    public void setNametag(String player, String prefix, String suffix) {
        Player onlinePlayer = Bukkit.getPlayerExact(player);
        prefix = handler.formatWithPlaceholders(onlinePlayer, prefix);
        suffix = handler.formatWithPlaceholders(onlinePlayer, suffix);
        manager.setNametag(player, prefix, suffix);
    }

}