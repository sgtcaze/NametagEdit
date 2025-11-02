package com.nametagedit.plugin.packets;

import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class PacketWrapper {

    public String error;
    private final int param;
    private final Object packet = PacketAccessor.createPacket();
    private final Object packetParams = PacketAccessor.createPacketParams();

    private static Method CraftChatMessage;
    private static Class<? extends Enum> typeEnumChatFormat;
    private static Enum RESET_COLOR;

    static {
        try {
            if (!PacketAccessor.isLegacyVersion()) {
                if (!PacketAccessor.isParamsVersion()) {
                    typeEnumChatFormat = (Class<? extends Enum>) Class.forName("net.minecraft.server." + PacketAccessor.SPIGOT_MAPPED_CRAFT_BUKKIT_VERSION + ".EnumChatFormat");
                } else {
                    // 1.17+
                    typeEnumChatFormat = (Class<? extends Enum>) Class.forName("net.minecraft.EnumChatFormat");
                }
                Class<?> typeCraftChatMessage = Class.forName(PacketAccessor.CRAFT_BUKKIT_PACKAGE + ".util.CraftChatMessage");
                CraftChatMessage = typeCraftChatMessage.getMethod("fromString", String.class);
                RESET_COLOR = Enum.valueOf(typeEnumChatFormat, "RESET");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PacketWrapper(String name, int param, List<String> members) {
        if (param != 3 && param != 4) {
            throw new IllegalArgumentException("Method must be join or leave for player constructor");
        }
        this.param = param;
        setupDefaults(name, param);
        setupMembers(members);
    }

    @SuppressWarnings("unchecked")
    public PacketWrapper(String name, String prefix, String suffix, int param, Collection<?> players, boolean visible) {
        this.param = param;
        setupDefaults(name, param);
        if (param == 0 || param == 2) {
            try {
                if (PacketAccessor.isLegacyVersion()) {
                    PacketAccessor.DISPLAY_NAME.set(packet, name);
                    PacketAccessor.PREFIX.set(packet, prefix);
                    PacketAccessor.SUFFIX.set(packet, suffix);
                } else {
                    String color = ChatColor.getLastColors(prefix);
                    String colorCode = null;
                    Enum<?> colorEnum = null;

                    if (!color.isEmpty()) {
                        colorCode = color.substring(color.length() - 1);
                        String chatColor = ChatColor.getByChar(colorCode).name();

                        if (chatColor.equalsIgnoreCase("MAGIC"))
                            chatColor = "OBFUSCATED";

                        colorEnum = Enum.valueOf(typeEnumChatFormat, chatColor);
                    }

                    if (colorCode != null)
                        suffix = ChatColor.getByChar(colorCode) + suffix;

                    if (!PacketAccessor.isParamsVersion()) {
                        PacketAccessor.TEAM_COLOR.set(packet, colorEnum == null ? RESET_COLOR : colorEnum);
                        PacketAccessor.DISPLAY_NAME.set(packet, Array.get(CraftChatMessage.invoke(null, name), 0));
                        PacketAccessor.PREFIX.set(packet, Array.get(CraftChatMessage.invoke(null, prefix), 0));
                        PacketAccessor.SUFFIX.set(packet, Array.get(CraftChatMessage.invoke(null, suffix), 0));
                    } else {
                        // 1.17+
                        PacketAccessor.TEAM_COLOR.set(packetParams, colorEnum == null ? RESET_COLOR : colorEnum);
                        PacketAccessor.DISPLAY_NAME.set(packetParams, Array.get(CraftChatMessage.invoke(null, name), 0));
                        PacketAccessor.PREFIX.set(packetParams, Array.get(CraftChatMessage.invoke(null, prefix), 0));
                        PacketAccessor.SUFFIX.set(packetParams, Array.get(CraftChatMessage.invoke(null, suffix), 0));
                    }
                }

                if (!PacketAccessor.isParamsVersion()) {
                    PacketAccessor.PACK_OPTION.set(packet, 1);

                    if (PacketAccessor.VISIBILITY != null) {
                        // VISIBILITY can be a String field in older versions or an Enum in newer versions
                        Object value = getVisibilityValue(PacketAccessor.VISIBILITY, visible, false);
                        PacketAccessor.VISIBILITY.set(packet, value);
                    }
                } else {
                    // 1.17+
                    PacketAccessor.PACK_OPTION.set(packetParams, 1);

                    if (PacketAccessor.VISIBILITY != null) {
                        Object value = getVisibilityValue(PacketAccessor.VISIBILITY, visible, false);
                        PacketAccessor.VISIBILITY.set(packetParams, value);
                    }
                }

                if (param == 0) {
                    ((Collection) PacketAccessor.MEMBERS.get(packet)).addAll(players);
                }
            } catch (Exception e) {
                error = e.getMessage();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setupMembers(Collection<?> players) {
        try {
            players = players == null || players.isEmpty() ? new ArrayList<>() : players;
            ((Collection) PacketAccessor.MEMBERS.get(packet)).addAll(players);
        } catch (Exception e) {
            error = e.getMessage();
        }
    }

    private void setupDefaults(String name, int param) {
        try {
            PacketAccessor.TEAM_NAME.set(packet, name);
            PacketAccessor.PARAM_INT.set(packet, param);

            if (PacketAccessor.isParamsVersion()) {
                // 1.17+ These null values are not allowed, this initializes them.
                PacketAccessor.MEMBERS.set(packet, new ArrayList<>());
                // Initialize PUSH using helper - in newer versions this field may be an enum (e.g. collisionRule)
                PacketAccessor.PUSH.set(packetParams, getPushValue(PacketAccessor.PUSH, "", true));
                // VISIBILITY puede ser enum o String dependiendo de la versión; usa el helper
                if (PacketAccessor.VISIBILITY != null) {
                    PacketAccessor.VISIBILITY.set(packetParams, getVisibilityValue(PacketAccessor.VISIBILITY, false, true));
                }
                PacketAccessor.TEAM_COLOR.set(packetParams, RESET_COLOR);
            }
            if (NametagHandler.DISABLE_PUSH_ALL_TAGS && PacketAccessor.PUSH != null) {
                if (!PacketAccessor.isParamsVersion()) {
                    PacketAccessor.PUSH.set(packet, getPushValue(PacketAccessor.PUSH, "never", false));
                } else {
                    // 1.17+
                    PacketAccessor.PUSH.set(packetParams, getPushValue(PacketAccessor.PUSH, "never", false));
                }
            }
        } catch (Exception e) {
            error = e.getMessage();
        }
    }

    private Object getVisibilityValue(Field visibilityField, boolean visible, boolean isInitialEmpty) {
        try {
            Class<?> fieldType = visibilityField.getType();
            if (fieldType.isEnum()) {
                // Map to enum constant (try common names in uppercase, fall back to lowercase)
                String name;
                if (isInitialEmpty) {
                    // default initialization: choose ALWAYS as neutral default
                    name = "ALWAYS";
                } else {
                    name = visible ? "ALWAYS" : "NEVER";
                }
                try {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    Object enumVal = Enum.valueOf((Class) fieldType, name);
                    return enumVal;
                } catch (IllegalArgumentException iae) {
                    // try lowercase variant
                    try {
                        @SuppressWarnings({"unchecked", "rawtypes"})
                        Object enumVal = Enum.valueOf((Class) fieldType, name.toLowerCase());
                        return enumVal;
                    } catch (Exception ex) {
                        // give up and return null
                        return null;
                    }
                }
            } else {
                if (isInitialEmpty) return "";
                return visible ? "always" : "never";
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper for setting the PUSH field which in some NMS versions is a String and in others is an Enum
     * (for example collisionRule). This tries to return an enum constant when appropriate or a string.
     *
     * @param pushField The reflected field for PUSH/collisionRule
     * @param value The desired string value (e.g. "never", "always" or "")
     * @param isInitialEmpty true when initializing defaults (map to a neutral enum if needed)
     * @return An enum constant instance or a String depending on the field type, or null on error
     */
    private Object getPushValue(Field pushField, String value, boolean isInitialEmpty) {
        try {
            if (pushField == null) return null;
            Class<?> fieldType = pushField.getType();
            if (fieldType.isEnum()) {
                String name;
                if (isInitialEmpty) {
                    name = "ALWAYS"; // neutral default
                } else {
                    name = (value == null || value.isEmpty()) ? "ALWAYS" : value.toUpperCase();
                }

                // Normalize common variants
                name = name.replace('-', '_').replace(' ', '_');

                try {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    Object enumVal = Enum.valueOf((Class) fieldType, name);
                    return enumVal;
                } catch (IllegalArgumentException iae) {
                    // try lowercase variant of field constants
                    try {
                        @SuppressWarnings({"unchecked", "rawtypes"})
                        Object enumVal = Enum.valueOf((Class) fieldType, name.toLowerCase());
                        return enumVal;
                    } catch (Exception ex) {
                        // give up and return null so reflection won't try to set incompatible value
                        return null;
                    }
                }
            } else {
                if (isInitialEmpty) return "";
                return value == null ? "" : value.toLowerCase();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void constructPacket() {
        try {
            if (PacketAccessor.isParamsVersion()) {
                // 1.17+
                PacketAccessor.PARAMS.set(packet, param == 0 ? Optional.ofNullable(packetParams) : Optional.empty());
            }
        } catch (Exception e) {
            error = e.getMessage();
        }
    }

    public void send() {
        constructPacket();
        PacketAccessor.sendPacket(Utils.getOnline(), packet);
    }

    public void send(Player player) {
        constructPacket();
        PacketAccessor.sendPacket(player, packet);
    }

}