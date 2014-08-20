package net.wtako.WTAKOFungeon.Utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Commands.WFun.ArgHelp;
import net.wtako.WTAKOFungeon.Commands.WFun.ArgReload;
import net.wtako.WTAKOFungeon.Commands.WFun.ArgTest;
import net.wtako.WTAKOFungeon.Commands.WFun.SubArgFungeon;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public enum Commands implements BaseCommands {

    MAIN_COMMAND(Lang.HELP_HELP.toString(), ArgHelp.class, Main.artifactId + ".use"),
    H(Lang.HELP_HELP.toString(), ArgHelp.class, Main.artifactId + ".use"),
    HELP(Lang.HELP_HELP.toString(), ArgHelp.class, Main.artifactId + ".use"),
    TEST(Lang.HELP_TEST.toString(), ArgTest.class, Main.artifactId + ".admin"),
    FUNGEON(Lang.HELP_FUNGEON.toString(), SubArgFungeon.class, Main.artifactId + ".admin"),
    F(Lang.HELP_FUNGEON.toString(), SubArgFungeon.class, Main.artifactId + ".admin"),
    RELOAD(Lang.HELP_RELOAD.toString(), ArgReload.class, Main.artifactId + ".reload");

    private String   helpMessage;
    private Class<?> targetClass;
    private String   permission;

    private Commands(String helpMessage, Class<?> targetClass, String permission) {
        this.helpMessage = helpMessage;
        this.targetClass = targetClass;
        this.permission = permission;
    }

    @Override
    public String getHelpMessage() {
        return helpMessage;
    }

    @Override
    public Class<?> getTargetClass() {
        return targetClass;
    }

    public String getRequiredPermission() {
        return permission;
    }

    public static String joinArgsInUse(String[] args, int level) {
        String argsMessage = "";
        for (int i = 0; i < level; i++) {
            argsMessage += MessageFormat.format(Lang.COMMAND_ARG_IN_USE.toString(), args[i]);
            if (i < level - 1) {
                argsMessage += " ";
            }
        }
        return argsMessage;
    }

    public static void sendHelp(final CommandSender sender, final BaseCommands[] commandValues, final String commandName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                final ArrayList<String> messages = new ArrayList<String>();
                messages.add(Main.getInstance().getName() + " v" + Main.getInstance().getProperty("version"));
                messages.add("Author: " + Main.getInstance().getProperty("author"));
                if (!commandName.equalsIgnoreCase("")) {
                    messages.add(MessageFormat.format(Lang.SUB_COMMAND.toString(), commandName));
                }
                final HashMap<String, ArrayList<String>> commandHelps = new HashMap<String, ArrayList<String>>();
                for (final BaseCommands command: commandValues) {
                    if (command.name().equalsIgnoreCase("MAIN_COMMAND")) {
                        continue;
                    }
                    boolean hasHelpMessage = false;
                    for (final Entry<String, ArrayList<String>> entry: commandHelps.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(command.getHelpMessage())) {
                            entry.getValue().add(command.name().toLowerCase().replace("_", "-"));
                            hasHelpMessage = true;
                            break;
                        }
                    }
                    if (!hasHelpMessage) {
                        final ArrayList<String> commandList = new ArrayList<String>();
                        commandList.add(command.name().toLowerCase());
                        commandHelps.put(command.getHelpMessage(), commandList);
                    }
                }
                for (final Entry<String, ArrayList<String>> entry: commandHelps.entrySet()) {
                    String displayCommands = "";
                    int counter = 0;
                    for (final String displayCommand: entry.getValue()) {
                        displayCommands += displayCommand;
                        counter++;
                        if (counter < entry.getValue().size()) {
                            displayCommands += Lang.COMMAND_HELP_SEPERATOR;
                        }
                    }
                    String permissionString = "";
                    for (final Commands command: Commands.values()) {
                        if (command.getHelpMessage().equalsIgnoreCase(entry.getKey())
                                && !sender.hasPermission(command.getRequiredPermission())) {
                            permissionString = Lang.NO_PERMISSION_HELP.toString();
                            break;
                        }
                    }
                    displayCommands = commandName.equalsIgnoreCase("") ? displayCommands : MessageFormat.format(
                            Lang.COMMAND_ARG_IN_USE.toString(), commandName) + " " + displayCommands;
                    messages.add(MessageFormat.format(entry.getKey(), displayCommands, permissionString));
                }
                sender.sendMessage(messages.toArray(new String[messages.size()]));
            }
        }.runTaskAsynchronously(Main.getInstance());
    }
}
