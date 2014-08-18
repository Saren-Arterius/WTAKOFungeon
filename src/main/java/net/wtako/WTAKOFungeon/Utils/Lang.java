package net.wtako.WTAKOFungeon.Utils;

import net.wtako.WTAKOFungeon.Main;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * An enum for requesting strings from the language file.
 * 
 * @author gomeow
 */
public enum Lang {

    TITLE("[" + Main.getInstance().getName() + "]"),

    FUNGEON("Fungeon"),
    FUNGEON_PLAYERS_FORMAT("{0}/{1}"),
    FUNGEON_TO_STRING_FORMAT("{0}. {1}"),
    COMMAND_HELP_SEPERATOR("&6 | &a"),
    HELP_HELP("Type &b/" + Main.getInstance().getProperty("mainCommand") + " &a{0}&f to show help (this message). {1}"),
    HELP_RELOAD("Type &b/" + Main.getInstance().getProperty("mainCommand") + " &a{0}&f to reload the plugin. {1}"),
    NO_PERMISSION_HELP("(&cno permission&f)"),
    PLUGIN_RELOADED("&aPlugin reloaded."),
    NO_PERMISSION_COMMAND("&cYou are not allowed to use this command."),
    ERROR_HOOKING("&4Error in hooking into {0}! Please contact server administrators."),
    DB_EXCEPTION("&4A database error occured! Please contact server administrators.");

    private String                   path;
    private String                   def;
    private static YamlConfiguration LANG;

    /**
     * Lang enum constructor.
     * 
     * @param path
     *            The string path.
     * @param start
     *            The default string.
     */
    Lang(String start) {
        this.path = name().toLowerCase().replace("_", "-");
        def = start;
    }

    /**
     * Set the {@code YamlConfiguration} to use.
     * 
     * @param config
     *            The config to set.
     */
    public static void setFile(YamlConfiguration config) {
        Lang.LANG = config;
    }

    @Override
    public String toString() {
        if (this == TITLE) {
            return ChatColor.translateAlternateColorCodes('&', Lang.LANG.getString(path, def)) + " ";
        }
        return ChatColor.translateAlternateColorCodes('&', Lang.LANG.getString(path, def));
    }

    /**
     * Get the default value of the path.
     * 
     * @return The default value of the path.
     */
    public String getDefault() {
        return def;
    }

    /**
     * Get the path to the string.
     * 
     * @return The path to the string.
     */
    public String getPath() {
        return path;
    }
}