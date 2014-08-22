package net.wtako.WTAKOFungeon.Methods;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Utils.ItemUtils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Cost {

    public static int addCashCost(int fungeonID, int money) throws SQLException {
        final PreparedStatement insStmt = Database.getConn().prepareStatement(
                "INSERT INTO costs (`fungeon_id`, `cash_amount`) VALUES (?, ?)");
        insStmt.setInt(1, fungeonID);
        insStmt.setInt(2, money);
        insStmt.execute();
        final ResultSet result = insStmt.getGeneratedKeys();
        final int rowID = result.getInt(1);
        result.close();
        insStmt.close();
        return rowID;
    }

    public static int addItemCost(int fungeonID, ItemStack stack) throws SQLException {
        final PreparedStatement insStmt = Database.getConn().prepareStatement(
                "INSERT INTO costs (`fungeon_id`, `item_json`) VALUES (?, ?)");
        insStmt.setInt(1, fungeonID);
        insStmt.setString(2, ItemUtils.encodeItem(stack).toJSONString());
        insStmt.execute();
        final ResultSet result = insStmt.getGeneratedKeys();
        final int rowID = result.getInt(1);
        result.close();
        insStmt.close();
        return rowID;
    }

    public static boolean chargeItems(Inventory inv, ArrayList<ItemStack> stacks) {
        if (!Cost.hasItems(inv, stacks)) {
            return false;
        }
        for (final ItemStack stack: stacks) {
            int remainAmount = stack.getAmount();
            for (final ItemStack item: inv) {
                if (!stack.isSimilar(item)) {
                    continue;
                }
                if (item.getAmount() - remainAmount > 0) {
                    item.setAmount(item.getAmount() - remainAmount);
                    break;
                }
                remainAmount -= item.getAmount();
                inv.remove(item);
                if (remainAmount == 0) {
                    break;
                }
            }
        }
        return true;
    }

    public static boolean chargeMoney(Player player, int money) {
        if (!Cost.hasAtLeast(player, money)) {
            return false;
        }
        Main.econ.withdrawPlayer(player, money);
        return true;
    }

    public static void deleteAllCosts(int fungeonID) throws SQLException {
        final PreparedStatement delStmt = Database.getConn().prepareStatement("DELETE FROM costs WHERE fungeon_id = ?");
        delStmt.setInt(1, fungeonID);
        delStmt.execute();
        delStmt.close();
    }

    public static void deleteCost(int costID) throws SQLException {
        final PreparedStatement delStmt = Database.getConn().prepareStatement("DELETE FROM costs WHERE row_id = ?");
        delStmt.setInt(1, costID);
        delStmt.execute();
        delStmt.close();
    }

    public static int getCashCost(int fungeonID) throws SQLException {
        int cashCost = 0;
        final PreparedStatement selStmt = Database.getConn().prepareStatement(
                "SELECT * FROM costs WHERE fungeon_id = ? AND cash_amount >= 0");
        selStmt.setInt(1, fungeonID);
        final ResultSet result = selStmt.executeQuery();
        while (result.next()) {
            cashCost += result.getInt("cash_amount");
        }
        result.close();
        selStmt.close();
        return cashCost;
    }

    public static ItemStack getItemCost(int costID) throws SQLException {
        final PreparedStatement selStmt = Database.getConn().prepareStatement(
                "SELECT * FROM costs WHERE row_id = ? AND item_json IS NOT NULL");
        selStmt.setInt(1, costID);
        final ResultSet result = selStmt.executeQuery();
        if (!result.next()) {
            result.close();
            selStmt.close();
            return null;
        }
        final ItemStack item = ItemUtils.restoreItem(result.getString("item_json"));
        result.close();
        selStmt.close();
        return item;
    }

    public static ArrayList<ItemStack> getItemCosts(int fungeonID) throws SQLException {
        final ArrayList<ItemStack> costs = new ArrayList<ItemStack>();
        final PreparedStatement selStmt = Database.getConn().prepareStatement(
                "SELECT * FROM costs WHERE fungeon_id = ? AND item_json IS NOT NULL");
        selStmt.setInt(1, fungeonID);
        final ResultSet result = selStmt.executeQuery();
        while (result.next()) {
            costs.add(ItemUtils.restoreItem(result.getString("item_json")));
        }
        result.close();
        selStmt.close();
        return costs;
    }

    public static boolean hasAtLeast(Player player, int money) {
        if (Main.econ == null) {
            return false;
        }
        return Main.econ.has(player, money);
    }

    public static boolean hasItems(Inventory inv, ArrayList<ItemStack> stacks) {
        for (final ItemStack stack: stacks) {
            if (!inv.containsAtLeast(stack, stack.getAmount())) {
                return false;
            }
        }
        return true;
    }

}
