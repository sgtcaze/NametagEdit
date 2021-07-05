package com.nametagedit.plugin.web;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.storage.AbstractConfig;
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
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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

        Gson gson = new Gson();
        JsonObject json = new JsonObject();
        JsonObject data = new JsonObject();

        AbstractConfig config = plugin.getHandler().getAbstractConfig();

        Collection<GroupData> groups = config.groups().join();
        Collection<PlayerData> players = config.players().join();

        data.add("players", gson.toJsonTree(players));
        data.add("groups", gson.toJsonTree(groups));

        json.addProperty("player", player.getName());
        json.add("data",data);

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

    @SuppressWarnings("UnstableApiUsage")
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

        Gson gson = new Gson();

        HttpURLConnection connection = this.createConnection(code);
        connection.setRequestMethod("GET");

        // Read the response from the server
        JsonObject response = this.readResponse(connection);
        JsonObject data = response.get("data").getAsJsonObject();

        Type playersType = new TypeToken<ArrayList<PlayerData>>(){}.getType();
        Type groupsType = new TypeToken<ArrayList<GroupData>>(){}.getType();

        Collection<PlayerData> players = gson.fromJson(data.get("players"), playersType);
        Collection<GroupData> groups = gson.fromJson(data.get("groups"), groupsType);

        NametagHandler handler = plugin.getHandler();

        groups.stream()
                .filter(Objects::nonNull)
                .forEach((group) -> {
                    plugin.debug("Importing " + group.getGroupName() + "...");
                    group.setPermission(group.getPermission());
                    handler.deleteGroup(group);
                    handler.addGroup(group);
                });

        players.stream()
                .filter(Objects::nonNull)
                .forEach((player) -> {
                    plugin.debug("Importing " + player.getName() + "...");
                    handler.removePlayerData(player.getUuid());
                    handler.storePlayerData(player.getUuid(),player);
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
