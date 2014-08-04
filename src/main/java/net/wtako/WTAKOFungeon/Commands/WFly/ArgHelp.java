package net.wtako.WTAKOFungeon.Commands.WFly;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;

public class ArgHelp {

    public ArgHelp(CommandSender sender) {
        sender.sendMessage(Main.getInstance().getName() + " v" + Main.getInstance().getProperty("version"));
        sender.sendMessage("Author: " + Main.getInstance().getProperty("author"));
        sender.sendMessage(Lang.HELP_RELOAD.toString());
    }

}
