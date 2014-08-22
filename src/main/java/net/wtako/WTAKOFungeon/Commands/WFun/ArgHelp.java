package net.wtako.WTAKOFungeon.Commands.WFun;

import net.wtako.WTAKOFungeon.Utils.CommandHelper;
import net.wtako.WTAKOFungeon.Utils.CommandsWFun;

import org.bukkit.command.CommandSender;

public class ArgHelp {

    public ArgHelp(final CommandSender sender, String[] args) {
        CommandHelper.sendHelp(sender, CommandsWFun.values(), "");
    }

}
