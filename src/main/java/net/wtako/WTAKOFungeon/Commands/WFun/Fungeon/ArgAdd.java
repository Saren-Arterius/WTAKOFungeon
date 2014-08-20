package net.wtako.WTAKOFungeon.Commands.WFun.Fungeon;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Utils.Commands;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class ArgAdd {

    public ArgAdd(final CommandSender sender, final String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_FUNGEON_ADD.toString(),
                    Commands.joinArgsInUse(args, args.length)));
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String fName = "";
                    for (int i = 2; i < args.length; i++) {
                        fName += args[i];
                        if (i < args.length - 1) {
                            fName += " ";
                        }
                    }
                    final Fungeon fungeon = new Fungeon(fName);
                    sender.sendMessage(MessageFormat.format(Lang.FUNGEON_ADD_SUCCESS.toString(), fungeon.getName(),
                            fungeon.getID()));
                } catch (final SQLException e) {
                    sender.sendMessage(Lang.DB_EXCEPTION.toString());
                    e.printStackTrace();
                }
            }

        }.runTaskAsynchronously(Main.getInstance());

    }

}
