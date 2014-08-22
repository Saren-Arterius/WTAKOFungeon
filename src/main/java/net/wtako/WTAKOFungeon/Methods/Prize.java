package net.wtako.WTAKOFungeon.Methods;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import net.wtako.WTAKOFungeon.Utils.ItemUtils;

import org.bukkit.inventory.ItemStack;

public class Prize {

    public static int addCashPrize(int fungeonID, int money) throws SQLException {
        final PreparedStatement insStmt = Database.getConn().prepareStatement(
                "INSERT INTO prizes (`fungeon_id`, `cash_amount`) VALUES (?, ?)");
        insStmt.setInt(1, fungeonID);
        insStmt.setInt(2, money);
        insStmt.execute();
        final ResultSet result = insStmt.getGeneratedKeys();
        final int rowID = result.getInt(1);
        result.close();
        insStmt.close();
        return rowID;
    }

    public static int addItemPrize(int fungeonID, ItemStack stack) throws SQLException {
        final PreparedStatement insStmt = Database.getConn().prepareStatement(
                "INSERT INTO prizes (`fungeon_id`, `item_json`) VALUES (?, ?)");
        insStmt.setInt(1, fungeonID);
        insStmt.setString(2, ItemUtils.encodeItem(stack).toJSONString());
        insStmt.execute();
        final ResultSet result = insStmt.getGeneratedKeys();
        final int rowID = result.getInt(1);
        result.close();
        insStmt.close();
        return rowID;
    }

    public static void deleteAllPrizes(int fungeonID) throws SQLException {
        final PreparedStatement delStmt = Database.getConn()
                .prepareStatement("DELETE FROM prizes WHERE fungeon_id = ?");
        delStmt.setInt(1, fungeonID);
        delStmt.execute();
        delStmt.close();
    }

    public static void deletePrize(int prizeID) throws SQLException {
        final PreparedStatement delStmt = Database.getConn().prepareStatement("DELETE FROM prizes WHERE row_id = ?");
        delStmt.setInt(1, prizeID);
        delStmt.execute();
        delStmt.close();
    }

    public static int getCashPrize(int fungeonID) throws SQLException {
        int cashPrize = 0;
        final PreparedStatement selStmt = Database.getConn().prepareStatement(
                "SELECT * FROM prizes WHERE fungeon_id = ? AND cash_amount >= 0");
        selStmt.setInt(1, fungeonID);
        final ResultSet result = selStmt.executeQuery();
        while (result.next()) {
            cashPrize += result.getInt("cash_amount");
        }
        result.close();
        selStmt.close();
        return cashPrize;
    }

    public static ItemStack getItemPrize(int prizeID) throws SQLException {
        final PreparedStatement selStmt = Database.getConn().prepareStatement(
                "SELECT * FROM prizes WHERE row_id = ? AND item_json IS NOT NULL");
        selStmt.setInt(1, prizeID);
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

    public static ArrayList<ItemStack> getItemPrizes(int fungeonID) throws SQLException {
        final ArrayList<ItemStack> prizes = new ArrayList<ItemStack>();
        final PreparedStatement selStmt = Database.getConn().prepareStatement(
                "SELECT * FROM prizes WHERE fungeon_id = ? AND item_json IS NOT NULL");
        selStmt.setInt(1, fungeonID);
        final ResultSet result = selStmt.executeQuery();
        while (result.next()) {
            prizes.add(ItemUtils.restoreItem(result.getString("item_json")));
        }
        result.close();
        selStmt.close();
        return prizes;
    }

}
