package net.wtako.WTAKOFungeon.EventHandlers;

import java.util.ArrayList;

import net.wtako.WTAKOFungeon.Commands.WFly.ArgTest;
import net.wtako.WTAKOFungeon.Methods.Funegon;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class TestListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!ArgTest.testingLocationPlayers.containsKey(event.getPlayer())) {
            return;
        }
        event.setCancelled(true);
        ArrayList<Location> locations = ArgTest.testingLocationPlayers.get(event.getPlayer());
        locations.add(event.getBlock().getLocation());
        switch (locations.size()) {
            case 1:
                event.getPlayer().sendMessage("p2?");
                break;
            case 2:
                event.getPlayer().sendMessage("check location?");
                break;
            case 3:
                event.getPlayer().sendMessage(
                        String.valueOf(Funegon.inRegion(locations.get(0), locations.get(1), locations.get(2))));
                ArgTest.testingLocationPlayers.remove(event.getPlayer()).clear();
                break;
        }
    }

}
