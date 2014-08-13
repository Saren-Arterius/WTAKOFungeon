package net.wtako.WTAKOFungeon.Methods;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Utils.ItemUtils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Fungeon {

    private static HashMap<Integer, Fungeon> validFungeons = new HashMap<Integer, Fungeon>();
    private final ArrayList<Player>          players       = new ArrayList<Player>();
    private final boolean                    isPlaying     = false;
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
    private Integer                          fungeonTimer;
    private Integer                          waitTimer;

    public Fungeon(int fungeonID) throws SQLException {
        final PreparedStatement selStmt = Database.getConn()
                .prepareStatement("SELECT * FROM funegons WHERE row_id = ?");
        selStmt.setInt(1, fungeonID);
        final ResultSet result = selStmt.executeQuery();
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
        lobby = Fungeon.getLocation(result.getInt("lobby_loc_id"));
        waitRoom = Fungeon.getLocation(result.getInt("wait_rm_loc_id"));
        areaP1 = Fungeon.getLocation(result.getInt("area_p1_loc_id"));
        areaP2 = Fungeon.getLocation(result.getInt("area_p2_loc_id"));
        startPoint = Fungeon.getLocation(result.getInt("start_pt_loc_id"));
        invokeCommand = result.getString("run_command");
        if (checkValidity() == Validity.VALID) {
            fungeonTimer = timeLimit;
            waitTimer = waitTime;
            Fungeon.validFungeons.put(fungeonID, this);
        }
    }

    enum Validity {
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

    enum Error {
        FUNGEON_HAS_ALREADY_STARTED,
        FUNGEON_HAS_NOT_STARTED,
        PLAYER_LIST_IS_FULL,
        PLAYER_NOT_IN_LIST,
        PLAYER_ALREADY_JOINED,
        FUNGEON_NOT_VALID,
        SUCCESS
    }

    enum Status {
        FUNGEON_NOT_VALID,
        IDLE,
        WAITING_OTHER_PLAYERS,
        PLAYING,
    }

    public void mainLoop() {
        assert fungeonTimer > 0;
        assert waitTimer > 0;
    }

    public Error addPlayer(Player player) {
        if (checkValidity() != Validity.VALID) {
            return Error.FUNGEON_NOT_VALID;
        }
        if (isPlaying) {
            return Error.FUNGEON_HAS_ALREADY_STARTED;
        }
        if (players.contains(player)) {
            return Error.PLAYER_ALREADY_JOINED;
        }
        if (players.size() >= maxPlayers) {
            return Error.PLAYER_LIST_IS_FULL;
        }
        player.teleport(waitRoom);
        players.add(player);
        return Error.SUCCESS;
    }

    public Error playerLeave(Player player) {
        if (checkValidity() != Validity.VALID) {
            return Error.FUNGEON_NOT_VALID;
        }
        if (!players.contains(player)) {
            return Error.PLAYER_NOT_IN_LIST;
        }
        players.remove(player);
        player.teleport(lobby);
        return Error.SUCCESS;
    }

    public Error kickPlayer(Player player) {
        if (checkValidity() != Validity.VALID) {
            return Error.FUNGEON_NOT_VALID;
        }
        if (isPlaying) {
            return Error.FUNGEON_HAS_ALREADY_STARTED;
        }
        if (!players.contains(player)) {
            return Error.PLAYER_NOT_IN_LIST;
        }
        players.remove(player);
        player.teleport(lobby);
        return Error.SUCCESS;
    }

    public Error win() {
        if (getStatus() != Status.PLAYING) {
            return Error.FUNGEON_HAS_NOT_STARTED;
        }
        for (final Player player: new ArrayList<Player>(players)) {
            playerLeave(player);
        }
        return Error.SUCCESS;
    }

    public Error lose() {
        if (getStatus() != Status.PLAYING) {
            return Error.FUNGEON_HAS_NOT_STARTED;
        }
        for (final Player player: new ArrayList<Player>(players)) {
            playerLeave(player);
        }
        return Error.SUCCESS;
    }

    public Status getStatus() {
        if (checkValidity() != Validity.VALID) {
            return Status.FUNGEON_NOT_VALID;
        }
        if (isPlaying) {
            return Status.PLAYING;
        }
        if (players.size() >= minPlayers) {
            return Status.WAITING_OTHER_PLAYERS;
        }
        return Status.IDLE;
    }

    public Validity checkValidity() {
        if (name.equalsIgnoreCase("")) {
            return Validity.FUNGEON_NAME_IS_EMPTY;
        }
        if (invokeCommand.equalsIgnoreCase("")) {
            return Validity.INVOKE_COMMAND_IS_NULL;
        }
        if (timeLimit == null || timeLimit <= 60 || waitTime == null || waitTime <= 0) {
            return Validity.DEFAULT_VALUE_FAIL;
        }
        if (lobby == null) {
            return Validity.LOBBY_IS_NULL;
        }
        if (waitRoom == null) {
            return Validity.WAIT_ROOM_IS_NULL;
        }
        if (areaP1 == null) {
            return Validity.AREA_P1_IS_NULL;
        }
        if (areaP2 == null) {
            return Validity.AREA_P2_IS_NULL;
        }
        if (startPoint == null) {
            return Validity.START_POINT_IS_NULL;
        }
        if (minPlayers > maxPlayers) {
            return Validity.MIN_PLAYERS_IS_GREATER_THAN_MAX_PLAYERS;
        }
        if (minPlayers < 1) {
            return Validity.MIN_PLAYERS_IS_LESS_THAN_1;
        }
        if (Fungeon.isInRegion(areaP1, areaP2, startPoint)) {
            return Validity.MIN_PLAYERS_IS_LESS_THAN_1;
        }
        return Validity.VALID;
    }

    public static void awardPlayer(Player player, ArrayList<ItemStack> itemPrizes) {

    }

    public static void awardPlayer(Player player, int cashPrize) {

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

    public static ArrayList<ItemStack> getItemPrizes(int fungeonID) throws SQLException {
        final ArrayList<ItemStack> prizes = new ArrayList<ItemStack>();
        final PreparedStatement selStmt = Database.getConn().prepareStatement(
                "SELECT * FROM prizes WHERE funegon_id = ? AND item_json IS NOT NULL");
        selStmt.setInt(1, fungeonID);
        final ResultSet result = selStmt.executeQuery();
        while (result.next()) {
            prizes.add(ItemUtils.restoreItem(result.getString("item_json")));
        }
        result.close();
        selStmt.close();
        return prizes;
    }

    public static int getCashPrize(int fungeonID) throws SQLException {
        int cashPrize = 0;
        final PreparedStatement selStmt = Database.getConn().prepareStatement(
                "SELECT * FROM prizes WHERE funegon_id = ? AND cash_amount >= 0");
        selStmt.setInt(1, fungeonID);
        final ResultSet result = selStmt.executeQuery();
        while (result.next()) {
            cashPrize += result.getInt("cash_amount");
        }
        result.close();
        selStmt.close();
        return cashPrize;
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
