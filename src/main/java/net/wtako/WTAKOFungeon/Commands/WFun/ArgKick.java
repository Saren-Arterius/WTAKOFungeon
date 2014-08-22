package net.wtako.WTAKOFungeon.Commands.WFun;

import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Error;
import net.wtako.WTAKOFungeon.Utils.Commands;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArgKick {

    @SuppressWarnings("deprecation")
    public ArgKick(final CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_KICK.toString(), Commands.joinArgsInUse(args, 1)));
            return;
        }
        final Player kicker = (Player) sender;
        final Player kickee = Main.getInstance().getServer().getPlayer(args[1]);
        if (kickee == null) {
            kicker.sendMessage(MessageFormat.format(Lang.PLAYER_NOT_FOUND.toString(), args[1]));
            return;
        }

        final Fungeon fungeon = Fungeon.getJoinedFungeon(kickee);
        if (fungeon == null) {
            kicker.sendMessage(MessageFormat.format(Lang.PLAYER_NOT_IN_FUNGEON.toString(), kickee.getName()));
            return;
        }
        final Error result = fungeon.kickPlayer(kicker, kickee, kicker.hasPermission(Main.artifactId + ".admin"));
        if (result == Error.FUNGEON_HAS_ALREADY_STARTED) {
            kicker.sendMessage(Lang.FUNGEON_IS_PLAYING_KICK.toString());
        } else if (result == Error.NOT_TEAM_LEADER) {
            kicker.sendMessage(Lang.YOU_ARE_NOT_LEADER.toString());
        } else if (result == Error.SUCCESS) {
            kicker.sendMessage(MessageFormat.format(Lang.PLAYER_KICKED.toString(), kickee.getName(), fungeon.toString()));
        } else {
            kicker.sendMessage(result.name());
        }
    }

}
