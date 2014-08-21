package net.wtako.WTAKOFungeon.Commands.WFun.Command;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.InvokeCommand;
import net.wtako.WTAKOFungeon.Utils.Commands;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class ArgDel {

    public ArgDel(final CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_COMMAND_DEL.toString(),
                    Commands.joinArgsInUse(args, args.length)));
            return;
        }
        final Integer commandID;
        try {
            commandID = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_COMMAND_DEL.toString(), Commands.joinArgsInUse(args, 2)));
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    InvokeCommand.deleteCommand(commandID);
                    sender.sendMessage(MessageFormat.format(Lang.OBJECT_DELETED.toString(),
                            MessageFormat.format("{0} (ID: {1})", Lang.COMMAND.toString(), commandID)));
                } catch (final SQLException e) {
                    sender.sendMessage(Lang.DB_EXCEPTION.toString());
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }
}
