# NametagEdit

[![Build Status](https://travis-ci.org/sgtcaze/NametagEdit.svg?branch=master)](https://travis-ci.org/sgtcaze/NametagEdit)
[![Dev Builds](https://img.shields.io/badge/Jenkins-Development%20Builds-lightgrey.svg)](http://ci.playmc.cc/job/NametagEdit/)
[![Support](https://img.shields.io/badge/Minecraft-1.7--1.10-red.svg)](documentation/Support)
[![Spigot](https://img.shields.io/badge/Spigot-Project%20Page-yellow.svg)](https://www.spigotmc.org/resources/nametagedit.3836/)
[![JDK](https://img.shields.io/badge/JDK-1.7-blue.svg)](http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html)

This plugin allows users to add up to 16 characters before and after their name. Individual tags can be created for players, or a group can be created that can be joined via permissions.

NametagEdit has support for EssentialsGroupManager, PermissionsEx and zPermissions. If a user changes groups or permissions, their tag is automatically updated.

* [Official Project Page](https://www.spigotmc.org/resources/nametagedit.3836/)
* [Development Builds](http://ci.playmc.cc/job/NametagEdit)

# Quick Links
* [API & Developers](documentation/Developers.creole)
* [Permissions](documentation/Permissions.creole)
* [Commands](documentation/Commands.creole)
* [Configuration](documentation/Configuration.creole)

# Features
✔ Converters to and from MySQL and FlatFile

✔ Efficient Flatfile support and MySQL connection pooling

✔ PermissionsEX, ZPermissions and GroupManager support

✔ Sortable Group/Player Tags in tab

✔ [MDvW Placeholder API](https://www.spigotmc.org/resources/mvdwplaceholderapi.11182/) Support

✔ [Clip Placeholder API](https://www.spigotmc.org/resources/placeholderapi.6245/) Support 

# Frequently Asked Questions
#### Q: Will this allow me to change my skin and name?
**A:** No. This plugin creates fake scoreboard teams with packets.

#### Q: My client crashes with the reason "Cannot remove from ID#". Why is this?
**A:** Due to how scoreboards were implemented in Minecraft, a player cannot belong to two teams. Any two plugins that use packets or the Bukkit API which alter team prefixes/suffixes will have conflicts. There is currently no way around this.

#### Q: Can I sort nametags in the tab list?
**A:** Yes. Read up on how to use it [here](documentation/Configuration.creole)

# Incompatible Plugins
✖ Coming Soon

## HELP! My nametags are not displaying!
Click [here](documentation/Support.creole) for common mistakes and proposed fixes.