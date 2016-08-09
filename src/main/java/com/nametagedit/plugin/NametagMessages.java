package com.nametagedit.plugin;

import com.nametagedit.plugin.utils.Utils;
import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@AllArgsConstructor
public enum NametagMessages {

    SET_PRIORITY("Set sort priority to %s for %s"),
    INVALID_SORT_PRIORITY("Sort priority cannot be less than -1 or equal to 0."),
    DEBUG_TOGGLED("NametagEdit debug has been %s"),
    GROUP_EXISTS("The group %s already exists"),
    GROUP_VALUE_CLEARED("Cleared the %s for the group %s"),
    GROUP_EXISTS_NOT("The group %s does not exist!"),
    OPERATION_COMPLETED("The conversion %s"),
    GROUP_VALUE("Changed %s's %s to %s"),
    USAGE_CONVERT("Usage: /nte convert <file/db> <file/db> <legacy (true/false)>"),
    GROUP_REMOVED("Successfully removed group %s"),
    MODIFY_OWN_TAG("You can only modify your own tag."),
    NO_PERMISSION("You do not have permission to use this."),
    RELOADED_DATA("Successfully reloaded plugin data"),
    FILE_DOESNT_EXIST("The file %s does not exist"),
    UUID_LOOKUP_FAILED("Could not find the uuid for %s"),
    CREATED_GROUP("Created group %s"),
    NOT_A_NUMBER("Uh-oh! %s does not appear to be a number!"),
    CONVERSION("Attempting to convert %s from %s to %s. (Legacy: %s)");

    private final String text;

    @Override
    public String toString() {
        return ChatColor.translateAlternateColorCodes('&', "&8» &a" + text);
    }

    public void send(CommandSender sender) {
        sender.sendMessage(toString());
    }

    public void send(CommandSender sender, String replacement) {
        sender.sendMessage(toString().replace("%s", Utils.format(replacement)));
    }

    public void send(CommandSender sender, Object... object) {
        sender.sendMessage(String.format(toString(), object));
    }

}