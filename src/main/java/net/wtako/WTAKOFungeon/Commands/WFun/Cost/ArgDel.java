package net.wtako.WTAKOFungeon.Commands.WFun.Cost;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Cost;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Utils.Commands;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class ArgDel {

    public ArgDel(final CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_COST_DEL.toString(),
                    Commands.joinArgsInUse(args, args.length)));
            return;
        }
        final Integer costID;
        try {
            costID = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_COST_DEL.toString(), Commands.joinArgsInUse(args, 2)));
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Cost.deleteCost(costID);
                    sender.sendMessage(MessageFormat.format(Lang.OBJECT_DELETED.toString(),
                            MessageFormat.format("{0} (ID: {1})", Lang.ITEM_COST.toString(), costID)));
                    for (final Fungeon fungeon: Fungeon.getAllFungeons().values()) {
                        fungeon.updateCosts();
                    }
                } catch (final SQLException e) {
                    sender.sendMessage(Lang.DB_EXCEPTION.toString());
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }
}
