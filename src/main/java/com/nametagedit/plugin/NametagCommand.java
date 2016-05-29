package com.nametagedit.plugin;

import com.nametagedit.plugin.api.events.NametagEvent;
import com.nametagedit.plugin.converter.Converter;
import com.nametagedit.plugin.converter.ConverterTask;
import com.nametagedit.plugin.storage.data.GroupData;
import com.nametagedit.plugin.utils.Utils;
import lombok.AllArgsConstructor;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

@AllArgsConstructor
public class NametagCommand implements CommandExecutor {

    private NametagHandler handler;

    /**
     * Base command for NametagEdit. See the Wiki for usage and examples.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("nametagedit.use")) {
            NametagMessages.NO_PERMISSION.send(sender);
            return false;
        }

        if (args.length < 1) {
            if (handler.isFancyMessageCompatible()) {
                sendUsage(sender);
            } else {
                sendLegacyUsage(sender);
            }
        } else if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "reload":
                    cmdReload(sender);
                    break;
                case "convert":
                    cmdConvert(sender, args);
                    break;
                case "clear":
                    cmdClear(sender, args);
                    break;
                case "prefix":
                case "suffix":
                    cmdEdit(sender, args);
                    break;
                case "groups":
                    cmdGroups(sender, args);
                    break;
                default:
                    sendUsage(sender);
                    break;
            }
        }
        return false;
    }

    private void sendLegacyUsage(CommandSender sender) {
        sender.sendMessage(Utils.format("&3&lNametag&f&lEdit &c&lCommand Usage"));
        sender.sendMessage(Utils.format("&e&lPlayer Usage"));
        sender.sendMessage(Utils.format("&6/ne reload &fReloads nametags and configs"));
        sender.sendMessage(Utils.format("&6/ne convert &fConverts data"));
        sender.sendMessage(Utils.format("&6/ne prefix <Player> <value> &fSets a prefix for a player"));
        sender.sendMessage(Utils.format("&6/ne suffix <Player> <value> &fSets a suffix for a player"));
        sender.sendMessage(Utils.format("&6/ne clear <Player> &fClears a player's nametag"));
        sender.sendMessage(Utils.format("&e&lGroup Usage"));
        sender.sendMessage(Utils.format("&6/ne groups list &fLists the current loaded groups"));
        sender.sendMessage(Utils.format("&6/ne groups order <MyTopGroup> <MySecond> ..."));
        sender.sendMessage(Utils.format("&6/ne groups add <Group> &fCreates a group"));
        sender.sendMessage(Utils.format("&6/ne groups remove <Group> &fRemoves a group"));
        sender.sendMessage(Utils.format("&6/ne groups <Group> permission <value> &fChanges the permission"));
        sender.sendMessage(Utils.format("&6/ne groups <Group> clear <prefix/suffix> &fClears the prefix or suffix"));
        sender.sendMessage(Utils.format("&6/ne groups <Group> prefix <value> &fChanges the prefix"));
        sender.sendMessage(Utils.format("&6/ne groups <Group> suffix <value> &fChanges the suffix"));
    }

    private void sendUsage(CommandSender sender) {
        new FancyMessage("Nametag").color(ChatColor.DARK_AQUA).style(ChatColor.BOLD).then("Edit ").color(ChatColor.WHITE).style(ChatColor.BOLD)
                .then("Command Usage").color(ChatColor.RED).style(ChatColor.BOLD)
                .then("\nPlayer Usage ").color(ChatColor.YELLOW).style(ChatColor.BOLD).then(" (Click a Command!)").color(ChatColor.GRAY)
                .then("\n/ne reload ").color(ChatColor.GOLD).suggest("/ne reload").then("Reloads nametags and configs").color(ChatColor.WHITE)
                .then("\n/ne convert ").tooltip(Utils.format("&e/ne convert  <file/mysql> <file/mysql> (legacy: true/false)")).color(ChatColor.GOLD).suggest("/ne convert ").then("Converts data").color(ChatColor.WHITE)
                .then("\n/ne prefix <Player> <value> ").color(ChatColor.GOLD).suggest("/ne prefix ").then("Sets a prefix for a player").color(ChatColor.WHITE)
                .then("\n/ne suffix <Player> <value> ").color(ChatColor.GOLD).suggest("/ne suffix ").then("Sets a suffix for a player").color(ChatColor.WHITE)
                .then("\n/ne clear <Player> ").color(ChatColor.GOLD).suggest("/ne clear ").then("Clears a player's nametag").color(ChatColor.WHITE)
                .then("\nGroup Usage ").color(ChatColor.YELLOW).style(ChatColor.BOLD).then(" (Click a Command!)").color(ChatColor.GRAY)
                .then("\n/ne groups list ").color(ChatColor.GOLD).suggest("/ne groups list").then("Lists the current loaded groups").color(ChatColor.WHITE)
                .then("\n/ne groups order <group> ... ").color(ChatColor.GOLD).suggest("/ne groups order ").then("Orders groups for the database").color(ChatColor.WHITE)
                .then("\n/ne groups add <Group> ").color(ChatColor.GOLD).suggest("/ne groups add ").then("Creates a group").color(ChatColor.WHITE)
                .then("\n/ne groups remove <Group> ").color(ChatColor.GOLD).suggest("/ne groups remove ").then("Removes a group").color(ChatColor.WHITE)
                .then("\n/ne groups <Group> permission <value> ").color(ChatColor.GOLD).suggest("/ne groups Group permission my.permission").then("Changes the permission").color(ChatColor.WHITE)
                .then("\n/ne groups <Group> clear <prefix/suffix> ").color(ChatColor.GOLD).suggest("/ne groups Group clear <prefix/suffix>").then("Clears the prefix or suffix").color(ChatColor.WHITE)
                .then("\n/ne groups <Group> prefix <value> ").color(ChatColor.GOLD).suggest("/ne groups Group prefix something").then("Changes the prefix").color(ChatColor.WHITE)
                .then("\n/ne groups <Group> suffix <value> ").color(ChatColor.GOLD).suggest("/ne groups Group suffix something").then("Changes the suffix").color(ChatColor.WHITE)
                .send(sender);
    }

    /**
     * Reloads plugin configuration
     */
    private void cmdReload(CommandSender sender) {
        if (sender.hasPermission("nametagedit.reload")) {
            handler.reload();
            NametagMessages.RELOADED_DATA.send(sender);
        } else {
            NametagMessages.NO_PERMISSION.send(sender);
        }
    }

