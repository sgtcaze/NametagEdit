package com.nametagedit.plugin.storage.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

@Getter
@Setter
@AllArgsConstructor
public class GroupData {

    private String groupName;
    private String prefix;
    private String suffix;
    private String permission;
    private Permission bukkitPermission;

    public GroupData() {

    }

    public void refresh() {
        bukkitPermission = new Permission(permission, PermissionDefault.FALSE);
    }

}