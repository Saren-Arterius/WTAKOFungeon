package net.wtako.WTAKOFungeon.Commands.WFun;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;

public class ArgReload {

    public ArgReload(CommandSender sender, String[] args) {
        Main.getInstance().reloadConfig();
        Main.getInstance().getServer().getPluginManager().disablePlugin(Main.getInstance());
        Main.getInstance().getServer().getPluginManager().enablePlugin(Main.getInstance());
        sender.sendMessage(Lang.PLUGIN_RELOADED.toString());
    }

}
