package net.wtako.WTAKOFungeon.Utils;

import java.util.List;

import net.wtako.WTAKOFungeon.Main;

import org.bukkit.configuration.file.FileConfiguration;

public enum Config {
    MIN_FUNGEON_TIME_LIMIT_SECONDS("fungeon.min.fungeon-time-limit-seconds", 60),
    MAX_KICKS_PER_PERSON_AND_ROUND("fungeon.kick.max-kicks-per-person-and-round", 3),
    KICKED_DELAY_SECONDS("fungeon.kick.kicked-delay-seconds", 60),
    DEFAULT_FUNGEON_TIME_LIMIT_SECONDS("fungeon.default.fungeon-time-limit-seconds", 900),
    DEFAULT_MIN_PLAYERS("fungeon.default.min-players", 3),
    DEFAULT_MAX_PLAYERS("fungeon.default.max-players", 8),
    DEFAULT_WAITING_ROOM_TIME("fungeon.default.waiting-room-time", 30),
    NO_ENEMIES_WAVE_INTERVAL("fungeon.no-enemies-wave-interval", 5),
    OUT_OF_AREA_KICK_TIMEOUT("fungeon.out-of-area-kick-timeout", 10),
    BROADCAST_MESSAGES("fungeon.broadcast-messages", true),
    PLUGIN_ENABLED("system.plugin-enabled", true);

    public static void saveAll() {
        final FileConfiguration config = Main.getInstance().getConfig();
        for (final Config setting: Config.values()) {
            if (!config.contains(setting.getPath())) {
                config.set(setting.getPath(), setting.getValue());
            }
        }
        Main.getInstance().saveConfig();
    }

    private String path;

    private Object value;

    Config(String path, Object var) {
        this.path = path;
        final FileConfiguration config = Main.getInstance().getConfig();
        if (config.contains(path)) {
            value = config.get(path);
        } else {
            value = var;
        }
    }

    public boolean getBoolean() {
        return (boolean) value;
    }

    public int getInt() {
        if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        return (int) value;
    }

    public long getLong() {
        return Integer.valueOf(getInt()).longValue();
    }

    public double getDouble() {
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        return (double) value;
    }

    public String getPath() {
        return path;
    }

    public String getString() {
        return (String) value;
    }

    @SuppressWarnings("unchecked")
    public List<String> getStrings() {
        return (List<String>) value;
    }

    public Object getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public List<Object> getValues() {
        return (List<Object>) value;
    }

}