package net.wtako.WTAKOFungeon.Commands.WFun.Prize;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Prize;
import net.wtako.WTAKOFungeon.Utils.CommandHelper;
import net.wtako.WTAKOFungeon.Utils.ItemStackUtils;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ArgGet {

    public ArgGet(final CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_PRIZE_GET.toString(),
                    CommandHelper.joinArgsInUse(args, args.length)));
            return;
        }
        final Integer prizeID;
        try {
            prizeID = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            sender.sendMessage(MessageFormat.format(Lang.HELP_PRIZE_GET.toString(),
                    CommandHelper.joinArgsInUse(args, 2)));
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    final ItemStack prize = Prize.getItemPrize(prizeID);
                    if (prize == null) {
                        sender.sendMessage(MessageFormat.format(Lang.NO_SUCH_A_ITEM_PRIZE.toString(), prizeID));
                        return;
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ItemStackUtils.giveToPlayerOrDrop(prize, (Player) sender, ((Player) sender).getLocation());
                            sender.sendMessage(ItemStackUtils.toHumanReadable(prize));
                        }
                    }.runTask(Main.getInstance());
                } catch (final SQLException e) {
                    sender.sendMessage(Lang.DB_EXCEPTION.toString());
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

}
