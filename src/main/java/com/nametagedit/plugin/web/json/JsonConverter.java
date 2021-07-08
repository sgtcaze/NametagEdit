package com.nametagedit.plugin.web.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.Validate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@AllArgsConstructor
public class JsonConverter {
    private Collection<GroupData> groups;
    private Collection<PlayerData> players;

    @SuppressWarnings("UnstableApiUsage")
    public List<JsonTrack> from(JsonElement element) {
        Type trackType = new TypeToken<List<JsonTrack>>(){}.getType();

        return new Gson().fromJson(element, trackType);
    }

    public JsonElement convert() {
        List<JsonTrack> tracks = new ArrayList<>();

        Validate.notNull(groups);
        Validate.notNull(players);

        groups.forEach((group) -> tracks.add(JsonTrack.toTrack(group)));
        players.forEach((group) -> tracks.add(JsonTrack.toTrack(group)));

        return new Gson().toJsonTree(tracks);
    }
}
