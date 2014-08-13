package net.wtako.WTAKOFungeon.Utils;

import java.util.Arrays;
import java.util.List;

import net.wtako.WTAKOFungeon.Main;

import org.bukkit.configuration.file.FileConfiguration;

public enum Config {

    DEBUG("system.debug", true),
    SHIT("lol.shit", false),
    SHIT_TIMES("lol.shit-times", 42),
    HAPPY_TIMES("lol.happy-times", 2),
    LIST_OF_NAMES("lol.list-of-names", Arrays.asList("Peter", "Mary", "John"));

    private String path;
    private Object value;

    Config(String path, Object var) {
        this.path = path;
        FileConfiguration config = Main.getInstance().getConfig();
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
        FileConfiguration config = Main.getInstance().getConfig();
        for (Config setting: Config.values()) {
            if (!config.contains(setting.getPath())) {
                config.set(setting.getPath(), setting.getValue());
            }
        }
        Main.getInstance().saveConfig();
    }

}