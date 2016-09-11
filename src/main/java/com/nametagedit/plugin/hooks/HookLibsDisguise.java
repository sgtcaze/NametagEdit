package com.nametagedit.plugin.hooks;

import com.nametagedit.plugin.NametagEdit;
import lombok.AllArgsConstructor;
import me.libraryaddict.disguise.events.UndisguiseEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@AllArgsConstructor
public class HookLibsDisguise implements Listener {

    private NametagEdit plugin;

    @EventHandler
    public void onUndisguiseEvent(UndisguiseEvent event) {
        if (event.getEntity() instanceof Player) {
            plugin.getHandler().applyTagToPlayer((Player) event.getEntity());
        }
    }

}