package net.wtako.WTAKOFungeon.EventHandlers;

import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class FungeonSignListener implements Listener {

    @EventHandler
    public void onFungeonSignRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final Fungeon fungeon = Fungeon.getFungeonFromSignBlock(event.getClickedBlock());
        if (fungeon == null) {
            return;
        }
        if (fungeon.getStatus() == Fungeon.Status.PLAYING) {
            event.getPlayer().sendMessage(Lang.FUNGEON_IS_PLAYING.toString());
            event.setCancelled(true);
            return;
        }
        if (fungeon.getStatus() == Fungeon.Status.FUNGEON_NOT_VALID) {
            event.getPlayer().sendMessage(
                    MessageFormat.format(Lang.FUNGEON_IS_NOT_READY.toString(), fungeon.toString()));
            event.setCancelled(true);
            return;
        }
        fungeon.joinPlayer(event.getPlayer());
    }

    @EventHandler
    public void onFungeonSignBreak(BlockBreakEvent event) {
        final Fungeon fungeon = Fungeon.getFungeonFromSignBlock(event.getBlock());
        if (fungeon == null) {
            return;
        }
        if (!event.getPlayer().hasPermission(Main.artifactId + ".admin")) {
            event.setCancelled(true);
            return;
        }
        if (fungeon.getStatus() == Fungeon.Status.PLAYING) {
            event.getPlayer().sendMessage(Lang.FUNGEON_IS_PLAYING.toString());
            event.setCancelled(true);
            return;
        }
    }
}
