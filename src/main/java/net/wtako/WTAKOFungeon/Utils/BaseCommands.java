package net.wtako.WTAKOFungeon.Utils;

public interface BaseCommands {

    public String getHelpMessage();

    public String getRequiredPermission();

    public Class<?> getTargetClass();

    public String name();

}
