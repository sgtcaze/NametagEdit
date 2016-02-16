package com.nametagedit.plugin.api;

import org.bukkit.entity.Player;

public interface INametagApi {

    void setPrefix(Player player, String prefix);

    void setSuffix(Player player, String suffix);

    void setPrefix(String player, String prefix);

    void setSuffix(String player, String suffix);

    void setNametag(Player player, String prefix, String suffix);

    void setNametag(String player, String prefix, String suffix);

}