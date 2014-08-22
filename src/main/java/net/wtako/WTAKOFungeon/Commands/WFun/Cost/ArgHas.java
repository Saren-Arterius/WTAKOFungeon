package net.wtako.WTAKOFungeon.Commands.WFun.Cost;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Cost;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Utils.CommandHelper;
import net.wtako.WTAKOFungeon.Utils.ItemStackUtils;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ArgHas {

    public ArgHas(final CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_COST_HAS.toString(),
                    CommandHelper.joinArgsInUse(args, args.length)));
            return;
        }
        final Integer fungeonID;
        try {
            fungeonID = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_COST_HAS.toString(), CommandHelper.joinArgsInUse(args, 2)));
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
                try {
                    final ArrayList<ItemStack> itemCosts = Cost.getItemCosts(fungeonID);
                    final int cashCost = Cost.getCashCost(fungeonID);
                    boolean canAfford = true;
                    for (final ItemStack stack: itemCosts) {
                        if (((Player) sender).getInventory().containsAtLeast(stack, stack.getAmount())) {
                            sender.sendMessage(MessageFormat.format(Lang.YOU_HAVE.toString(),
                                    ItemStackUtils.toHumanReadable(stack)));
                        } else {
                            sender.sendMessage(MessageFormat.format(Lang.YOU_DONT_HAVE.toString(),
                                    ItemStackUtils.toHumanReadable(stack)));
                            canAfford = false;
                        }
                    }
                    if (Cost.hasAtLeast((Player) sender, cashCost)) {
                        sender.sendMessage(MessageFormat.format(Lang.YOU_HAVE_MONEY.toString(), cashCost));
                    } else {
                        sender.sendMessage(MessageFormat.format(Lang.YOU_DONT_HAVE_MONEY.toString(), cashCost));
                        canAfford = false;
                    }
                    if (canAfford) {
                        sender.sendMessage(MessageFormat.format(Lang.YOU_CAN_AFFORD_COST.toString(), fungeon.getID(),
                                fungeon.toString()));
                    } else {
                        sender.sendMessage(MessageFormat.format(Lang.YOU_CANT_AFFORD_COST.toString(), fungeon.getID(),
                                fungeon.toString()));
                    }
                } catch (final SQLException e) {
                    sender.sendMessage(Lang.DB_EXCEPTION.toString());
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }
}
