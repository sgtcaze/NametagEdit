package com.nametagedit.plugin.hooks;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagHandler;
import java.util.UUID;
import lombok.AllArgsConstructor;
import me.stevie212.McDuels.Events.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class HookMcDuels implements Listener {

    private NametagHandler handler;
    private NametagEdit plugin;

    @EventHandler
    public void onBattleEnd(BattleEndEvent event) {
        for(UUID uuid : event.getAllPlayers()){
            Player player = Bukkit.getPlayer(uuid);
            if(player != null && player.isOnline()){
                plugin.getHandler().getNametagManager().reset(player.getName());
            }
        }
        new BukkitRunnable() {
        @Override
        public void run() {
        for(UUID uuid : event.getAllPlayers()){
            Player player = Bukkit.getPlayer(uuid);
            if(player != null && player.isOnline()){
                handler.applyTagToPlayer(player, true);
            }
        }
        }
        }.runTaskLater(plugin, 3);
    }
    @EventHandler
    public void onBotEnd(DuelBotEndEvent event) {
        for(UUID uuid : event.getAllPlayers()){
            Player player = Bukkit.getPlayer(uuid);
            if(player != null && player.isOnline()){
                plugin.getHandler().getNametagManager().reset(player.getName());
            }
        }
        new BukkitRunnable() {
        @Override
        public void run() {
        for(UUID uuid : event.getAllPlayers()){
            Player player = Bukkit.getPlayer(uuid);
            if(player != null && player.isOnline()){
                handler.applyTagToPlayer(player, true);
            }
        }
        }
        }.runTaskLater(plugin, 3);
    }
    @EventHandler
    public void onDuelEnd(Duel1v1EndEvent event) {
        plugin.getHandler().getNametagManager().reset(event.getLooser().getName());
        plugin.getHandler().getNametagManager().reset(event.getWinner().getName());
        new BukkitRunnable() {
        @Override
        public void run() {
        handler.applyTagToPlayer(event.getLooser(), true);
        handler.applyTagToPlayer(event.getWinner(), true);
        }
        }.runTaskLater(plugin, 3);
    }
    @EventHandler
    public void onPartyFightEnd(PartyFightEndEvent event) {
        for(UUID uuid : event.getLoosingPartyPlayers()){
            Player player = Bukkit.getPlayer(uuid);
            plugin.getHandler().getNametagManager().reset(player.getName());
        }
        for(UUID uuid : event.getWinningPartyPlayers()){
            Player player = Bukkit.getPlayer(uuid);
            plugin.getHandler().getNametagManager().reset(player.getName());
        }
        new BukkitRunnable() {
        @Override
        public void run() {
        for(UUID uuid : event.getLoosingPartyPlayers()){
            Player player = Bukkit.getPlayer(uuid);
            handler.applyTagToPlayer(player, true);
        }
        for(UUID uuid : event.getWinningPartyPlayers()){
            Player player = Bukkit.getPlayer(uuid);
            handler.applyTagToPlayer(player, true);
        }
        }
        }.runTaskLater(plugin, 3);
    }
}
