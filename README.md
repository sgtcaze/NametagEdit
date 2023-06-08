# NametagEdit

[![Dev Builds](https://img.shields.io/badge/Jenkins-Development%20Builds-lightgrey.svg)](https://ci.nametagedit.com/job/NametagEdit)
[![Support](https://img.shields.io/badge/Minecraft-1.7--1.19-green.svg)](documentation/Support)
[![Spigot](https://img.shields.io/badge/Spigot-Project%20Page-yellow.svg)](https://www.spigotmc.org/resources/nametagedit.3836/)
[![JDK](https://img.shields.io/badge/JDK-1.8-blue.svg)](https://jdk.java.net/java-se-ri/8-MR3)
[![NametagEditAPI](https://img.shields.io/badge/NTE-Developer%20API-ff69b4.svg)](documentation/Developers.creole)

This plugin allows users to add up to 16 characters before and after their name. Individual tags can be created for players, or a group can be created that can be joined via permissions.

* Minecraft `1.7.x to 1.12.x` has a max 16-character limit.
* Minecraft `1.13.x to 1.20.x` has a max 256-character limit.
* Minecraft `1.16.x to 1.20.x` has `hex color` support.

NametagEdit has support for EssentialsGroupManager, PermissionsEx, zPermissions, LuckPerms and LibsDisguises. If a user changes groups or permissions, their tag is automatically updated.

* [Official Project Page](https://www.spigotmc.org/resources/nametagedit.3836/)
* [Development Builds](https://ci.nametagedit.com/job/NametagEdit)

# Quick Links
* [API & Developers](documentation/Developers.creole)
* [Permissions](documentation/Permissions.creole)
* [Commands](documentation/Commands.creole)
* [Configuration](documentation/Configuration.creole)
* [Common Issues](documentation/Support.creole)

# Features
✔ Converters to and from MySQL and FlatFile

✔ Efficient Flatfile support and MySQL connection pooling

✔ PermissionsEX, zPermissions, GroupManager, LuckPerms (https://www.spigotmc.org/resources/luckperms-an-advanced-permissions-plugin.28140/) support

✔ Sortable Group/Player Tags in tab

✔ [MVdW Placeholder API](https://www.spigotmc.org/resources/mvdwplaceholderapi.11182/) Support

✔ [Clip Placeholder API](https://www.spigotmc.org/resources/placeholderapi.6245/) Support 

✔ [Guilds](https://www.spigotmc.org/resources/guilds.66176/) Support 

# Frequently Asked Questions
#### Q: Will this allow me to change my skin and name?
**A:** No. This plugin creates fake scoreboard teams with packets.

#### Q: My client crashes with the reason "Cannot remove from ID#". Why is this?
**A:** Due to how scoreboards were implemented in Minecraft, a player cannot belong to two teams. Any two plugins that use packets or the Bukkit API which alter team prefixes/suffixes will have conflicts. There is currently no way around this.

#### Q: My nametag is cut short, even with LongTags enabled!
**A:** LongTags is only able to disable a longer nametag in the tablist. The name above your head has a different limit (16 characters for prefix and suffix.) We are unable to change this, as this limit is imposed by Mojang.

#### Q: Can I sort nametags in the tab list?
**A:** Yes. Read up on how to use it [here](documentation/Configuration.creole)

# TODO

# Incompatible Plugins
✖ Any plugin that creates NPCs that share the same username as players who have 'NametagEdit' nametags

✖ Any plugin that uses Team color sidebars without specifically supporting NametagEdit
