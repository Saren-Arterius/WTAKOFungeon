package net.wtako.WTAKOFungeon.EventHandlers;

import java.sql.SQLException;
import java.util.ArrayList;

import net.wtako.WTAKOFungeon.Commands.WFun.ArgTest;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Utils.LocationUtils;

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
        final ArrayList<Location> locations = ArgTest.testingLocationPlayers.get(event.getPlayer());
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
                        String.valueOf(Fungeon.isInRegion(locations.get(0), locations.get(1), locations.get(2))));
                for (final Location location: locations) {
                    try {
                        event.getPlayer().sendMessage(String.valueOf(LocationUtils.saveLocation(location)));
                    } catch (final SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                ArgTest.testingLocationPlayers.remove(event.getPlayer()).clear();
                break;
        }
    }

}
