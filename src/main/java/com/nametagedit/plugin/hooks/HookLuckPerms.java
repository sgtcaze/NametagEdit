package com.nametagedit.plugin.hooks;

import com.nametagedit.plugin.NametagHandler;
import lombok.AllArgsConstructor;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.event.user.track.UserTrackEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@AllArgsConstructor
public class HookLuckPerms implements Listener {

    private NametagHandler handler;

    @EventHandler
    public void onUserTrackEvent(UserTrackEvent event) {
        User user = event.getUser();
        Player player = Bukkit.getPlayer(user.getUuid());
        if (player != null) {
            handler.applyTagToPlayer(player);
        }
    }

}