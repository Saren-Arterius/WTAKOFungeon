package net.wtako.WTAKOFungeon.Commands.WFun.Fungeon;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Methods.Database;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Utils.Commands;
import net.wtako.WTAKOFungeon.Utils.Config;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;

public class ArgAdd {

    public ArgAdd(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_FUNGEON_ADD.toString(),
                    Commands.joinArgsInUse(args, args.length)));
            return;
        }
        try {
            final PreparedStatement insStmt = Database.getConn().prepareStatement(
                    "INSERT INTO `fungeons` (`enabled`, `fungeon_name`, "
                            + "`time_limit`, `min_players`, `max_players`, `wait_time`) VALUES (?, ?, ?, ?, ?, ?)");
            insStmt.setInt(1, 1);
            insStmt.setString(2, args[2]);
            insStmt.setInt(3, Config.DEFAULT_FUNGEON_TIME_LIMIT_SECONDS.getInt());
            insStmt.setInt(4, Config.DEFAULT_MIN_PLAYERS.getInt());
            insStmt.setInt(5, Config.DEFAULT_MAX_PLAYERS.getInt());
            insStmt.setInt(6, Config.DEFAULT_WAITING_ROOM_TIME.getInt());
            insStmt.execute();
            final int fungeonID = insStmt.getGeneratedKeys().getInt(1);
            insStmt.close();
            new Fungeon(fungeonID);
            sender.sendMessage(MessageFormat.format(Lang.FUNGEON_ADD_SUCCESS.toString(), args[2], fungeonID));
        } catch (final SQLException e) {
            sender.sendMessage(Lang.DB_EXCEPTION.toString());
            e.printStackTrace();
        }
    }

}
