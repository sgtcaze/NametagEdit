package com.nametagedit.plugin.api;

import com.nametagedit.plugin.api.data.Nametag;
import org.bukkit.entity.Player;

public interface INametagApi {

    /**
     * Function gets the nametag for a player if
     * it exists. This will never return a null.
     *
     * @param player the player to check
     * @return the nametag for the player
     */
    Nametag getNametag(Player player);

    /**
     * Removes a player's nametag in memory
     * only.
     * <p>
     * Note: Only affects memory, does NOT
     * add/remove from storage.
     *
     * @param player whose nametag to clear
     */
    void clearNametag(Player player);

    /**
     * Reloads a nametag if the player has a
     * custom nametag via the Players or Groups
     * configurations.
     * <p>
     *
     * @param player whose nametag to reload
     */
    void reloadNametag(Player player);

    /**
     * Removes a player's nametag in memory
     * only.
     * <p>
     * Note: Only affects memory, does NOT
     * add/remove from storage.
     *
     * @param player whose nametag to clear
     */
    void clearNametag(String player);

    /**
     * Sets the prefix for a player. The previous
     * suffix is kept if it exists.
     * <p>
     * Note: Only affects memory, does NOT
     * add/remove from storage.
     *
     * @param player the player whose nametag to change
     * @param prefix the prefix to change to
     */
    void setPrefix(Player player, String prefix);

    /**
     * Sets the suffix for a player. The previous
     * prefix is kept if it exists.
     * <p>
     * Note: Only affects memory, does NOT
     * add/remove from storage.
     *
     * @param player the player whose nametag to change
     * @param suffix the suffix to change to
     */
    void setSuffix(Player player, String suffix);

    /**
     * Sets the prefix for a player. The previous
     * suffix is kept if it exists.
     * <p>
     * Note: Only affects memory, does NOT
     * add/remove from storage.
     *
     * @param player the player whose nametag to change
     * @param prefix the prefix to change to
     */
    void setPrefix(String player, String prefix);

    /**
     * Sets the suffix for a player. The previous
     * prefix is kept if it exists.
     * <p>
     * Note: Only affects memory, does NOT
     * add/remove from storage.
     *
     * @param player the player whose nametag to change
     * @param suffix the suffix to change to
     */
    void setSuffix(String player, String suffix);

    /**
     * Sets the nametag for a player.
     * <p>
     * Note: Only affects memory, does NOT
     * add/remove from storage.
     *
     * @param player the player whose nametag to change
     * @param prefix the prefix to change to
     * @param suffix the suffix to change to
     */
    void setNametag(Player player, String prefix, String suffix);

    /**
     * Sets the nametag for a player.
     * <p>
     * Note: Only affects memory, does NOT
     * add/remove from storage.
     *
     * @param player the player whose nametag to change
     * @param prefix the prefix to change to
     * @param suffix the suffix to change to
     */
    void setNametag(String player, String prefix, String suffix);

}