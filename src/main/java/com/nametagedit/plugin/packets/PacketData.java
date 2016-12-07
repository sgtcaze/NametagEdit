package com.nametagedit.plugin.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
enum PacketData {

    v1_7("e", "c", "d", "a", "f", "g", "b", "NA"),
    v1_8("g", "c", "d", "a", "h", "i", "b", "NA"),
    v1_9("h", "c", "d", "a", "i", "j", "b", "f"),
    v1_10("h", "c", "d", "a", "i", "j", "b", "f"),
    v1_11("h", "c", "d", "a", "i", "j", "b", "f");

    private String members;
    private String prefix;
    private String suffix;
    private String teamName;
    private String paramInt;
    private String packOption;
    private String displayName;
    private String push;

}
