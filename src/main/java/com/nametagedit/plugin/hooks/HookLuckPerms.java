package com.nametagedit.plugin.hooks;

import com.nametagedit.plugin.NametagHandler;
import lombok.AllArgsConstructor;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class HookLuckPerms {

    private NametagHandler handler;

    public void register() {
        LuckPermsApi api = Bukkit.getServicesManager().getRegistration(LuckPermsApi.class).getProvider();
        api.getEventBus().subscribe(UserDataRecalculateEvent.class, this::onDataRecalculate);
    }

    private void onDataRecalculate(UserDataRecalculateEvent event) {
        Bukkit.getScheduler().runTask(handler.getPlugin(), () -> {
            Player player = Bukkit.getPlayer(event.getUser().getUuid());
            if (player != null) {
                handler.applyTagToPlayer(player);
            }
        });
    }

}