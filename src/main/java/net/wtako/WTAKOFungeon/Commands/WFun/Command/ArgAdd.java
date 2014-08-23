package net.wtako.WTAKOFungeon.Commands.WFun.Command;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.InvokeCommand;
import net.wtako.WTAKOFungeon.Utils.CommandHelper;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class ArgAdd {

    public ArgAdd(final CommandSender sender, final String[] args) {
        if (args.length < 4) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_COMMAND_ADD.toString(),
                    CommandHelper.joinArgsInUse(args, 2)));
            return;
        }
        final Integer fungeonID;
        try {
            fungeonID = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_COMMAND_ADD.toString(),
                    CommandHelper.joinArgsInUse(args, 2)));
            return;
        }
        final Fungeon fungeon = Fungeon.getAllFungeons().get(fungeonID);
        if (fungeon == null) {
            sender.sendMessage(MessageFormat.format(Lang.FUNGEON_DOES_NOT_EXIST.toString(), fungeonID));
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                String command = "";
                for (int i = 3; i < args.length; i++) {
                    command += args[i];
                    if (i < args.length - 1) {
                        command += " ";
                    }
                }
                try {
                    sender.sendMessage(MessageFormat.format(Lang.COMMAND_ADDED.toString(), command, fungeon.toString(),
                            InvokeCommand.addCommand(fungeonID, command)));
                } catch (final SQLException e) {
                    sender.sendMessage(Lang.DB_EXCEPTION.toString());
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }
}
