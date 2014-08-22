package net.wtako.WTAKOFungeon.Commands.WFun.Prize;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Prize;
import net.wtako.WTAKOFungeon.Utils.CommandsWFun;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class ArgDel {

    public ArgDel(final CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_PRIZE_DEL.toString(),
                    CommandsWFun.joinArgsInUse(args, args.length)));
            return;
        }
        final Integer prizeID;
        try {
            prizeID = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_PRIZE_DEL.toString(), CommandsWFun.joinArgsInUse(args, 2)));
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Prize.deletePrize(prizeID);
                    sender.sendMessage(MessageFormat.format(Lang.OBJECT_DELETED.toString(),
                            MessageFormat.format("{0} (ID: {1})", Lang.ITEM_PRIZE.toString(), prizeID)));
                } catch (final SQLException e) {
                    sender.sendMessage(Lang.DB_EXCEPTION.toString());
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }
}
