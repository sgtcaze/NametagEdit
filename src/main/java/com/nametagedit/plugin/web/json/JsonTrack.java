package com.nametagedit.plugin.web.json;

import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
@Getter
public class JsonTrack {
    private final String type;
    private final String id;
    private final String prefix;
    private final String suffix;
    private final int sortPriority;
    @Nullable private final String permission;

    @NotNull
    @Contract("_ -> new")
    public static JsonTrack toTrack(@NotNull GroupData group) {
        return new JsonTrack("group",group.getGroupName(),group.getPrefix(),group.getSuffix(),group.getSortPriority(),group.getPermission());
    }

    @NotNull
    @Contract("_ -> new")
    public static JsonTrack toTrack(@NotNull PlayerData player) {
        return new JsonTrack("player",player.getUuid().toString(),player.getPrefix(),player.getSuffix(),player.getSortPriority(),null);
    }
}
