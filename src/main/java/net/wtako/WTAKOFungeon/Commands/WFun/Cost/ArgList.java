package net.wtako.WTAKOFungeon.Commands.WFun.Cost;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Database;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Utils.CommandHelper;
import net.wtako.WTAKOFungeon.Utils.ItemStackUtils;
import net.wtako.WTAKOFungeon.Utils.ItemUtils;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class ArgList {

    public ArgList(final CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_COST_LIST.toString(),
                    CommandHelper.joinArgsInUse(args, args.length)));
            return;
        }
        final Integer fungeonID;
        try {
            fungeonID = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_COST_LIST.toString(), CommandHelper.joinArgsInUse(args, 2)));
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
                    sender.sendMessage(MessageFormat.format(Lang.COST_LIST_FUNGEON.toString(), fungeon.toString()));
                    int counter = 0;
                    PreparedStatement selStmt = Database.getConn().prepareStatement(
                            "SELECT * FROM costs WHERE fungeon_id = ? AND item_json IS NOT NULL");
                    selStmt.setInt(1, fungeonID);
                    ResultSet result = selStmt.executeQuery();
                    while (result.next()) {
                        sender.sendMessage(MessageFormat.format("{0}. {1} (ID: {2})", counter++ + 1,
                                ItemStackUtils.toHumanReadable(ItemUtils.restoreItem(result.getString("item_json"))),
                                result.getInt("row_id")));
                    }
                    result.close();
                    selStmt.close();
                    sender.sendMessage(MessageFormat.format(Lang.LIST_TOTAL.toString(), counter));
                    sender.sendMessage(MessageFormat.format("{0}:", Lang.CASH_COST.toString()));
                    counter = 0;
                    int cashCost = 0;
                    selStmt = Database.getConn().prepareStatement(
                            "SELECT * FROM costs WHERE fungeon_id = ? AND cash_amount >= 0");
                    selStmt.setInt(1, fungeonID);
                    result = selStmt.executeQuery();
                    while (result.next()) {
                        cashCost += result.getInt("cash_amount");
                        sender.sendMessage(MessageFormat.format("{0}. ${1} (ID: {2})", counter++ + 1,
                                result.getInt("cash_amount"), result.getInt("row_id")));
                    }
                    result.close();
                    selStmt.close();
                    sender.sendMessage(MessageFormat.format(Lang.LIST_TOTAL.toString(), cashCost));
                } catch (final SQLException e) {
                    sender.sendMessage(Lang.DB_EXCEPTION.toString());
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }
}
