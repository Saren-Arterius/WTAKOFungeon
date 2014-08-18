package net.wtako.WTAKOFungeon.Methods;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Utils.Config;
import net.wtako.WTAKOFungeon.Utils.ItemStackUtils;
import net.wtako.WTAKOFungeon.Utils.ItemUtils;
import net.wtako.WTAKOFungeon.Utils.Lang;
import net.wtako.WTAKOFungeon.Utils.LocationUtils;

import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Fungeon {

    private static HashMap<Integer, Fungeon> validFungeons = new HashMap<Integer, Fungeon>();
    private final ArrayList<Player>          players       = new ArrayList<Player>();
    private final boolean                    isPlaying     = false;
    private Integer                          id;
    private String                           name;
    private Integer                          fungeonTimeLimit;
    private Integer                          minPlayers;
    private Integer                          maxPlayers;
    private Integer                          waitRoomTime;
    private Location                         lobby;
    private Location                         waitRoom;
    private Location                         areaP1;
    private Location                         areaP2;
    private Location                         startPoint;
    private String                           invokeCommand;
    private Integer                          fungeonTimer;
    private Integer                          waitRoomTimer;
    private Integer                          winTimer;

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
        id = fungeonID;
        name = result.getString("funegon_name");
        fungeonTimeLimit = result.getInt("time_limit");
        minPlayers = result.getInt("min_players");
        maxPlayers = result.getInt("max_players");
        waitRoomTime = result.getInt("waitTime");
        lobby = LocationUtils.getLocation(result.getInt("lobby_loc_id"));
        waitRoom = LocationUtils.getLocation(result.getInt("wait_rm_loc_id"));
        areaP1 = LocationUtils.getLocation(result.getInt("area_p1_loc_id"));
        areaP2 = LocationUtils.getLocation(result.getInt("area_p2_loc_id"));
        startPoint = LocationUtils.getLocation(result.getInt("start_pt_loc_id"));
        invokeCommand = result.getString("run_command");
        if (checkValidity() == Validity.VALID) {
            fungeonTimer = fungeonTimeLimit;
            waitRoomTimer = waitRoomTime;
            Fungeon.validFungeons.put(fungeonID, this);
        }
    }

    public enum Validity {
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

    public enum Error {
        FUNGEON_HAS_ALREADY_STARTED,
        FUNGEON_HAS_NOT_STARTED,
        PLAYER_LIST_IS_FULL,
        PLAYER_NOT_IN_LIST,
        PLAYER_ALREADY_JOINED,
        NOT_ENOUGH_PLAYERS,
        FUNGEON_NOT_VALID,
        SUCCESS
    }

    public enum Status {
        FUNGEON_NOT_VALID,
        IDLE,
        WAITING_OTHER_PLAYERS,
        PLAYING,
    }

    public void checkWaitingRoom() {
        if (getStatus() != Status.WAITING_OTHER_PLAYERS) {
            waitRoomTimer = waitRoomTime;
            return;
        }
        if (waitRoomTimer-- <= 0) {
            start();
        }
    }

    public void checkFungeon() {
        if (getStatus() != Status.PLAYING) {
            fungeonTimer = fungeonTimeLimit;
            winTimer = Config.NO_ENEMIES_WIN_TIMER.getInt();
            return;
        }
        int enemiesAlive = 0;
        for (Entity mob: startPoint.getWorld().getEntities()) {
            if ((mob instanceof Monster || mob instanceof Animals) && isInRegion(areaP1, areaP2, mob.getLocation())) {
                enemiesAlive++;
            }
        }
        if (enemiesAlive == 0 && winTimer-- <= 0) {
            win();
            return;
        }
        winTimer = Config.NO_ENEMIES_WIN_TIMER.getInt();
        if (fungeonTimer-- <= 0) {
            lose();
        }

    }

    public void mainLoop() {
        assert id != null;
        assert fungeonTimer != null;
        assert waitRoomTimer != null;
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

    public Error start() {
        if (checkValidity() != Validity.VALID) {
            return Error.FUNGEON_NOT_VALID;
        }
        if (getStatus() == Status.PLAYING) {
            return Error.FUNGEON_HAS_ALREADY_STARTED;
        }
        if (players.size() >= minPlayers) {
            return Error.NOT_ENOUGH_PLAYERS;
        }
        for (Player player: players) {
            player.teleport(startPoint);
        }
        return Error.SUCCESS;
    }

    public Error forceStart() {
        if (checkValidity() != Validity.VALID) {
            return Error.FUNGEON_NOT_VALID;
        }
        if (getStatus() == Status.PLAYING) {
            return Error.FUNGEON_HAS_ALREADY_STARTED;
        }
        for (Player player: players) {
            player.teleport(startPoint);
        }
        return Error.SUCCESS;
    }

    public Error forceEnd() {
        return lose();
    }

    private Error win() {
        if (getStatus() != Status.PLAYING) {
            return Error.FUNGEON_HAS_NOT_STARTED;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    final ArrayList<ItemStack> itemPrizes = Fungeon.getItemPrizes(id);
                    final int getCashPrize = Fungeon.getCashPrize(id);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (final Player player: new ArrayList<Player>(players)) {
                                playerLeave(player);
                                Fungeon.awardPlayer(player, itemPrizes);
                                Fungeon.awardPlayer(player, getCashPrize);
                            }
                        }
                    }.runTask(Main.getInstance());
                } catch (SQLException e) {
                    for (final Player player: new ArrayList<Player>(players)) {
                        playerLeave(player);
                        player.sendMessage(Lang.DB_EXCEPTION.toString());
                    }
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
        return Error.SUCCESS;
    }

    private Error lose() {
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
        if (fungeonTimeLimit == null || fungeonTimeLimit <= 60 || waitRoomTime == null || waitRoomTime <= 0) {
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
        if (!Fungeon.isInRegion(areaP1, areaP2, startPoint)) {
            return Validity.START_POINT_NOT_IN_AREA;
        }
        return Validity.VALID;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public String toString() {
        return MessageFormat.format(Lang.FUNGEON_PLAYERS_FORMAT.toString(), id, name);
    }

    public static HashMap<Integer, Fungeon> getValidFungeons() {
        return validFungeons;
    }

    private static void awardPlayer(Player player, ArrayList<ItemStack> itemPrizes) {
        for (final ItemStack itemStack: itemPrizes) {
            ItemStackUtils.giveToPlayerOrDrop(itemStack, player, player.getLocation());
        }
    }

    private static void awardPlayer(Player player, int cashPrize) {
        if (Main.econ != null) {
            Main.econ.depositPlayer(player, cashPrize);
        } else {
            player.sendMessage(MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Vault"));
        }
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

}
