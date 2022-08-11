package com.nametagedit.plugin.api.data;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.utils.Utils;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Scoreboard Team. It is used
 * to keep track of the current members of a Team, and
 * is responsible for
 */
@Data
public class FakeTeam {

    @Getter private static final List<String> createdTeamsNames = new ArrayList<>();

    // Because some networks use NametagEdit on multiple servers, we may have clashes
    // with the same Team names. The UNIQUE_ID ensures there will be no clashing.
    private static final String UNIQUE_ID = Utils.generateUUID();
    // This represents the number of FakeTeams that have been created.
    // It is used to generate a unique Team name.
    private static int ID = 0;
    private final ArrayList<String> members = new ArrayList<>();
    private String name;
    private String prefix;
    private String suffix;
    private boolean visible = true;

    public FakeTeam(String prefix, String suffix, int sortPriority, boolean playerTag) {
        String generatedName = UNIQUE_ID + "_" + getNameFromInput(sortPriority) + ++ID + (playerTag ? "+P" : "");
        while(createdTeamsNames.contains(generatedName)){
            generatedName = Utils.generateUUID() + "_" + getNameFromInput(sortPriority) + ++ID + (playerTag ? "+P" : "");
        }
        this.name = generatedName;

        if(NametagEdit.getInstance().getVersion().getProtocolNumber() >= 393){
            this.name = this.name.length() > 256 ? this.name.substring(0, 256) : this.name;
        }else{
            this.name = this.name.length() > 16 ? this.name.substring(0, 16) : this.name;
        }

        this.prefix = prefix;
        this.suffix = suffix;

        createdTeamsNames.add(this.name);
    }

    public void addMember(String player) {
        if (!members.contains(player)) {
            members.add(player);
        }
    }

    public boolean isSimilar(String prefix, String suffix, boolean visible) {
        return this.prefix.equals(prefix) && this.suffix.equals(suffix) && this.visible == visible;
    }

    /**
     * This is a special method to sort nametags in
     * the tablist. It takes a priority and converts
     * it to an alphabetic representation to force a
     * specific sort.
     *
     * @param input the sort priority
     * @return the team name
     */
    private String getNameFromInput(int input) {
        if (input < 0) return "Z";
        char letter = (char) ((input / 5) + 65);
        int repeat = input % 5 + 1;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < repeat; i++) {
            builder.append(letter);
        }
        return builder.toString();
    }

}
