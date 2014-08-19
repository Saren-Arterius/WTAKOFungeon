package net.wtako.WTAKOFungeon.Commands.WFun.Fungeon;

import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.EventHandlers.FungeonWizardListener;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Utils.Commands;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArgSet {

    public ArgSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_FUNGEON_SET.toString(),
                    Commands.joinArgsInUse(args, args.length)));
            return;
        }
        Integer fungeonID;
        try {
            fungeonID = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_FUNGEON_SET.toString(), Commands.joinArgsInUse(args, 2)));
            return;
        }
        final Fungeon fungeon = Fungeon.getAllFungeons().get(fungeonID);
        if (fungeon != null) {
            FungeonWizardListener.goToWizard((Player) sender, fungeon);
        } else {
            sender.sendMessage(MessageFormat.format(Lang.FUNGEON_DOES_NOT_EXIST.toString(), fungeonID));
        }
    }

}
