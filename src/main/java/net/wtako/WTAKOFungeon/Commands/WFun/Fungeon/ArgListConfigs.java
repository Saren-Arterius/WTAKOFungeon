package net.wtako.WTAKOFungeon.Commands.WFun.Fungeon;

import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Utils.Commands;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;

public class ArgListConfigs {

    public ArgListConfigs(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_FUNGEON_LIST_CONFIGS.toString(),
                    Commands.joinArgsInUse(args, args.length)));
            return;
        }
        Integer fungeonID;
        try {
            fungeonID = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_FUNGEON_LIST_CONFIGS.toString(),
                    Commands.joinArgsInUse(args, args.length)));
            return;
        }
        sender.sendMessage(String.valueOf(fungeonID));
    }

}
