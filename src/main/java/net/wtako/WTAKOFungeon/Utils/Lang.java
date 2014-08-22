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

    OBJECT_DELETED("&aSuccessfully deleted {0}! (If it exists)"),

    COMMAND_LIST_FUNGEON("Command invocations for fungeon &f(&f{0}&f):"),
    COMMAND_ADDED("&aSuccessfully added command ({0}) for fungeon &f(&f{1}&f)&a as command ID {2}."),
    NO_SUCH_A_COMMAND("&eThere is no such a command where ID is {0}."),

    CASH_PRIZE_ADDED("&aSuccessfully added cash prize (${0}) for fungeon &f(&f{1}&f)&a as prize ID {2}."),
    ITEM_PRIZE_ADDED("&aSuccessfully added item prize &f({0}&f)&a for fungeon &f(&f{1}&f)&a as prize ID {2}."),
    NO_SUCH_A_ITEM_PRIZE("&eThere is no such a item prize where ID is {0}."),
    CANNOT_ADD_AIR("&cCannot add air as prize."),
    YOU_ARE_AWARDED_ITEM("&aYou are awarded the following prize: {0}"),
    YOU_ARE_AWARDED_MONEY("&aYou are awarded ${0}"),

    BAR_WAITING_ROOM_IDLE_FORMAT("Fungeon &f(&f{0}&f): Idle - {1}"),
    BAR_WAITING_ROOM_COUNTDOWN_FORMAT("Fungeon &f(&f{0}&f): Countdown - {1}"),
    BAR_FUNGEON_COUNTDOWN_FORMAT("Fungeon &f(&f{0}&f) - Wave {1} | Enemies left: {2}"),
    BAR_FUNGEON_WAVE_END_FORMAT("Fungeon &f(&f{0}&f) - Wave {1} End."),
    BAR_FUNGEON_FIRST_WAVE_COMING_FORMAT("Fungeon &f(&f{0}&f) - First wave is coming!"),
    FUNGEON_ADD_SUCCESS("&aSuccessfully added a new fungeon. (Name: {0}, ID: {1})"),
    FUNGEON_DELETE_SUCCESS("&aSuccessfully deleted an existing fungeon. &f(&f{0}&f)"),
    FUNGEON_DOES_NOT_EXIST("&eFungeon ID {0} does not exist."),
    FUNGEON_HAS_NO_WAVES("&cFungeon has no enemy waves."),

    PRIZE_LIST_FUNGEON("Prize list for fungeon &f(&f{0}&f):"),
    NO_FUNGEON_TO_DISPLAY("&eCurrently there is no fungeon to display."),
    FUNGEON_IS_PLAYING("&eThere are players playing this fungeon. Please try again later."),
    FUNGEON_IS_NOT_READY("&eThis fungeon &f(&f{0}&f)&e is currently not ready."),
    ALREADY_JOINED_FUNGEON("&cYou have already join a fungeon! &f(&f{0}&f)"),

    FORCE_LEAVE_FUNGEON("&cYou have been forced to leave the fungeon. (Kicked by {0})"),
    YOU_ARE_LEADER_NOW("&aYou are team leader now."),
    PLAYER_JOINED("&a{0} has joined the team."),
    PLAYER_LEFT("&a{0} has left the team."),
    FUNGEON_JOIN("&aYou have successfully join a fungeon &f(&f{0}&f)&a. "
            + "Please wait until team size reaches {1} players. &f(Current: {2})"),
    FUNGEON_LEAVE("&cYou have left the fungeon."),
    FUNGEON_START("&aFungeon has started!"),
    FUNGEON_END("&cFungeon has ended."),
    YOU_WIN("&aYou win!"),
    YOU_LOSE("&cYou lose."),

    WELCOME_TO_WIZARD("&eWelcome to config wizard of fungeon &f(&f{0}&f)&e. &aType &fexit&a at anytime to exit."),
    ALREADY_IN_WIZARD("&cYou are already in another wizard."),
    EXIT_WIZARD("&eExited wizard mode."),
    AVAILABLE_CONFIGS("&eAvailable configs: &f{0}"),
    CONFIG_WIZARD("&eYou entered config &f{0}&e wizard."),
    TYPE_SOMETHING_TO_CONTINUE("&eType something to continue. &f(Example: {0})"),
    BREAK_SOME_BLOCKS_TO_CONTINUE("&eClick some block(s) to continue."),
    NO_SUCH_A_CONFIG("&eThere is no such a config: {0}"),
    CONFIG_SET("&aConfig successfully set. ({0}: {1})"),
    CONFIG_SET_PENDING("&aConfig successfully set. Pending another config... ({0}: {1})"),
    CONFIG_SET_FAIL("&cFailed to set config. Please try again. ({0}: {1}) - {2}"),

    ITEM_PRINT_FORMAT("{0} x {1}"),
    LOCATION_FORMAT("World: {0}, X: {1}, Y: {2}, Z: {3}"),
    FUNGEON_PLAYERS_FORMAT("{0}/{1}"),
    FUNGEON_TO_STRING_FORMAT("{1}"),
    LIST_TOTAL("Total: {0}"),

    FUNGEON("Fungeon"),
    CASH_PRIZE("Cash prize"),
    ITEM_PRIZE("Item prize"),
    COMMAND("Command"),
    SYSTEM_WORD("system"),

    COMMAND_HELP_SEPERATOR("&6 | &a"),
    COMMAND_ARG_IN_USE("&e{0}&a"),
    SUB_COMMAND("Sub-command: &e{0}"),
    HELP_HELP("Type &b/" + Main.getInstance().getProperty("mainCommand") + " &a{0}&f to show help (this message)."),
    HELP_RELOAD("Type &b/" + Main.getInstance().getProperty("mainCommand") + " &a{0}&f to reload the plugin."),
    HELP_PRIZE("Type &b/" + Main.getInstance().getProperty("mainCommand") + " &a{0}&f to manage fungeon prizes."),
    HELP_PRIZE_ADD("Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0} &a<fungeon ID> [Cash]&f to add a prize to a fungeon."),
    HELP_PRIZE_GET("Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0} &a<prize ID>&f to get a prize for debugging use."),
    HELP_PRIZE_DEL("Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0} &a<prize ID>&f to delete an existing prize."),
    HELP_PRIZE_CLEAR("Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0} &a<fungeon ID>&f to delete all prizes for a fungeon."),
    HELP_PRIZE_LIST("Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0} &a<fungeon ID>&f to view all prizes of a fungeon."),
    HELP_COMMAND("Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0}&f to manage fungeon command invocations."),
    HELP_COMMAND_ADD("Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0} &a<fungeon ID> <command>&f to add a command to be invoked by a fungeon."),
    HELP_COMMAND_DEL("Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0} &a<command ID>&f to delete an existing fungeon command invocation."),
    HELP_COMMAND_CLEAR("Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0} &a<fungeon ID>&f to delete all command invocations for a fungeon."),
    HELP_COMMAND_LIST("Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0} &a<fungeon ID>&f to view command invocations of a fungeon."),
    HELP_FUNGEON("Type &b/" + Main.getInstance().getProperty("mainCommand") + " &a{0}&f to manage fungeons."),
    HELP_FUNGEON_ADD("Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0} &a<fungeon name>&f to add a new fungeon."),
    HELP_FUNGEON_DEL("Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0} &a<fungeon ID>&f to delete an existing fungeon."),
    HELP_FUNGEON_SET("Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0} &a<fungeon ID>&f to set config of a fungeon."),
    HELP_FUNGEON_LIST("Type &b/" + Main.getInstance().getProperty("mainCommand") + " &a{0}&f to view all fungeons."),
    HELP_FUNGEON_LIST_CONFIGS("Type &b/" + Main.getInstance().getProperty("mainCommand")
            + " &a{0}&f to view all config values of a fungeon."),
    NO_PERMISSION_HELP(" (&cno permission&f)"),
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
        path = name().toLowerCase().replace("_", "-");
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