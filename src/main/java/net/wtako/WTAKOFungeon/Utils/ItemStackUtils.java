package net.wtako.WTAKOFungeon.Utils;

import java.text.MessageFormat;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemStackUtils {

    public static void giveToPlayerOrDrop(ItemStack itemStack, Player player, Location location) {
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(location, itemStack);
        } else {
            player.getInventory().addItem(itemStack);
        }
    }

    public static String toHumanReadable(ItemStack stack) {
        return MessageFormat.format(Lang.ITEM_PRINT_FORMAT.toString(), stack.getAmount(), stack.hasItemMeta()
                && !stack.getItemMeta().getDisplayName().equalsIgnoreCase("") ? stack.getItemMeta().getDisplayName()
                        : stack.getType().name());
    }
}
