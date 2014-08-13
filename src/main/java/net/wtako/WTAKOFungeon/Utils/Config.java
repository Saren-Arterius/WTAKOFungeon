package net.wtako.WTAKOFungeon.Utils;

import java.util.List;

import net.wtako.WTAKOFungeon.Main;

import org.bukkit.configuration.file.FileConfiguration;

public enum Config {
    DEFAULT_TIME_LIMIT_SECONDS("default.time-limit-seconds", 1800),
    DEFAULT_MIN_PLAYERS("default.min-players", 3),
    DEFAULT_MAX_PLAYERS("default.max-players", 8),
    DEFAULT_WAIT_TIME("default.wait-time", 60),
    PLUGIN_ENABLED("system.plugin-enabled", true);

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

    public Object getValue() {
        return value;
    }

    public boolean getBoolean() {
        return (boolean) value;
    }

    public String getString() {
        return (String) value;
    }

    public int getInt() {
        return (int) value;
    }

    public long getLong() {
        return (long) value;
    }

    public double getDouble() {
        return (double) value;
    }

    public String getPath() {
        return path;
    }

    @SuppressWarnings("unchecked")
    public List<Object> getValues() {
        return (List<Object>) value;
    }

    @SuppressWarnings("unchecked")
    public List<String> getStrings() {
        return (List<String>) value;
    }

    public static void saveAll() {
        final FileConfiguration config = Main.getInstance().getConfig();
        for (final Config setting: Config.values()) {
            if (!config.contains(setting.getPath())) {
                config.set(setting.getPath(), setting.getValue());
            }
        }
        Main.getInstance().saveConfig();
    }

}