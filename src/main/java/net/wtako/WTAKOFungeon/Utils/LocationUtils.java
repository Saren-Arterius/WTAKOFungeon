package net.wtako.WTAKOFungeon.Utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Database;

import org.bukkit.Location;

public class LocationUtils {

    public static void deleteLocation(int locID) throws SQLException {
        final PreparedStatement delStmt = Database.getConn().prepareStatement("DELETE locations WHERE row_id = ?");
        delStmt.setInt(1, locID);
        delStmt.execute();
        delStmt.close();
    }

    public static int saveLocation(Location location) throws SQLException {
        if (location == null) {
            return -1;
        }
        final PreparedStatement insStmt = Database.getConn().prepareStatement(
                "INSERT INTO locations (`world`, `x`, `y`, `z`) VALUES (?, ?, ?, ?)");
        insStmt.setString(1, location.getWorld().getName());
        insStmt.setDouble(2, location.getX());
        insStmt.setDouble(3, location.getY());
        insStmt.setDouble(4, location.getZ());
        insStmt.execute();
        final int locID = insStmt.getGeneratedKeys().getInt(1);
        insStmt.close();
        return locID;
    }

    public static Location getLocation(int locID) throws SQLException {
        final PreparedStatement selStmt = Database.getConn().prepareStatement(
                "SELECT * FROM locations WHERE row_id = ?");
        selStmt.setInt(1, locID);
        final ResultSet result = selStmt.executeQuery();
        if (!result.next()) {
            result.close();
            selStmt.close();
            return null;
        }
        final Location location = new Location(Main.getInstance().getServer().getWorld(result.getString("world")),
                result.getDouble("x"), result.getDouble("y"), result.getDouble("z"));
        result.close();
        selStmt.close();
        return location;
    }

    public static Integer getLocationID(Location location) throws SQLException {
        if (location == null) {
            return null;
        }
        final PreparedStatement selStmt = Database.getConn().prepareStatement(
                "SELECT * FROM locations WHERE world = ? AND x = ? AND y = ? AND z = ?");
        selStmt.setString(1, location.getWorld().getName());
        selStmt.setDouble(2, location.getX());
        selStmt.setDouble(3, location.getY());
        selStmt.setDouble(4, location.getZ());
        final ResultSet result = selStmt.executeQuery();
        if (!result.next()) {
            result.close();
            selStmt.close();
            return null;
        }
        final int locID = result.getInt("row_id");
        result.close();
        selStmt.close();
        return locID;
    }
}
