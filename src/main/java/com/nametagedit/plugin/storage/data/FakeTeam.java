package com.nametagedit.plugin.storage.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FakeTeam {

    private String name;
    private String prefix = "";
    private String suffix = "";

    public boolean isSimilar(FakeTeam fakeTeam) {
        return fakeTeam != null && fakeTeam.getPrefix().equals(prefix) && fakeTeam.getSuffix().equals(suffix);
    }

}