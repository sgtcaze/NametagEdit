package com.nametagedit.plugin.api.data;

import lombok.Data;

import java.util.ArrayList;

/**
 * This class represents a Scoreboard Team. It is used
 * to keep track of the current members of a Team, and
 * is responsible for
 */
@Data
public class FakeTeam {

    // This represents the number of FakeTeams that have been created.
    // It is used to generate a unique Team name.
    private static int ID = 0;

    private final ArrayList<String> members = new ArrayList<>();
    private String name;
    private String prefix = "";
    private String suffix = "";

    public FakeTeam(String prefix, String suffix, String name) {
        this.name = name == null ? "NTE-" + ++ID : name + ++ID;
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