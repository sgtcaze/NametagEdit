package com.nametagedit.plugin.hooks;

import com.nametagedit.plugin.NametagHandler;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.tehkode.permissions.events.PermissionEntityEvent;

@AllArgsConstructor
public class HookPermissionsEX implements Listener {

    private final NametagHandler handler;

    @EventHandler
    public void onPermissionEntityEvent(PermissionEntityEvent event) {
        Player player = Bukkit.getPlayerExact(event.getEntity().getName());
        if (player != null) {
            handler.applyTagToPlayer(player, false);
        }
    }

}