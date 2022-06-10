package com.nametagedit.plugin.hooks.invisibility;

import com.nametagedit.plugin.NametagEdit;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class InvisibilityTask extends BukkitRunnable {

    private final Player player;

    @Override
    public void run() {
        if(player == null || !player.isOnline()){
            cancel();
            return;
        }

        if(player.hasPotionEffect(PotionEffectType.INVISIBILITY)){
            NametagEdit.getApi().hideNametag(player);
        }else{
            NametagEdit.getApi().showNametag(player);
        }
    }
}
