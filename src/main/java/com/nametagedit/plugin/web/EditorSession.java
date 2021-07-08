package com.nametagedit.plugin.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.storage.AbstractConfig;
import com.nametagedit.plugin.web.json.JsonConverter;
import com.nametagedit.plugin.web.json.JsonTrack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RequiredArgsConstructor
@Getter
public class EditorSession {
    private static final String EDITOR = "http://localhost:3000/";
    private static final URL PASTE;
    private final NametagEdit plugin;
    private final CommandSender player;
    @Setter
    @Nullable private String code;

    static {
        URL paste = null;

        // todo edit
        try {
            paste = new URL("http://localhost:8080/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        PASTE = paste;
    }

    @NotNull
    public static EditorSession from(NametagEdit plugin, CommandSender player, String code) {
        EditorSession session = new EditorSession(plugin, player);
        session.setCode(code);
        return session;
    }

    public void startSession() throws IOException {
        if (plugin.getServer().isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    this.startSession();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return;
        }

        JsonObject json = new JsonObject();

        AbstractConfig config = plugin.getHandler().getAbstractConfig();

        Collection<GroupData> groups = config.groups().join();
        Collection<PlayerData> players = config.players().join();

        JsonElement data = new JsonConverter(groups,players).convert();

        json.addProperty("player", player.getName());
        json.add("tracks",data);

        // Create a connection to the paste server and make a request
        HttpURLConnection connection = this.createConnection("post");
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");

        try(OutputStream os = connection.getOutputStream()) {
            byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read the response from the server
        JsonObject response = this.readResponse(connection);
        this.code = response.get("key").getAsString();
        connection.disconnect();
    }

    public void retrieveData() throws IOException {
        if (plugin.getServer().isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    this.retrieveData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return;
        }

        HttpURLConnection connection = this.createConnection(code);
        connection.setRequestMethod("GET");

        // Read the response from the server
        JsonObject response = this.readResponse(connection);
        JsonObject data = response.get("tracks").getAsJsonObject();

        List<JsonTrack> tracks = new JsonConverter().from(data);

        NametagHandler handler = plugin.getHandler();

        tracks.forEach((track) -> {
            plugin.debug("Importing " + track.getId() + "...");

            switch (track.getType().toLowerCase()) {
                case "group":
                    GroupData group = new GroupData();
                    group.setPermission(track.getPermission());
                    group.setGroupName(track.getId());
                    group.setPrefix(track.getPrefix());
                    group.setSuffix(track.getSuffix());
                    group.setSortPriority(track.getSortPriority());

                    handler.deleteGroup(group);
                    handler.addGroup(group);
                    break;
                case "player":
                    PlayerData player = new PlayerData();
                    player.setUuid(UUID.fromString(track.getId()));
                    player.setPrefix(track.getPrefix());
                    player.setSuffix(track.getSuffix());
                    player.setSortPriority(track.getSortPriority());

                    handler.removePlayerData(player.getUuid());
                    handler.storePlayerData(player.getUuid(),player);
                    break;
                default:
                    break;
            }
        });

        connection.disconnect();
    }

    private JsonObject readResponse(URLConnection connection) throws IOException {
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            return new JsonParser().parse(response.toString()).getAsJsonObject();
        }
    }

    @NotNull
    private HttpURLConnection createConnection(String page) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(PASTE + page).openConnection();
        connection.setRequestProperty("User-Agent", String.format("%s/%s (%s)",plugin.getName(),plugin.getDescription().getVersion(),this.getClass().getName()));
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        return connection;
    }

    public String formatUrl() {
        return EDITOR + code;
    }
}