    /**
     * Converts data
     */
    private void cmdConvert(CommandSender sender, String[] args) {
        if (sender.hasPermission("nametagedit.convert")) {
            if (args.length == 4) {
                boolean sourceIsFile = args[1].equalsIgnoreCase("file");
                boolean destinationIsSQL = args[2].equalsIgnoreCase("mysql");
                boolean legacy = args[3].equalsIgnoreCase("true");
                NametagMessages.CONVERSION.sendMulti(sender, "groups & players", sourceIsFile ? "file" : "mysql", destinationIsSQL ? "mysql" : "file", legacy);

                if (sourceIsFile && !destinationIsSQL && legacy) {
                    new Converter().legacyConversion(sender, handler.getPlugin());
                } else if ((destinationIsSQL && sourceIsFile) || (!sourceIsFile && !destinationIsSQL)) {
                    new ConverterTask(!destinationIsSQL, sender, handler.getPlugin()).runTaskAsynchronously(handler.getPlugin());
                }
            } else {
                sender.sendMessage(Utils.format("&6/ne convert <file/mysql> <file/mysql> (legacy: true/false)"));
            }
        }
    }

    /**
     * Clears the tag for a given player
     */
    private void cmdClear(CommandSender sender, String[] args) {
        if (!(sender.hasPermission("nametagedit.clear.self") || sender.hasPermission("nametagedit.clear.others"))) {
            NametagMessages.NO_PERMISSION.send(sender);
            return;
        }

        if (args.length == 2) {
            String targetName = args[1];
            if (!sender.hasPermission("nametagedit.clear.others") && !targetName.equalsIgnoreCase(sender.getName())) {
                NametagMessages.MODIFY_OWN_TAG.send(sender);
                return;
            }

            handler.clear(sender, targetName);
            handler.applyTagToPlayer(Bukkit.getPlayerExact(targetName));
        }
    }

    /**
     * Edits the prefix or suffix for other players
     */
    private void cmdEdit(CommandSender sender, String[] args) {
        if (!(sender.hasPermission("nametagedit.edit.self") || sender.hasPermission("nametagedit.edit.others"))) {
            NametagMessages.NO_PERMISSION.send(sender);
            return;
        }

        if (args.length > 2) {
            String targetName = args[1];

            if (!sender.hasPermission("nametagedit.edit.others") && !targetName.equalsIgnoreCase(sender.getName())) {
                NametagMessages.MODIFY_OWN_TAG.send(sender);
                return;
            }

            NametagEvent.ChangeType changeType = args[0].equalsIgnoreCase("prefix") ? NametagEvent.ChangeType.PREFIX : NametagEvent.ChangeType.SUFFIX;
            handler.save(sender, targetName, changeType, Utils.format(args, 2, args.length));
        }
    }

