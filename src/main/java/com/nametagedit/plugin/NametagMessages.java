package com.nametagedit.plugin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public enum NametagMessages {

    GROUP_EXISTS("The group &c%s &falready exists"),
    GROUP_VALUE_CLEARED("Cleared the %s for the group %s"),
    GROUP_EXISTS_NOT("The group &c%s &fdoes not exist!"),
    OPERATION_COMPLETED("The conversion %s"),
    GROUP_VALUE("Changed &c%s's &f%s to %s"),
    GROUP_USAGE("Usage &c/ne groups [option] <group> <value>"),
    GROUP_REMOVED("Successfully removed group &c%s"),
    MODIFY_OWN_TAG("You can only modify your own tag."),
    NO_PERMISSION("You do not have permission to use this."),
    RELOADED_DATA("Successfully reloaded plugin data"),
    FILE_DOESNT_EXIST("The file %s does not exist"),
    UUID_LOOKUP_FAILED("Could not find the uuid for &c%s"),
    CREATED_GROUP("Created group %s"),
    CONVERSION("Attempting to convert %s from %s to %s. (Legacy: %s)");

    private final String text;

    NametagMessages(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return ChatColor.translateAlternateColorCodes('&', "&3NTE &4» &r" + text);
    }

    public void send(CommandSender sender) {
        sender.sendMessage(toString());
    }

    public void send(CommandSender sender, String replacement) {
        sender.sendMessage(toString().replace("%s", replacement));
    }

    public void sendMulti(CommandSender sender, Object... object) {
        sender.sendMessage(String.format(toString(), object));
    }

}