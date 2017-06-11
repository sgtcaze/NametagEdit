package com.nametagedit.plugin;

import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.events.NametagEvent;
import com.nametagedit.plugin.converter.Converter;
import com.nametagedit.plugin.converter.ConverterTask;
import com.nametagedit.plugin.utils.Utils;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class NametagCommand implements CommandExecutor, TabExecutor {

    private NametagHandler handler;

    private List<String> getSuggestions(String argument, String... array) {
        argument = argument.toLowerCase();
        List<String> suggestions = new ArrayList<>();
        for (String suggestion : array) {
            if (suggestion.toLowerCase().startsWith(argument)) {
                suggestions.add(suggestion);
            }
        }
        return suggestions;
    }

    /**
     * Handles auto completions
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return getSuggestions(args[0], "debug", "reload", "convert", "player", "group");
        } else if (args.length == 2 || args.length == 3) {
            if (args[0].equalsIgnoreCase("player")) {
                if (args.length == 2) {
                    List<String> suggestions = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            suggestions.add(player.getName());
                        }
                    }
                    return suggestions;
                } else {
                    return getSuggestions(args[2], "clear", "prefix", "suffix", "priority");
                }
            } else if (args[0].equalsIgnoreCase("group")) {
                if (args.length == 2) {
                    List<String> data = new ArrayList<>(handler.getGroupData().size() + 4);
                    data.add("list");
                    data.add("add");
                    data.add("remove");
                    data.add("order");
                    for (GroupData groupData : handler.getGroupData()) {
                        data.add(groupData.getGroupName());
                    }

                    return getSuggestions(args[1], data.toArray(new String[data.size()]));
                } else {
                    return getSuggestions(args[2], "clear", "prefix", "suffix", "permission", "priority");
                }
            }
        }

        return new ArrayList<>();
    }

    /**
     * Base command for NametagEdit. See the Wiki for usage and examples.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (isNotPermissed(sender, "nametagedit.use")) return false;
        if (args.length < 1) {
            sendUsage(sender);
        } else if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "reload":
                    cmdReload(sender);
                    break;
                case "convert":
                    cmdConvert(sender, args);
                    break;
                case "debug":
                    handler.toggleDebug();
                    NametagMessages.DEBUG_TOGGLED.send(sender, handler.debug() ? "&aENABLED" : "&cDISABLED");
                    break;
                case "player":
                    cmdPlayer(sender, args);
                    break;
                case "group":
                    cmdGroups(sender, args);
                    break;
                case "teams":
                    int emptyTeams = 0;
                    boolean unregister = args.length > 1 && args[1].equalsIgnoreCase("clear");
                    for (Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
                        if (team.getEntries().isEmpty()) {
                            if (unregister) {
                                team.unregister();
                            }
                            emptyTeams++;
                        }
                    }

                    NametagMessages.CLEARED_TEAMS.send(sender, emptyTeams, unregister);
                    break;
                default:
                    sendUsage(sender);
                    break;
            }
        }

        return false;
    }

    private boolean isPermissed(CommandSender sender, String permission) {
        return !isNotPermissed(sender, permission);
    }

    private boolean isNotPermissed(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            NametagMessages.NO_PERMISSION.send(sender);
            return true;
        }

        return false;
    }

    private void sendUsagePlayer(CommandSender sender) {
        sender.sendMessage(Utils.format("\n&8» &a&lNametagEdit Player Help &8«"));
        sender.sendMessage(Utils.format("\n\n&8Type a command to get started:"));
        sender.sendMessage(Utils.format("&8» &a/nte player <Player> clear"));
        sender.sendMessage(Utils.format("&8» &a/nte player <Player> prefix <MyPrefix>"));
        sender.sendMessage(Utils.format("&8» &a/nte player <Player> suffix <MySuffix>"));
        sender.sendMessage(Utils.format("&8» &a/nte player <Player> priority <#>"));
    }

    private void sendUsageGroup(CommandSender sender) {
        sender.sendMessage(Utils.format("\n&8» &a&lNametagEdit Player Help &8«"));
        sender.sendMessage(Utils.format("\n\n&8Type a command to get started:"));
        sender.sendMessage(Utils.format("&8» &a/nte group list"));
        sender.sendMessage(Utils.format("&8» &a/nte group add <MyGroup>"));
        sender.sendMessage(Utils.format("&8» &a/nte group remove <MyGroup>"));
        sender.sendMessage(Utils.format("&8» &a/nte group order <GroupOrder>"));
        sender.sendMessage(Utils.format("&8» &a/nte group <MyGroup> clear <prefix/suffix>"));
        sender.sendMessage(Utils.format("&8» &a/nte group <MyGroup> prefix <MyPrefix>"));
        sender.sendMessage(Utils.format("&8» &a/nte group <MyGroup> suffix <MySuffix>"));
        sender.sendMessage(Utils.format("&8» &a/nte group <MyGroup> permission <MyPermission>"));
        sender.sendMessage(Utils.format("&8» &a/nte group <MyGroup> priority <#>"));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Utils.format("\n&8» &a&lNametagEdit Plugin Help &8«"));
        sender.sendMessage(Utils.format("     by Cory and sgtcaze"));
        sender.sendMessage(Utils.format("\n\n&8Type a command to get started:"));
        sender.sendMessage(Utils.format("&8» &a/nte debug"));
        sender.sendMessage(Utils.format("&8» &a/nte reload"));
        sender.sendMessage(Utils.format("&8» &a/nte convert"));
        sender.sendMessage(Utils.format("&8» &a/nte player"));
        sender.sendMessage(Utils.format("&8» &a/nte group"));
    }

    /**
     * Handles /nte reload
     */
    private void cmdReload(CommandSender sender) {
        if (isPermissed(sender, "nametagedit.reload")) {
            handler.reload();
            NametagMessages.RELOADED_DATA.send(sender);
        }
    }

    /**
     * Handles /nte convert
     */
    private void cmdConvert(CommandSender sender, String[] args) {
        if (isNotPermissed(sender, "nametagedit.convert")) return;
        if (args.length != 4) {
            NametagMessages.USAGE_CONVERT.send(sender);
        } else {
            boolean sourceIsFile = args[1].equalsIgnoreCase("file");
            boolean destinationIsSQL = args[2].equalsIgnoreCase("db");
            boolean legacy = args[3].equalsIgnoreCase("true");
            NametagMessages.CONVERSION.send(sender, "groups & players", sourceIsFile ? "file" : "mysql", destinationIsSQL ? "mysql" : "file", legacy);

            if (sourceIsFile && !destinationIsSQL && legacy) {
                new Converter().legacyConversion(sender, handler.getPlugin());
            } else if ((destinationIsSQL && sourceIsFile) || (!sourceIsFile && !destinationIsSQL)) {
                new ConverterTask(!destinationIsSQL, sender, handler.getPlugin()).runTaskAsynchronously(handler.getPlugin());
            }
        }
    }

    /**
     * Handles /nte player
     */
    private void cmdPlayer(CommandSender sender, String[] args) {
        if (args.length == 3) {
            if (!args[2].equalsIgnoreCase("clear")) {
                sendUsagePlayer(sender);
                return;
            }

            if (isNotPermissed(sender, "nametagedit.clear.self")) return;

            String targetName = args[1];

            if (!sender.hasPermission("nametagedit.clear.others") && !targetName.equalsIgnoreCase(sender.getName())) {
                NametagMessages.MODIFY_OWN_TAG.send(sender);
                return;
            }

            handler.clear(sender, targetName);
            handler.applyTagToPlayer(Bukkit.getPlayerExact(targetName));
        } else if (args.length >= 4) {
            switch (args[2].toLowerCase()) {
                case "prefix":
                case "suffix":
                    if (isNotPermissed(sender, "nametagedit.edit.self")) return;

                    String targetName = args[1];

                    if (!sender.hasPermission("nametagedit.edit.others") && !targetName.equalsIgnoreCase(sender.getName())) {
                        NametagMessages.MODIFY_OWN_TAG.send(sender);
                        return;
                    }

                    NametagEvent.ChangeType changeType = args[2].equalsIgnoreCase("prefix") ? NametagEvent.ChangeType.PREFIX : NametagEvent.ChangeType.SUFFIX;
                    handler.save(sender, targetName, changeType, Utils.format(args, 3, args.length));
                    break;
                case "priority":
                    if (isNotPermissed(sender, "nametagedit.edit.self")) return;

                    String priorityName = args[1];

                    if (!sender.hasPermission("nametagedit.edit.others") && !priorityName.equalsIgnoreCase(sender.getName())) {
                        NametagMessages.MODIFY_OWN_TAG.send(sender);
                        break;
                    }

                    setupPriority(sender, true, priorityName, args[3]);
                    break;
                default:
                    sendUsagePlayer(sender);
            }
        } else {
            sendUsagePlayer(sender);
        }
    }

    /**
     * Modifies groups
     */
    private void cmdGroups(CommandSender sender, String[] args) {
        if (isNotPermissed(sender, "nametagedit.groups")) return;
        if (args.length < 2) {
            sendUsageGroup(sender);
        } else {
            if (args[1].equalsIgnoreCase("list")) {
                sender.sendMessage(Utils.format("&f&lLoaded Groups"));
                for (GroupData groupData : handler.getGroupData()) {
                    sender.sendMessage(Utils.format("&6Group: &f" + groupData.getGroupName() + " &6Permission: &f" + groupData.getPermission()
                            + " &6Formatted: " + groupData.getPrefix() + sender.getName() + groupData.getSuffix()));
                }
            } else if (args[1].equalsIgnoreCase("order")) {
                if (args.length <= 2) {
                    sendUsageGroup(sender);
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

                    handler.addGroup(new GroupData(group, "", "", "", new Permission("my.perm", PermissionDefault.FALSE), -1));
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
                        NametagMessages.GROUP_VALUE.send(sender, group, "permission", args[3]);
                    } else if (args[2].equalsIgnoreCase("prefix")) {
                        String value = Utils.format(args, 3, args.length).replace("\"", "");
                        groupData.setPrefix(Utils.format(value));
                        handler.applyTags();
                        handler.save(groupData);
                        NametagMessages.GROUP_VALUE.send(sender, group, "prefix", Utils.format(value));
                    } else if (args[2].equalsIgnoreCase("suffix")) {
                        String value = Utils.format(args, 3, args.length).replace("\"", "");
                        groupData.setSuffix(Utils.format(value));
                        handler.applyTags();
                        handler.save(groupData);
                        NametagMessages.GROUP_VALUE.send(sender, group, "suffix", Utils.format(value));
                    } else if (args[2].equalsIgnoreCase("clear")) {
                        boolean prefix = args[3].equalsIgnoreCase("prefix");
                        if (prefix) {
                            groupData.setPrefix("&f");
                        } else {
                            groupData.setSuffix("&f");
                        }
                        handler.applyTags();
                        handler.save(groupData);
                        NametagMessages.GROUP_VALUE_CLEARED.send(sender, prefix ? "prefix" : "suffix", group);
                    } else if (args[2].equalsIgnoreCase("priority")) {
                        setupPriority(sender, false, group, args[3]);
                    }
                } else {
                    sendUsageGroup(sender);
                }
            }
        }
    }

    private void setupPriority(CommandSender sender, boolean player, String key, String number) {
        int priority;

        try {
            priority = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            NametagMessages.NOT_A_NUMBER.send(sender, number);
            return;
        }

        if (priority < -1 || priority == 0) {
            NametagMessages.INVALID_SORT_PRIORITY.send(sender);
            return;
        }

        handler.save(sender, player, key, priority);
        NametagMessages.SET_PRIORITY.send(sender, priority, key);
    }

}