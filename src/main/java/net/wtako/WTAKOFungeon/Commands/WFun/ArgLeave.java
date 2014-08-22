package net.wtako.WTAKOFungeon.Commands.WFun;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Events.PlayerLeaveFungeonEvent;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.LeaveCause;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArgLeave {

    public ArgLeave(final CommandSender sender, String[] args) {
        final Player player = (Player) sender;
        final Fungeon fungeon = Fungeon.getJoinedFungeon(player);
        if (fungeon == null) {
            player.sendMessage(Lang.NOT_JOINED_FUNGEON.toString());
            return;
        }
        final PlayerLeaveFungeonEvent event = new PlayerLeaveFungeonEvent(player, null, fungeon, LeaveCause.COMMAND);
        Main.getInstance().getServer().getPluginManager().callEvent(event);
        fungeon.playerLeave(player);
    }

}
