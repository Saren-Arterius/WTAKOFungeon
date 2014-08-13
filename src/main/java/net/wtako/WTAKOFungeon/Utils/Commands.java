package net.wtako.WTAKOFungeon.Utils;

import net.wtako.WTAKOFungeon.Commands.WFun.ArgHelp;
import net.wtako.WTAKOFungeon.Commands.WFun.ArgReload;
import net.wtako.WTAKOFungeon.Commands.WFun.ArgTest;

public enum Commands {

    MAIN_COMMAND(Lang.HELP_HELP.toString(), ArgHelp.class),
    H(Lang.HELP_HELP.toString(), ArgHelp.class),
    HELP(Lang.HELP_HELP.toString(), ArgHelp.class),
    RELOAD(Lang.HELP_RELOAD.toString(), ArgReload.class),
    TEST("/wfun {0}: test command", ArgTest.class);

    private String   helpMessage;
    private Class<?> targetClass;

    private Commands(String helpMessage, Class<?> targetClass) {
        this.helpMessage = helpMessage;
        this.targetClass = targetClass;
    }

    public String getHelpMessage() {
        return helpMessage;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }
}
