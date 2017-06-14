package com.nametagedit.plugin.api.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.UUID;

/**
 * This class represents a Scoreboard Team. It is used
 * to keep track of the current members of a Team, and
 * is responsible for
 */
@Data
public class FakeTeam {

    // Because some networks use NametagEdit on multiple servers, we may have clashes
    // with the same Team names. The UNIQUE_ID ensures there will be no clashing.
    private static final String UNIQUE_ID = UUID.randomUUID().toString().replaceAll("[^a-zA-Z]", "").toUpperCase().substring(0, 5);
    // This represents the number of FakeTeams that have been created.
    // It is used to generate a unique Team name.
    private static int ID = 0;
    private final ArrayList<String> members = new ArrayList<>();
    private String name;
    private String prefix = "";
    private String suffix = "";

    public FakeTeam(String prefix, String suffix, int sortPriority) {
        this.name = sortPriority <= 0 ? UNIQUE_ID + ++ID : UNIQUE_ID + "-" + sortPriority + "_" + ++ID;
        // It is possible the names of the Team exceeded the length of 16 in the past,
        // and caused crashes as a result. This is a layer of protection against that.
        this.name = this.name.length() > 16 ? this.name.substring(0, 16) : this.name;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public void addMember(String player) {
        if (!members.contains(player)) {
            members.add(player);
        }
    }

    public boolean isSimilar(String prefix, String suffix) {
        return this.prefix.equals(prefix) && this.suffix.equals(suffix);
    }

}