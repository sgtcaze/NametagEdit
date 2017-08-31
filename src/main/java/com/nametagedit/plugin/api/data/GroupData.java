package com.nametagedit.plugin.api.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

/**
 * This class represents a group nametag. There
 * are several properties available.
 */
@Data
@AllArgsConstructor
public class GroupData implements INametag {

    private String groupName;
    private String prefix;
    private String suffix;
    private String permission;
    private Permission bukkitPermission;
    private int sortPriority;

    public GroupData() {

    }

    public void setPermission(String permission) {
        this.permission = permission;
        bukkitPermission = new Permission(permission, PermissionDefault.FALSE);
    }

    @Override
    public boolean isPlayerTag() {
        return false;
    }

}