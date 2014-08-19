package net.wtako.WTAKOFungeon.Commands.WFun.Fungeon;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Methods.Database;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Utils.Commands;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;

public class ArgDel {

    public ArgDel(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_FUNGEON_DEL.toString(),
                    Commands.joinArgsInUse(args, args.length)));
            return;
        }
        Integer fungeonID;
        try {
            fungeonID = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_FUNGEON_DEL.toString(),
                    Commands.joinArgsInUse(args, args.length)));
            return;
        }
        Fungeon.getValidFungeons().remove(fungeonID);
        final Fungeon fungeon = Fungeon.getAllFungeons().remove(fungeonID);
        if (fungeon != null) {
            if (fungeon.kickAll() == Fungeon.Error.FUNGEON_HAS_ALREADY_STARTED) {
                fungeon.forceEnd();
            }
        }
        if (fungeon != null) {
            try {
                final PreparedStatement insStmt = Database.getConn().prepareStatement(
                        "DELETE FROM `fungeons` WHERE `row_id` = ?");
                insStmt.setInt(1, fungeonID);
                insStmt.execute();
                insStmt.close();
                sender.sendMessage(MessageFormat.format(Lang.FUNGEON_DELETE_SUCCESS.toString(), fungeon.toString()));
            } catch (final SQLException e) {
                sender.sendMessage(Lang.DB_EXCEPTION.toString());
                e.printStackTrace();
            }
        } else {
            sender.sendMessage(MessageFormat.format(Lang.FUNGEON_DOES_NOT_EXIST.toString(), fungeonID));
        }
    }
}
