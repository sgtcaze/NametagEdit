# NametagEdit
This plugin allows users to add up to 16 characters before and after their name. Individual tags can be created for players, or a group can be created that can be joined via permissions.

NametagEdit has support for EssentialsGroupManager, PermissionsEx and zPermissions. If a user changes groups or permissions, their tag is automatically updated.

* [Official Project Page](https://www.spigotmc.org/resources/nametagedit.3836/)
* [Development Builds](http://ci.playmc.cc/job/NametagEdit)

# Quick Links
* [API & Developers](documentation/Developers.creole)
* [Permissions](documentation/Permissions.creole)
* [Commands](documentation/Commands.creole)
* [Configuration](documentation/Configuration.creole)

# Frequently Asked Questions
#### Q: Will this allow me to change my skin and name?
**A:** No. This plugin creates fake scoreboard teams and adds players to them.

#### Q: My client crashes with the reason "Cannot remove from ID#". Why is this?
**A:** Due to how scoreboards were implemented in Minecraft, a player cannot belong to two teams. Any two scoreboard plugins, whether through packets or the bukkit scoreboard api - which are basically the same thing, that alter team prefixes/suffixes, will have conflicts. There is currently no way around this.