    /**
     * Modifies groups
     */
    private void cmdGroups(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nametagedit.groups")) {
            NametagMessages.NO_PERMISSION.send(sender);
            return;
        }

        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("list")) {
                sender.sendMessage(Utils.format("&f&lLoaded Groups"));
                for (GroupData groupData : handler.getGroupData()) {
                    sender.sendMessage(Utils.format("&6Group: &f" + groupData.getGroupName() + " &6Permission: &f" + groupData.getPermission()
                            + " &6Formatted: " + groupData.getPrefix() + sender.getName() + groupData.getSuffix()));
                }
            } else if (args[1].equalsIgnoreCase("order")) {
                if (!handler.getPlugin().getConfig().getBoolean("MySQL.Enabled")) {
                    sender.sendMessage(Utils.format("&cThis option is (temporarily) only available for NametagEdit servers that use a database."));
                    return;
                }

                if (args.length <= 2) {
                    sender.sendMessage(Utils.format("&cBe sure to enter the group order!"));
                    return;
                }

                handler.getAbstractConfig().orderGroups(sender, args);
            } else if (args[1].equalsIgnoreCase("remove")) {
                if (args.length == 3) {
                    String group = args[2];

                    GroupData toDelete = null;
                    for (GroupData groupData : handler.getGroupData()) {
                        if (groupData.getGroupName().equalsIgnoreCase(group)) {
                            toDelete = groupData;
                            break;

                        }
                    }

                    if (toDelete != null) {
                        handler.deleteGroup(toDelete);
                        NametagMessages.GROUP_REMOVED.send(sender, group);
                    }
                }
            } else if (args[1].equalsIgnoreCase("add")) {
                if (args.length == 3) {
                    String group = args[2];

                    for (GroupData groupData : handler.getGroupData()) {
                        if (groupData.getGroupName().equalsIgnoreCase(group)) {
                            NametagMessages.GROUP_EXISTS.send(sender, group);
                            return;
                        }
                    }

                    handler.addGroup(new GroupData(group, "", "", "", new Permission("my.perm", PermissionDefault.FALSE)));
                    NametagMessages.CREATED_GROUP.send(sender, group);
                }
            } else {
                if (args.length >= 4) {
                    String group = args[1];
                    GroupData groupData = null;

                    for (GroupData groups : handler.getGroupData()) {
                        if (groups.getGroupName().equalsIgnoreCase(group)) {
                            groupData = groups;
                            break;
                        }
                    }

                    if (groupData == null) {
                        NametagMessages.GROUP_EXISTS_NOT.send(sender, group);
                        return;
                    }

                    if (args[2].equalsIgnoreCase("permission")) {
                        groupData.setPermission(args[3]);
                        handler.save(groupData);
                        NametagMessages.GROUP_VALUE.sendMulti(sender, group, "permission", args[3]);
                    } else if (args[2].equalsIgnoreCase("prefix")) {
                        String value = Utils.format(args, 3, args.length).replace("\"", "");
                        groupData.setPrefix(Utils.format(value));
                        handler.applyTags();
                        handler.save(groupData);
                        NametagMessages.GROUP_VALUE.sendMulti(sender, group, "prefix", Utils.format(value));
                    } else if (args[2].equalsIgnoreCase("suffix")) {
                        String value = Utils.format(args, 3, args.length).replace("\"", "");
                        groupData.setSuffix(Utils.format(value));
                        handler.applyTags();
                        handler.save(groupData);
                        NametagMessages.GROUP_VALUE.sendMulti(sender, group, "suffix", Utils.format(value));
                    } else if (args[2].equalsIgnoreCase("clear")) {
                        boolean prefix = args[3].equalsIgnoreCase("prefix");
                        if (prefix) {
                            groupData.setPrefix("&f");
                        } else {
                            groupData.setSuffix("&f");
                        }
                        handler.applyTags();
                        handler.save(groupData);
                        NametagMessages.GROUP_VALUE_CLEARED.sendMulti(sender, prefix ? "prefix" : "suffix", group);
                    }
                } else {
                    NametagMessages.GROUP_USAGE.send(sender);
                }
            }
        }
    }

}
