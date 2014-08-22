package net.wtako.WTAKOFungeon.EventHandlers;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Status;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerItemDropListener implements Listener {

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        for (final Fungeon fungeon: Fungeon.getAllFungeons().values()) {
            if (fungeon.getStatus() != Status.PLAYING && fungeon.getJoinedPlayers().contains(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }

}
