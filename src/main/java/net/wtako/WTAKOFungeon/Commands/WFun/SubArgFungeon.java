package net.wtako.WTAKOFungeon.Commands.WFun;

import java.lang.reflect.InvocationTargetException;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Commands.WFun.Fungeon.ArgAdd;
import net.wtako.WTAKOFungeon.Commands.WFun.Fungeon.ArgDel;
import net.wtako.WTAKOFungeon.Commands.WFun.Fungeon.ArgList;
import net.wtako.WTAKOFungeon.Commands.WFun.Fungeon.ArgListConfigs;
import net.wtako.WTAKOFungeon.Commands.WFun.Fungeon.ArgSet;
import net.wtako.WTAKOFungeon.Utils.BaseCommands;
import net.wtako.WTAKOFungeon.Utils.Commands;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;

public class SubArgFungeon {

    public enum SubCommands implements BaseCommands {
        MAIN_COMMAND(Lang.HELP_FUNGEON.toString(), SubArgFungeon.class, Main.artifactId + ".use"),
        LIST(Lang.HELP_FUNGEON_LIST.toString(), ArgList.class, Main.artifactId + ".use"),
        LIST_CONFIGS(Lang.HELP_FUNGEON_LIST_CONFIGS.toString(), ArgListConfigs.class, Main.artifactId + ".admin"),
        LC(Lang.HELP_FUNGEON_LIST_CONFIGS.toString(), ArgListConfigs.class, Main.artifactId + ".admin"),
        ADD(Lang.HELP_FUNGEON_ADD.toString(), ArgAdd.class, Main.artifactId + ".admin"),
        DEL(Lang.HELP_FUNGEON_DEL.toString(), ArgDel.class, Main.artifactId + ".admin"),
        SET(Lang.HELP_FUNGEON_SET.toString(), ArgSet.class, Main.artifactId + ".admin");

        private String   helpMessage;
        private Class<?> targetClass;
        private String   permission;

        private SubCommands(String helpMessage, Class<?> targetClass, String permission) {
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
    }

    public SubArgFungeon(final CommandSender sender, String[] args) {
        if (args.length < 2 || !callCommand(sender, args, args[1])) {
            Commands.sendHelp(sender, SubCommands.values(), args[0]);
        }
    }

    public boolean callCommand(CommandSender sender, String[] args, String targetCommandName) {
        try {
            final SubCommands targetCommand = SubCommands.valueOf(targetCommandName.toUpperCase().replace("-", "_"));
            if (targetCommand == SubCommands.MAIN_COMMAND) {
                return false;
            }
            if (!sender.hasPermission(targetCommand.getRequiredPermission())) {
                sender.sendMessage(Lang.NO_PERMISSION_COMMAND.toString());
            }
            targetCommand.getTargetClass().getDeclaredConstructor(CommandSender.class, String[].class)
                    .newInstance(sender, args);
            return true;
        } catch (final IllegalArgumentException e) {
            return false;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }

    }

}
