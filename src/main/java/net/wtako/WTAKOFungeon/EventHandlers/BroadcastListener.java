package net.wtako.WTAKOFungeon.EventHandlers;

import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Events.FungeonEndEvent;
import net.wtako.WTAKOFungeon.Events.PlayerJoinFungeonEvent;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BroadcastListener implements Listener {

    @EventHandler
    public void onFungeonEnd(FungeonEndEvent event) {
        if (event.getFungeon().getJoinedPlayers().size() == 0) {
            return;
        }
        if (event.getCashPrize() == null && event.getPrizes() == null) {
            final String msg = MessageFormat.format(Lang.BC_FUNGEON_LOST.toString(), event.getFungeon()
                    .getJoinedPlayers().size(), event.getFungeon().getName());
            Main.getInstance().getServer().broadcastMessage(msg);
        } else {
            final String msg = MessageFormat.format(Lang.BC_FUNGEON_WIN.toString(), event.getFungeon()
                    .getJoinedPlayers().size(), event.getFungeon().getName());
            Main.getInstance().getServer().broadcastMessage(msg);
        }
    }

    @EventHandler
    public void onPlayerJoinFungeon(PlayerJoinFungeonEvent event) {
        final int lack = event.getFungeon().getMinPlayers() - event.getFungeon().getJoinedPlayers().size() - 1;
        final String msg = MessageFormat.format(Lang.BC_PLAYER_JOIN.toString(), event.getPlayer().getName(), event
                .getFungeon().getName(), event.getFungeon().getJoinedPlayers().size() + 1, event.getFungeon()
                .getMaxPlayers(), lack < 0 ? 0 : lack);
        Main.getInstance().getServer().broadcastMessage(msg);
    }
}
