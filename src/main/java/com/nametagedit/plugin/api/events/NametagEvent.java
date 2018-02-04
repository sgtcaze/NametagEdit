package com.nametagedit.plugin.api.events;

import com.nametagedit.plugin.api.data.Nametag;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This class represents an Event that is fired when a
 * nametag is changed.
 */
public class NametagEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled;

    @Getter
    @Setter
    @Deprecated
    private String value;

    @Getter
    @Setter
    private Nametag nametag;

    @Getter
    @Setter
    private String player;

    @Getter
    @Setter
    private ChangeType changeType;

    @Getter
    @Setter
    private ChangeReason changeReason;

    @Getter
    @Setter
    private StorageType storageType;

    public NametagEvent(String player, String value) {
        this(player, value, ChangeType.UNKNOWN);
    }

    public NametagEvent(String player, String value, Nametag nametag, ChangeType type) {
        this(player, value, type);
        this.nametag = nametag;
    }

    public NametagEvent(String player, String value, ChangeType changeType) {
        this(player, value, changeType, StorageType.MEMORY, ChangeReason.UNKNOWN);
    }

    public NametagEvent(String player, String value, ChangeType changeType, ChangeReason changeReason) {
        this(player, value, changeType, StorageType.MEMORY, changeReason);
    }

    public NametagEvent(String player, String value, ChangeType changeType, StorageType storageType, ChangeReason changeReason) {
        this.player = player;
        this.value = value;
        this.changeType = changeType;
        this.storageType = storageType;
        this.changeReason = changeReason;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public enum ChangeReason {
        API, PLUGIN, UNKNOWN
    }

    public enum ChangeType {
        PREFIX, SUFFIX, GROUP, CLEAR, PREFIX_AND_SUFFIX, RELOAD, UNKNOWN
    }

    public enum StorageType {
        MEMORY, PERSISTENT
    }

}