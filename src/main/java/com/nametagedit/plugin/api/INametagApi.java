package com.nametagedit.plugin.api;

import com.nametagedit.plugin.api.data.FakeTeam;
import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.Nametag;
import org.bukkit.entity.Player;

import java.util.List;

/**
 *
 */
public interface INametagApi {

    /**
     * Function gets the fake team data for
     * player.
     *
     * @param player the player to check
     * @return the fake team
     */
    FakeTeam getFakeTeam(Player player);

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

    /**
     * Hide the name above the head
     * @param player the player whose nametag to hide
     */
    void hideNametag(Player player);

    /**
     * Hide the name above the head
     * @param player he player whose nametag to hide
     */
    void hideNametag(String player);

    /**
     * Show the name above the head
     * @param player he player whose nametag to show
     */
    void showNametag(Player player);

    /**
     * Show the name above the head
     * @param player he player whose nametag to show
     */
    void showNametag(String player);

    /**
     * Gets the data of all groups
     * @return list containing all group data
     */
    List<GroupData> getGroupData();


    /**
     * Saves the provided group data
     * @param groupData the group data to save
     */
    void saveGroupData(GroupData... groupData);

    /**
     * Applies tags to all players
     * <p>
     * Note: Only affects memory, does NOT
     * add/remove from storage.
     * </p>
     */
    void applyTags();

    /**
     * Applies tags to specific player
     * <p>
     * Note: Only affects memory, does NOT
     * add/remove from storage
     * </p>
     * @param player the player to apply nametag to
     * @param loggedIn is the player logged in
     */
    void applyTagToPlayer(Player player, boolean loggedIn);

    /**
     * Updates a players prefix
     * <p>
     * Note: Does affect memory. Automatically applies tags to player
     * so no applyTags call is necessary
     * </p>
     * @param target name of the player to update tag of
     * @param prefix prefix to change to
     */
    void updatePlayerPrefix(String target, String prefix);

    /**
     * Updates a players suffix
     * <p>
     * Note: Does affect memory. Automatically applies tags to player
     * so no applyTags call is necessary
     * </p>
     * @param target name of the player to update tag of
     * @param suffix suffix to change to
     */
    void updatePlayerSuffix(String target, String suffix);

    /**
     * Updates a players nametag
     * <p>
     * Note: Does affect memory. Automatically applies tags to player
     * so no applyTags call is necessary
     * </p>
     * @param target name of the player to update tag of
     * @param prefix prefix to set to
     * @param suffix suffix to set to
     */
    void updatePlayerNametag(String target, String prefix, String suffix);
}