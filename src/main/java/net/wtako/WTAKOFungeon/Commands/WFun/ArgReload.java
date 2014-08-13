package net.wtako.WTAKOFungeon.Commands.WFun;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;

public class ArgReload {

    public ArgReload(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Main.getInstance().getProperty("artifactId") + ".reload")) {
            sender.sendMessage(Lang.NO_PERMISSION_COMMAND.toString());
            return;
        }
        Main.getInstance().getServer().getPluginManager().disablePlugin(Main.getInstance());
        Main.getInstance().getServer().getPluginManager().enablePlugin(Main.getInstance());
        Main.getInstance().reloadConfig();
        sender.sendMessage(Lang.PLUGIN_RELOADED.toString());
    }

}
