package net.wtako.WTAKOFungeon.Commands.WFly;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArgTest {

    public static HashMap<Player, ArrayList<Location>> testingLocationPlayers = new HashMap<Player, ArrayList<Location>>();

    public ArgTest(CommandSender sender) {
        if (!testingLocationPlayers.containsKey(sender)) {
            testingLocationPlayers.put((Player) sender, new ArrayList<Location>());
            sender.sendMessage("p1?");
        } else {
            testingLocationPlayers.remove(sender).clear();
        }
    }

}
