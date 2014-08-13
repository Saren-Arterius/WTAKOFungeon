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

    TITLE("title", "[" + Main.getInstance().getName() + "]"),
    COMMAND_HELP_SEPERATOR("command-help-seperator", "&6 | &a"),
    HELP_HELP("help-help", "Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0}&f to show help (this message). {1}"),
    HELP_RELOAD("help-reload", "Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0}&f to reload the plugin. {1}"),
    NO_PERMISSION_HELP("no-permission-help", "(&cno permission&f)"),
    PLUGIN_RELOADED("plugin-reloaded", "&aPlugin reloaded."),
    NO_PERMISSION_COMMAND("no-permission-command", "&cYou are not allowed to use this command.");

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
    Lang(String path, String start) {
        this.path = path;
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