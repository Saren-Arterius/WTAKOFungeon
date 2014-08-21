package net.wtako.WTAKOFungeon.Commands.WFun.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Database;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Utils.Commands;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class ArgList {

    public ArgList(final CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_COMMAND_LIST.toString(),
                    Commands.joinArgsInUse(args, args.length)));
            return;
        }
        final Integer fungeonID;
        try {
            fungeonID = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_COMMAND_LIST.toString(), Commands.joinArgsInUse(args, 2)));
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
                try {
                    sender.sendMessage(MessageFormat.format(Lang.COMMAND_LIST_FUNGEON.toString(), fungeon.toString()));
                    int counter = 0;
                    final PreparedStatement selStmt = Database.getConn().prepareStatement(
                            "SELECT * FROM invoke_commands WHERE fungeon_id = ?");
                    selStmt.setInt(1, fungeonID);
                    final ResultSet result = selStmt.executeQuery();
                    while (result.next()) {
                        sender.sendMessage(MessageFormat.format("{0}. {1} (ID: {2})", counter++ + 1,
                                result.getString("command"), result.getInt("row_id")));
                    }
                    result.close();
                    selStmt.close();
                    sender.sendMessage(MessageFormat.format(Lang.LIST_TOTAL.toString(), counter));
                } catch (final SQLException e) {
                    sender.sendMessage(Lang.DB_EXCEPTION.toString());
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }
}
