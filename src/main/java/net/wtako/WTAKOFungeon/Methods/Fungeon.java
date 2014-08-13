package net.wtako.WTAKOFungeon.Methods;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import net.wtako.WTAKOFungeon.Main;

import org.bukkit.Location;

public class Fungeon {

    private static HashMap<Integer, Fungeon> validFungeons = new HashMap<Integer, Fungeon>();
    private boolean                          isPlaying     = false;
    private String                           name;
    private Integer                          timeLimit;
    private Integer                          minPlayers;
    private Integer                          maxPlayers;
    private Integer                          waitTime;
    private Location                         lobby;
    private Location                         waitRoom;
    private Location                         areaP1;
    private Location                         areaP2;
    private Location                         startPoint;
    private String                           invokeCommand;

    public Fungeon(int funegonID) throws SQLException {
        PreparedStatement selStmt = Database.getConn().prepareStatement("SELECT * FROM locations WHERE row_id = ?");
        selStmt.setInt(1, funegonID);
        ResultSet result = selStmt.executeQuery();
        if (!result.next()) {
            result.close();
            selStmt.close();
            return;
        }
        name = result.getString("funegon_name");
        timeLimit = result.getInt("time_limit");
        minPlayers = result.getInt("min_players");
        maxPlayers = result.getInt("max_players");
        waitTime = result.getInt("waitTime");
        lobby = getLocation(result.getInt("lobby_loc_id"));
        waitRoom = getLocation(result.getInt("wait_rm_loc_id"));
        areaP1 = getLocation(result.getInt("area_p1_loc_id"));
        areaP2 = getLocation(result.getInt("area_p2_loc_id"));
        startPoint = getLocation(result.getInt("start_pt_loc_id"));
        invokeCommand = result.getString("run_command");
    }

    enum Status {
        DEFAULT_VALUE_FAIL,
        FUNGEON_NAME_IS_EMPTY,
        LOBBY_IS_NULL,
        WAIT_ROOM_IS_NULL,
        MIN_PLAYERS_IS_GREATER_THAN_MAX_PLAYERS,
        MIN_PLAYERS_IS_LESS_THAN_1,
        AREA_P1_IS_NULL,
        AREA_P2_IS_NULL,
        START_POINT_IS_NULL,
        INVOKE_COMMAND_IS_NULL,
        START_POINT_NOT_IN_AREA,
        VALID
    }

    public Status checkStatus() {
        if (name.equalsIgnoreCase("")) {
            return Status.FUNGEON_NAME_IS_EMPTY;
        }
        if (invokeCommand.equalsIgnoreCase("")) {
            return Status.INVOKE_COMMAND_IS_NULL;
        }
        if (timeLimit == null || timeLimit <= 60 || waitTime == null || waitTime <= 0) {
            return Status.DEFAULT_VALUE_FAIL;
        }
        if (lobby == null) {
            return Status.LOBBY_IS_NULL;
        }
        if (waitRoom == null) {
            return Status.WAIT_ROOM_IS_NULL;
        }
        if (areaP1 == null) {
            return Status.AREA_P1_IS_NULL;
        }
        if (areaP2 == null) {
            return Status.AREA_P2_IS_NULL;
        }
        if (startPoint == null) {
            return Status.START_POINT_IS_NULL;
        }
        if (minPlayers > maxPlayers) {
            return Status.MIN_PLAYERS_IS_GREATER_THAN_MAX_PLAYERS;
        }
        if (minPlayers < 1) {
            return Status.MIN_PLAYERS_IS_LESS_THAN_1;
        }
        if (isInRegion(areaP1, areaP2, startPoint)) {
            return Status.MIN_PLAYERS_IS_LESS_THAN_1;
        }
        return Status.VALID;
    }

    public static boolean isInRegion(Location p1, Location p2, Location check) {
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
