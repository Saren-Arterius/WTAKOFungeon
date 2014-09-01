package net.wtako.WTAKOFungeon.EventHandlers;

import java.util.HashMap;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Status;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerGameListener implements Listener {

    private static HashMap<Player, Fungeon> lostPlayers = new HashMap<Player, Fungeon>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Fungeon fungeon = Fungeon.getJoinedFungeon(event.getEntity());
        if (fungeon == null) {
            return;
        }
        if (fungeon.isRespawning() && fungeon.getStatus() == Status.PLAYING) {
            return;
        }
        fungeon.playerLeave(event.getEntity(), false);
        PlayerGameListener.lostPlayers.put(event.getEntity(), fungeon);
        event.setDeathMessage(Lang.YOU_LOSE.toString());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (PlayerGameListener.lostPlayers.containsKey(event.getPlayer())) {
            event.getPlayer().teleport(PlayerGameListener.lostPlayers.remove(event.getPlayer()).getLobby());
            return;
        }
        final Fungeon fungeon = Fungeon.getJoinedFungeon(event.getPlayer());
        if (fungeon == null) {
            return;
        }
        fungeon.playerLeave(event.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (PlayerGameListener.lostPlayers.containsKey(event.getPlayer())) {
            event.setRespawnLocation(PlayerGameListener.lostPlayers.remove(event.getPlayer()).getLobby());
            return;
        }
        final Fungeon fungeon = Fungeon.getJoinedFungeon(event.getPlayer());
        if (fungeon == null) {
            return;
        }
        if (fungeon.isRespawning() && fungeon.getStatus() == Status.PLAYING) {
            event.setRespawnLocation(fungeon.getStartLocation());
        }
    }

}
