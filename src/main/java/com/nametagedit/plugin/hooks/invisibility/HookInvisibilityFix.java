package com.nametagedit.plugin.hooks.invisibility;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.api.data.FakeTeam;
import com.nametagedit.plugin.api.data.Nametag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HookInvisibilityFix implements Listener {

    private final Map<UUID, BukkitTask> playerTasks = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        InvisibilityTask invisibilityTask = new InvisibilityTask(event.getPlayer());
        BukkitTask task = invisibilityTask.runTaskTimerAsynchronously(NametagEdit.getInstance(), 0L, 2L);
        this.playerTasks.put(event.getPlayer().getUniqueId(), task);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        BukkitTask task = playerTasks.remove(event.getPlayer().getUniqueId());
        if(task != null){
            task.cancel();
        }
    }

}
