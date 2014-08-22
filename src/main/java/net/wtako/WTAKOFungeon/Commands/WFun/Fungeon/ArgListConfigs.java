package net.wtako.WTAKOFungeon.Commands.WFun.Fungeon;

import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Utils.CommandsWFun;
import net.wtako.WTAKOFungeon.Utils.Lang;
import net.wtako.WTAKOFungeon.Utils.LocationUtils;

import org.bukkit.command.CommandSender;

public class ArgListConfigs {

    public ArgListConfigs(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_FUNGEON_LIST_CONFIGS.toString(),
                    CommandsWFun.joinArgsInUse(args, args.length)));
            return;
        }
        Integer fungeonID;
        try {
            fungeonID = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_FUNGEON_LIST_CONFIGS.toString(),
                    CommandsWFun.joinArgsInUse(args, args.length)));
            return;
        }
        final Fungeon fungeon = Fungeon.getAllFungeons().get(fungeonID);
        if (fungeon == null) {
            sender.sendMessage(MessageFormat.format(Lang.FUNGEON_DOES_NOT_EXIST.toString(), fungeonID));
            return;
        }
        sender.sendMessage("====================");
        sender.sendMessage(MessageFormat.format("ID: {0}", fungeon.getID()));
        sender.sendMessage(MessageFormat.format("Name: {0}", fungeon.getName()));
        sender.sendMessage(MessageFormat.format("Enabled: {0}", fungeon.isEnabled()));
        sender.sendMessage(MessageFormat.format("Respawning: {0}", fungeon.isRespawning()));
        sender.sendMessage(MessageFormat.format("Players: {0} - {1}", fungeon.getMinPlayers(), fungeon.getMaxPlayers()));
        sender.sendMessage(MessageFormat.format("Time limit: {0}s", fungeon.getFungeonTimeLimit()));
        sender.sendMessage(MessageFormat.format("Wait room time limit: {0}s", fungeon.getWaitRoomTimeLimit()));
        sender.sendMessage(MessageFormat.format("Wait room: {0}", LocationUtils.toHumanReadable(fungeon.getWaitRoom())));
        sender.sendMessage(MessageFormat.format("Lobby: {0}", LocationUtils.toHumanReadable(fungeon.getLobby())));
        sender.sendMessage(MessageFormat.format("Sign: {0}", LocationUtils.toHumanReadable(fungeon.getSignLocation())));
        sender.sendMessage(MessageFormat.format("Area point 1: {0}", LocationUtils.toHumanReadable(fungeon.getAreaP1())));
        sender.sendMessage(MessageFormat.format("Area point 2: {0}", LocationUtils.toHumanReadable(fungeon.getAreaP2())));
        sender.sendMessage(MessageFormat.format("Start point: {0}",
                LocationUtils.toHumanReadable(fungeon.getStartLocation())));
        sender.sendMessage(MessageFormat.format("Status: {0}", fungeon.getStatus().name()));
        sender.sendMessage("====================");
    }

}
