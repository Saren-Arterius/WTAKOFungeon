package net.wtako.WTAKOFungeon.Utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Database;

import org.bukkit.Location;

public class LocationUtils {

    public static int saveLocation(Location location) throws SQLException {
        final PreparedStatement insStmt = Database.getConn().prepareStatement(
                "INSERT INTO locations (`world`, `x`, `y`, `z`) VALUES (?, ?, ?, ?)");
        insStmt.setString(1, location.getWorld().getName());
        insStmt.setDouble(2, location.getX());
        insStmt.setDouble(3, location.getY());
        insStmt.setDouble(4, location.getZ());
        insStmt.execute();
        int locID = insStmt.getGeneratedKeys().getInt(1);
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
}
