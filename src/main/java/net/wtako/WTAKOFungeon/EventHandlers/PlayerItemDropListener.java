package net.wtako.WTAKOFungeon.EventHandlers;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Status;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerItemDropListener implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        for (final Fungeon fungeon: Fungeon.getAllFungeons().values()) {
            if (fungeon.getStatus() != Status.PLAYING && fungeon.getJoinedPlayers().contains(event.getPlayer())) {
                event.setCancelled(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getPlayer().updateInventory();
                    }
                }.runTaskLater(Main.getInstance(), 10L);
                break;
            }
        }
    }

}
