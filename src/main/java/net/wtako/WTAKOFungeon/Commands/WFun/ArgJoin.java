package net.wtako.WTAKOFungeon.Commands.WFun;

import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Utils.CommandHelper;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArgJoin {

    public ArgJoin(final CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_JOIN.toString(), CommandHelper.joinArgsInUse(args, 1)));
            return;
        }
        final Integer fungeonID;
        try {
            fungeonID = Integer.parseInt(args[1]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_JOIN.toString(), CommandHelper.joinArgsInUse(args, 1)));
            return;
        }
        final Fungeon fungeon = Fungeon.getAllFungeons().get(fungeonID);
        if (fungeon == null) {
            sender.sendMessage(MessageFormat.format(Lang.FUNGEON_DOES_NOT_EXIST.toString(), fungeonID));
            return;
        }
        sender.sendMessage(fungeon.joinPlayer((Player) sender).name());
    }

}
