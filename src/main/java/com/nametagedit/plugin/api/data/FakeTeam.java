package com.nametagedit.plugin.api.data;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class FakeTeam {

    private String name;
    private String prefix = "";
    private String suffix = "";

    private static int ID = 0;
    private final ArrayList<String> members = new ArrayList<>();

    public FakeTeam(String prefix, String suffix) {
        this(prefix, suffix, -1);
    }

    public FakeTeam(String prefix, String suffix, int sortPriority) {
        this.name = sortPriority == -1 ? "ID" + ++ID : "0" + sortPriority;
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