package net.wtako.WTAKOFungeon.Commands.WFun;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArgTest {

    public static HashMap<Player, ArrayList<Location>> testingLocationPlayers = new HashMap<Player, ArrayList<Location>>();

    public ArgTest(CommandSender sender, String[] args) {
        if (!ArgTest.testingLocationPlayers.containsKey(sender)) {
            ArgTest.testingLocationPlayers.put((Player) sender, new ArrayList<Location>());
            sender.sendMessage("p1?");
        } else {
            ArgTest.testingLocationPlayers.remove(sender).clear();
        }
    }

}
