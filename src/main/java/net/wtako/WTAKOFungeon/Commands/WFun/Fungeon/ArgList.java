package net.wtako.WTAKOFungeon.Commands.WFun.Fungeon;

import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Status;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;

public class ArgList {

    public ArgList(CommandSender sender, String[] args) {
        int fungeons = 0;
        for (final Fungeon fungeon: Fungeon.getAllFungeons().values()) {
            final Status status = fungeon.getStatus();
            if (status == Fungeon.Status.FUNGEON_NOT_VALID) {
                sender.sendMessage(MessageFormat.format("{0}. {1} - {2} ({3})", fungeon.getID(), fungeon.getName(),
                        status, fungeon.checkValidity()));
            } else {
                sender.sendMessage(MessageFormat.format("{0}. {1} - {2}", fungeon.getID(), fungeon.getName(), status));
            }
            fungeons++;
        }
        if (fungeons == 0) {
            sender.sendMessage(Lang.NO_FUNGEON_TO_DISPLAY.toString());
        } else {
            sender.sendMessage(MessageFormat.format(Lang.LIST_TOTAL.toString(), fungeons));
        }
    }

}
