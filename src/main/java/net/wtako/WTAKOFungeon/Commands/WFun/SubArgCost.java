package net.wtako.WTAKOFungeon.Commands.WFun;

import java.lang.reflect.InvocationTargetException;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Commands.WFun.Cost.ArgAdd;
import net.wtako.WTAKOFungeon.Commands.WFun.Cost.ArgClear;
import net.wtako.WTAKOFungeon.Commands.WFun.Cost.ArgDel;
import net.wtako.WTAKOFungeon.Commands.WFun.Cost.ArgHas;
import net.wtako.WTAKOFungeon.Commands.WFun.Cost.ArgList;
import net.wtako.WTAKOFungeon.Utils.BaseCommands;
import net.wtako.WTAKOFungeon.Utils.Commands;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;

public class SubArgCost {

    public enum SubCommands implements BaseCommands {
        MAIN_COMMAND(Lang.HELP_COST.toString(), SubArgCost.class, Main.artifactId + ".use"),
        LIST(Lang.HELP_COST_LIST.toString(), ArgList.class, Main.artifactId + ".use"),
        DEL(Lang.HELP_COST_DEL.toString(), ArgDel.class, Main.artifactId + ".admin"),
        CLEAR(Lang.HELP_COST_CLEAR.toString(), ArgClear.class, Main.artifactId + ".admin"),
        ADD(Lang.HELP_COST_ADD.toString(), ArgAdd.class, Main.artifactId + ".admin"),
        HAS(Lang.HELP_COST_HAS.toString(), ArgHas.class, Main.artifactId + ".use");

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

    public SubArgCost(final CommandSender sender, String[] args) {
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
