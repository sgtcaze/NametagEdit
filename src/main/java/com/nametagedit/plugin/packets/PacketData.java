package com.nametagedit.plugin.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
enum PacketData {

    v1_7("e", "c", "d", "a", "f", "g", "b", "NA", "NA", "NA", "NA"),
    cauldron("field_149317_e", "field_149319_c", "field_149316_d", "field_149320_a",
            "field_149314_f", "field_149315_g", "field_149318_b", "NA", "NA", "NA", "NA"),
    v1_8("g", "c", "d", "a", "h", "i", "b", "NA", "NA", "e", "NA"),
    v1_9("h", "c", "d", "a", "i", "j", "b", "NA", "f", "e", "NA"),
    v1_10("h", "c", "d", "a", "i", "j", "b", "NA", "f", "e", "NA"),
    v1_11("h", "c", "d", "a", "i", "j", "b", "NA", "f", "e", "NA"),
    v1_12("h", "c", "d", "a", "i", "j", "b", "NA", "f", "e", "NA"),
    v1_13("h", "c", "d", "a", "i", "j", "b", "g", "f", "e", "NA"),
    v1_14("h", "c", "d", "a", "i", "j", "b", "g", "f", "e", "NA"),
    v1_15("h", "c", "d", "a", "i", "j", "b", "g", "f", "e", "NA"),
    v1_16("h", "c", "d", "a", "i", "j", "b", "g", "f", "e", "NA"),
    v1_17("j", "b", "c", "i", "h", "g", "a", "f", "e", "d", "k"),
    v1_18("j", "b", "c", "i", "h", "g", "a", "f", "e", "d", "k"),
    v1_19("j", "b", "c", "i", "h", "g", "a", "f", "e", "d", "k"),
    v1_20("j", "b", "c", "i", "h", "g", "a", "f", "e", "d", "k");

    private final String members;
    private final String prefix;
    private final String suffix;
    private final String teamName;
    private final String paramInt;
    private final String packOption;
    private final String displayName;
    private final String color;
    private final String push;
    private final String visibility;
    // 1.17+
    private final String params;

}
