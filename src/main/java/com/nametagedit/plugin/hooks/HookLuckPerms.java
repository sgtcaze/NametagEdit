package com.nametagedit.plugin.hooks;

import com.nametagedit.plugin.NametagHandler;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.event.EventBus;
import me.lucko.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class HookLuckPerms implements Listener {

    private NametagHandler handler;

    public HookLuckPerms(NametagHandler handler) {
        this.handler = handler;
        LuckPermsApi api = LuckPerms.getApi();
        EventBus eventBus = api.getEventBus();
        eventBus.subscribe(UserDataRecalculateEvent.class, this::onUserDataRecalculateEvent);
    }

    private void onUserDataRecalculateEvent(UserDataRecalculateEvent event) {
        User user = event.getUser();
        Player player = Bukkit.getPlayer(user.getUuid());
        if (player != null) {
            handler.applyTagToPlayer(player, false);
        }
    }

}