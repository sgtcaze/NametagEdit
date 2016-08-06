package com.nametagedit.plugin.api.data;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class FakeTeam {

    private static int ID = 0;
    private final ArrayList<String> members = new ArrayList<>();
    private String name;
    private String prefix = "";
    private String suffix = "";

    public FakeTeam(String prefix, String suffix) {
        this(prefix, suffix, "NTE-" + ++ID);
    }

    public FakeTeam(String prefix, String suffix, String name) {
        this.name = name == null ? "NTE-" + ++ID : name;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public boolean isMember(String player) {
        return members.contains(player);
    }

    public boolean isSimilar(String prefix, String suffix) {
        return this.prefix.equals(prefix) && this.suffix.equals(suffix);
    }

    public boolean isSimilar(FakeTeam fakeTeam) {
        return fakeTeam != null && fakeTeam.getPrefix().equals(prefix) && fakeTeam.getSuffix().equals(suffix);
    }

}