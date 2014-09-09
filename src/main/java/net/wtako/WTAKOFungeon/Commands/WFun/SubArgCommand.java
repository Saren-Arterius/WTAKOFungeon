package net.wtako.WTAKOFungeon.Commands.WFun;

import java.lang.reflect.InvocationTargetException;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Commands.WFun.Command.ArgAdd;
import net.wtako.WTAKOFungeon.Commands.WFun.Command.ArgClear;
import net.wtako.WTAKOFungeon.Commands.WFun.Command.ArgDel;
import net.wtako.WTAKOFungeon.Commands.WFun.Command.ArgList;
import net.wtako.WTAKOFungeon.Utils.BaseCommands;
import net.wtako.WTAKOFungeon.Utils.CommandHelper;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;

public class SubArgCommand {

    public enum SubCommands implements BaseCommands {
        MAIN_COMMAND(Lang.HELP_COMMAND.toString(), SubArgCommand.class, Main.artifactId + ".admin"),
        LIST(Lang.HELP_COMMAND_LIST.toString(), ArgList.class, Main.artifactId + ".admin"),
        DEL(Lang.HELP_COMMAND_DEL.toString(), ArgDel.class, Main.artifactId + ".admin"),
        CLEAR(Lang.HELP_COMMAND_CLEAR.toString(), ArgClear.class, Main.artifactId + ".admin"),
        ADD(Lang.HELP_COMMAND_ADD.toString(), ArgAdd.class, Main.artifactId + ".admin");

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
        public String getRequiredPermission() {
            return permission;
        }

        @Override
        public Class<?> getTargetClass() {
            return targetClass;
        }
    }

    public SubArgCommand(final CommandSender sender, String[] args) {
        if (args.length < 2 || !callCommand(sender, args, args[1])) {
            CommandHelper.sendHelp(sender, SubCommands.values(), args[0]);
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
                return true;
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
