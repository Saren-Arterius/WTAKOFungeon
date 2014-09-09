package net.wtako.WTAKOFungeon.Utils;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Commands.WFun.ArgHelp;
import net.wtako.WTAKOFungeon.Commands.WFun.ArgJoin;
import net.wtako.WTAKOFungeon.Commands.WFun.ArgKick;
import net.wtako.WTAKOFungeon.Commands.WFun.ArgLeave;
import net.wtako.WTAKOFungeon.Commands.WFun.ArgReload;
import net.wtako.WTAKOFungeon.Commands.WFun.SubArgCommand;
import net.wtako.WTAKOFungeon.Commands.WFun.SubArgCost;
import net.wtako.WTAKOFungeon.Commands.WFun.SubArgFungeon;
import net.wtako.WTAKOFungeon.Commands.WFun.SubArgPrize;

public enum CommandsWFun implements BaseCommands {

    MAIN_COMMAND(Lang.HELP_HELP.toString(), ArgHelp.class, Main.artifactId + ".use"),
    H(Lang.HELP_HELP.toString(), ArgHelp.class, Main.artifactId + ".use"),
    HELP(Lang.HELP_HELP.toString(), ArgHelp.class, Main.artifactId + ".use"),
    LEAVE(Lang.HELP_LEAVE.toString(), ArgLeave.class, Main.artifactId + ".use"),
    F(Lang.HELP_FUNGEON.toString(), SubArgFungeon.class, Main.artifactId + ".use"),
    K(Lang.HELP_KICK.toString(), ArgKick.class, Main.artifactId + ".use"),
    KICK(Lang.HELP_KICK.toString(), ArgKick.class, Main.artifactId + ".use"),
    J(Lang.HELP_JOIN.toString(), ArgJoin.class, Main.artifactId + ".admin"),
    JOIN(Lang.HELP_JOIN.toString(), ArgJoin.class, Main.artifactId + ".admin"),
    FUNGEON(Lang.HELP_FUNGEON.toString(), SubArgFungeon.class, Main.artifactId + ".use"),
    P(Lang.HELP_PRIZE.toString(), SubArgPrize.class, Main.artifactId + ".use"),
    PRIZE(Lang.HELP_PRIZE.toString(), SubArgPrize.class, Main.artifactId + ".use"),
    C(Lang.HELP_COST.toString(), SubArgCost.class, Main.artifactId + ".use"),
    COST(Lang.HELP_COST.toString(), SubArgCost.class, Main.artifactId + ".use"),
    COM(Lang.HELP_COMMAND.toString(), SubArgCommand.class, Main.artifactId + ".admin"),
    COMMAND(Lang.HELP_COMMAND.toString(), SubArgCommand.class, Main.artifactId + ".admin"),
    RELOAD(Lang.HELP_RELOAD.toString(), ArgReload.class, Main.artifactId + ".reload");

    private String   helpMessage;

    private Class<?> targetClass;

    private String   permission;

    private CommandsWFun(String helpMessage, Class<?> targetClass, String permission) {
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
