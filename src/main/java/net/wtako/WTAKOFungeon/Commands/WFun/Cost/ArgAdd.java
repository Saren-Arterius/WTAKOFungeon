package net.wtako.WTAKOFungeon.Commands.WFun.Cost;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Cost;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Utils.CommandHelper;
import net.wtako.WTAKOFungeon.Utils.ItemStackUtils;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ArgAdd {

    public ArgAdd(final CommandSender sender, final String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_COST_ADD.toString(),
                    CommandHelper.joinArgsInUse(args, args.length)));
            return;
        }
        final Integer fungeonID;
        try {
            fungeonID = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_COST_ADD.toString(), CommandHelper.joinArgsInUse(args, 2)));
            return;
        }
        final Fungeon fungeon = Fungeon.getAllFungeons().get(fungeonID);
        if (fungeon == null) {
            sender.sendMessage(MessageFormat.format(Lang.FUNGEON_DOES_NOT_EXIST.toString(), fungeonID));
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                if (args.length > 3) {
                    Integer money;
                    try {
                        money = Integer.parseInt(args[3]);
                        sender.sendMessage(MessageFormat.format(Lang.CASH_COST_ADDED.toString(), money,
                                fungeon.toString(), Cost.addCashCost(fungeonID, money)));
                        fungeon.updateCosts();
                    } catch (final NumberFormatException e) {
                        sender.sendMessage(MessageFormat.format(Lang.HELP_COST_ADD.toString(),
                                CommandHelper.joinArgsInUse(args, 3)));
                    } catch (final SQLException e) {
                        sender.sendMessage(Lang.DB_EXCEPTION.toString());
                        e.printStackTrace();
                    }
                } else {
                    try {
                        final ItemStack item = ((Player) sender).getItemInHand();
                        if (item.getType() == Material.AIR) {
                            sender.sendMessage(Lang.CANNOT_ADD_AIR.toString());
                            return;
                        }
                        sender.sendMessage(MessageFormat.format(Lang.ITEM_COST_ADDED.toString(),
                                ItemStackUtils.toHumanReadable(item), fungeon.toString(),
                                Cost.addItemCost(fungeonID, item)));
                        fungeon.updateCosts();
                    } catch (final SQLException e) {
                        sender.sendMessage(Lang.DB_EXCEPTION.toString());
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }
}
