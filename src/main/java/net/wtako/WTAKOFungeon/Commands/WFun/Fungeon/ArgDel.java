package net.wtako.WTAKOFungeon.Commands.WFun.Fungeon;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Database;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Utils.CommandsWFun;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class ArgDel {

    public ArgDel(final CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_FUNGEON_DEL.toString(),
                    CommandsWFun.joinArgsInUse(args, args.length)));
            return;
        }
        final Integer fungeonID;
        try {
            fungeonID = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_FUNGEON_DEL.toString(),
                    CommandsWFun.joinArgsInUse(args, args.length)));
            return;
        }
        Fungeon.getValidFungeons().remove(fungeonID);
        final Fungeon fungeon = Fungeon.getAllFungeons().remove(fungeonID);
        if (fungeon != null) {
            fungeon.forceResetAll();
        }
        if (fungeon != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        final PreparedStatement insStmt = Database.getConn().prepareStatement(
                                "DELETE FROM `fungeons` WHERE `row_id` = ?");
                        insStmt.setInt(1, fungeonID);
                        insStmt.execute();
                        insStmt.close();
                        sender.sendMessage(MessageFormat.format(Lang.FUNGEON_DELETE_SUCCESS.toString(),
                                fungeon.toString()));
                    } catch (final SQLException e) {
                        sender.sendMessage(Lang.DB_EXCEPTION.toString());
                        e.printStackTrace();
                    }
                }

            }.runTaskAsynchronously(Main.getInstance());
        } else {
            sender.sendMessage(MessageFormat.format(Lang.FUNGEON_DOES_NOT_EXIST.toString(), fungeonID));
        }
    }
}
