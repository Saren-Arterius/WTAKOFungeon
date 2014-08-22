package net.wtako.WTAKOFungeon.EventHandlers;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Status;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerGameListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Fungeon fungeon = Fungeon.getJoinedFungeon(event.getEntity());
        if (fungeon == null) {
            return;
        }
        if (fungeon.isRespawning() && fungeon.getStatus() == Status.PLAYING) {
            return;
        }
        fungeon.playerLeave(event.getEntity());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Fungeon fungeon = Fungeon.getJoinedFungeon(event.getPlayer());
        if (fungeon == null) {
            return;
        }
        fungeon.playerLeave(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final Fungeon fungeon = Fungeon.getJoinedFungeon(event.getPlayer());
        if (fungeon == null) {
            return;
        }
        if (fungeon.isRespawning() && fungeon.getStatus() == Status.PLAYING) {
            event.setRespawnLocation(fungeon.getStartLocation());
        }
    }

}
