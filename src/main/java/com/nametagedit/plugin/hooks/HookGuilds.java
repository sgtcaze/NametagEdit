package com.nametagedit.plugin.hooks;

import com.nametagedit.plugin.NametagHandler;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import me.glaremasters.guilds.api.events.base.GuildEvent;

@AllArgsConstructor
public class HookGuilds implements Listener {

    private final NametagHandler handler;

    @EventHandler
    public void onGuildEvent(GuildEvent event) {
        Player player = Bukkit.getPlayerExact(event.getPlayer().getName());
        if (player != null) {
            handler.applyTagToPlayer(player, false);
        }
    }

}