package net.wtako.WTAKOFungeon.Methods;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import net.wtako.WTAKOFungeon.Main;

import org.bukkit.Location;

public class Funegon {

    private static HashMap<Integer, Funegon> activeFunegons = new HashMap<Integer, Funegon>();
    private String                           name;
    private int                              timeLimit;
    private int                              min_players;
    private int                              max_players;
    private int                              wait_time;
    private Location                         lobby;
    private Location                         waitRoom;
    private Location                         areaP1;
    private Location                         areaP2;
    private Location                         startPoint;
    private String                           invokeCommand;

    public Funegon(int funegonID) {

    }

    public static boolean inRegion(Location p1, Location p2, Location check) {
        if (p1.getWorld() != p2.getWorld()) {
            return false;
        }
        if (p1.getWorld() != check.getWorld()) {
            return false;
        }
        if (!((p1.getX() <= check.getX() && check.getX() <= p2.getX()) || (p2.getX() <= check.getX() && check.getX() <= p1
                .getX()))) {
            return false;
        }
        if (!((p1.getY() <= check.getY() && check.getY() <= p2.getY()) || (p2.getY() <= check.getY() && check.getY() <= p1
                .getY()))) {
            return false;
        }
        if (!((p1.getZ() <= check.getZ() && check.getZ() <= p2.getZ()) || (p2.getZ() <= check.getZ() && check.getZ() <= p1
                .getZ()))) {
            return false;
        }
        return true;
    }

    public static Location getLocation(int locID) throws SQLException {
        PreparedStatement selStmt = Database.getConn().prepareStatement("SELECT * FROM locations WHERE row_id = ?");
        selStmt.setInt(1, locID);
        ResultSet result = selStmt.executeQuery();
        if (!result.next()) {
            result.close();
            selStmt.close();
            return null;
        }
        Location location = new Location(Main.getInstance().getServer().getWorld(result.getString("world")),
                result.getDouble("x"), result.getDouble("y"), result.getDouble("z"));
        result.close();
        selStmt.close();
        return location;
    }
}
