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
    private static HashMap<Integer, Fungeon> allFungeons   = new HashMap<Integer, Fungeon>();
    private final ArrayList<Player>          players       = new ArrayList<Player>();
    private boolean                          isPlaying     = false;
    private Integer                          id;
    private boolean                          enabled       = true;
    private String                           name;
    private Integer                          fungeonTimeLimit;
    private Integer                          minPlayers;
    private Integer                          maxPlayers;
    private Integer                          waitRoomTimeLimit;
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
                .prepareStatement("SELECT * FROM fungeons WHERE row_id = ?");
        selStmt.setInt(1, fungeonID);
        final ResultSet result = selStmt.executeQuery();
        if (!result.next()) {
            result.close();
            selStmt.close();
            return;
        }
        id = fungeonID;
        enabled = result.getInt("enabled") == 0 ? false : true;
        name = result.getString("fungeon_name");
        fungeonTimeLimit = result.getInt("time_limit");
        minPlayers = result.getInt("min_players");
        maxPlayers = result.getInt("max_players");
        waitRoomTimeLimit = result.getInt("wait_time");
        lobby = LocationUtils.getLocation(result.getInt("lobby_loc_id"));
        waitRoom = LocationUtils.getLocation(result.getInt("wait_rm_loc_id"));
        areaP1 = LocationUtils.getLocation(result.getInt("area_p1_loc_id"));
        areaP2 = LocationUtils.getLocation(result.getInt("area_p2_loc_id"));
        startPoint = LocationUtils.getLocation(result.getInt("start_pt_loc_id"));
        invokeCommand = result.getString("run_command");
        Fungeon.allFungeons.put(fungeonID, this);
        if (checkValidity() == Validity.VALID) {
            Fungeon.validFungeons.put(fungeonID, this);
        }
    }

    public enum Validity {
        NOT_ENABLED,
        DEFAULT_VALUE_FAIL,
        FUNGEON_NAME_IS_EMPTY,
        LOBBY_IS_NULL,
        WAIT_ROOM_IS_NULL,
        MAX_PLAYERS_IS_LESSER_THAN_MIN_PLAYERS,
        MIN_PLAYERS_IS_LESS_THAN_1,
        AREA_P1_IS_NULL,
        AREA_P2_IS_NULL,
        START_POINT_IS_NULL,
        INVOKE_COMMAND_IS_NULL,
        START_POINT_NOT_IN_AREA,
        PARSE_FAIL,
        PENDING,
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
            waitRoomTimer = waitRoomTimeLimit;
            return;
        }
        if (waitRoomTimer-- <= 0) {
            start(false);
        }
    }

    public void checkFungeon() {
        if (getStatus() != Status.PLAYING) {
            fungeonTimer = fungeonTimeLimit;
            winTimer = Config.NO_ENEMIES_WIN_TIMER.getInt();
            return;
        }
        int enemiesAlive = 0;
        for (final Entity mob: startPoint.getWorld().getEntities()) {
            if ((mob instanceof Monster || mob instanceof Animals)
                    && Fungeon.isInRegion(areaP1, areaP2, mob.getLocation())) {
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

    public void forceReset() {
        for (final Player player: new ArrayList<Player>(players)) {
            player.teleport(lobby);
        }
        players.clear();
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

    public Error kickAll() {
        if (checkValidity() != Validity.VALID) {
            return Error.FUNGEON_NOT_VALID;
        }
        if (isPlaying) {
            return Error.FUNGEON_HAS_ALREADY_STARTED;
        }
        for (final Player player: new ArrayList<Player>(players)) {
            kickPlayer(player);
        }
        return Error.SUCCESS;
    }

    public Error start(boolean force) {
        if (checkValidity() != Validity.VALID) {
            return Error.FUNGEON_NOT_VALID;
        }
        if (getStatus() == Status.PLAYING) {
            return Error.FUNGEON_HAS_ALREADY_STARTED;
        }
        if (!force) {
            if (players.size() >= minPlayers) {
                return Error.NOT_ENOUGH_PLAYERS;
            }
        }
        for (final Player player: players) {
            player.teleport(startPoint);
        }
        isPlaying = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                Main.getInstance().getServer()
                        .dispatchCommand(Main.getInstance().getServer().getConsoleSender(), invokeCommand);
            }
        }.runTaskLater(Main.getInstance(), Config.INVOKE_COMMAND_DELAY_SECONDS.getLong() * 20L);
        return Error.SUCCESS;
    }

    public Error forceEnd() {
        return lose();
    }

    private Error win() {
        if (getStatus() != Status.PLAYING) {
            return Error.FUNGEON_HAS_NOT_STARTED;
        }
        isPlaying = false;
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
                } catch (final SQLException e) {
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
        isPlaying = false;
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
        if (!enabled) {
            return Validity.NOT_ENABLED;
        }
        if (name.equalsIgnoreCase("")) {
            return Validity.FUNGEON_NAME_IS_EMPTY;
        }
        if (invokeCommand == null || invokeCommand.equalsIgnoreCase("")) {
            return Validity.INVOKE_COMMAND_IS_NULL;
        }
        if (fungeonTimeLimit == null || fungeonTimeLimit <= 60 || waitRoomTimeLimit == null || waitRoomTimeLimit <= 0) {
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
            return Validity.MAX_PLAYERS_IS_LESSER_THAN_MIN_PLAYERS;
        }
        if (minPlayers < 1) {
            return Validity.MIN_PLAYERS_IS_LESS_THAN_1;
        }
        if (!Fungeon.isInRegion(areaP1, areaP2, startPoint)) {
            return Validity.START_POINT_NOT_IN_AREA;
        }
        return Validity.VALID;
    }

    public void save() throws SQLException {
        final PreparedStatement upStmt = Database.getConn().prepareStatement(
                "UPDATE `fungeons` SET `fungeon_name` = ?, `time_limit` = ?, `min_players` = ?,"
                        + "`max_players` = ?, `wait_time` = ?, `run_command` = ? WHERE `row_id` = ?");
        upStmt.setString(1, name);
        upStmt.setInt(2, fungeonTimeLimit);
        upStmt.setInt(3, minPlayers);
        upStmt.setInt(4, maxPlayers);
        upStmt.setInt(5, waitRoomTimeLimit);
        upStmt.setString(6, invokeCommand);
        upStmt.setInt(7, id);
        upStmt.execute();
        upStmt.close();
    }

    public void saveLocations() throws SQLException {
        Integer lobbyID = LocationUtils.getLocationID(lobby);
        if (lobbyID == null) {
            lobbyID = LocationUtils.saveLocation(lobby);
        }
        Integer waitRoomID = LocationUtils.getLocationID(waitRoom);
        if (waitRoomID == null) {
            waitRoomID = LocationUtils.saveLocation(waitRoom);
        }
        Integer areaP1ID = LocationUtils.getLocationID(areaP1);
        if (areaP1ID == null) {
            areaP1ID = LocationUtils.saveLocation(areaP1);
        }
        Integer areaP2ID = LocationUtils.getLocationID(areaP2);
        if (areaP2ID == null) {
            areaP2ID = LocationUtils.saveLocation(areaP2);
        }
        Integer startPtID = LocationUtils.getLocationID(startPoint);
        if (startPtID == null) {
            startPtID = LocationUtils.saveLocation(startPoint);
        }
        final PreparedStatement upStmt = Database.getConn().prepareStatement(
                "UPDATE `fungeons` SET `lobby_loc_id` = ?, `wait_rm_loc_id` = ?,"
                        + "`area_p1_loc_id` = ?, `area_p2_loc_id` = ?, `start_pt_loc_id` = ? WHERE `row_id` = ?");
        upStmt.setInt(1, lobbyID);
        upStmt.setInt(2, waitRoomID);
        upStmt.setInt(3, areaP1ID);
        upStmt.setInt(4, areaP2ID);
        upStmt.setInt(5, startPtID);
        upStmt.setInt(6, id);
        upStmt.execute();
        upStmt.close();
    }

    public Validity setWaitRoom(Location location) {
        waitRoom = location;
        return Validity.VALID;
    }

    public Validity setLobby(Location location) {
        lobby = location;
        return Validity.VALID;
    }

    public Validity setAreaStartPoint(Location p1, Location p2, Location startPt) {
        if (!Fungeon.isInRegion(p1, p2, startPt)) {
            return Validity.START_POINT_NOT_IN_AREA;
        }
        areaP1 = p1;
        areaP2 = p2;
        startPoint = startPt;
        return Validity.VALID;
    }

    public Validity setMinMaxPlayers(Integer minPlayers, Integer maxPlayers) {
        if (minPlayers < 1) {
            return Validity.MIN_PLAYERS_IS_LESS_THAN_1;
        }
        if (maxPlayers < minPlayers) {
            return Validity.MAX_PLAYERS_IS_LESSER_THAN_MIN_PLAYERS;
        }
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        return Validity.VALID;
    }

    public String getInvokeCommand() {
        return invokeCommand;
    }

    public Validity setInvokeCommand(String command) {
        if (command.equalsIgnoreCase("")) {
            return Validity.INVOKE_COMMAND_IS_NULL;
        }
        invokeCommand = command;
        return Validity.VALID;
    }

    public int getWaitRoomTimeLimit() {
        return waitRoomTimeLimit;
    }

    public Validity setWaitRoomTimeLimit(int sec) {
        if (sec <= 0) {
            return Validity.DEFAULT_VALUE_FAIL;
        }
        waitRoomTimeLimit = sec;
        return Validity.VALID;
    }

    public int getFungeonTimeLimit() {
        return fungeonTimeLimit;
    }

    public Validity setFungeonTimeLimit(int sec) {
        if (sec <= 60) {
            return Validity.DEFAULT_VALUE_FAIL;
        }
        fungeonTimeLimit = sec;
        return Validity.VALID;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Validity setEnabled(boolean enabled) {
        this.enabled = enabled;
        return Validity.VALID;
    }

    public String getName() {
        return name;
    }

    public Validity setName(String name) {
        if (name.equalsIgnoreCase("")) {
            return Validity.FUNGEON_NAME_IS_EMPTY;
        }
        this.name = name;
        return Validity.VALID;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    @Override
    public String toString() {
        return MessageFormat.format(Lang.FUNGEON_TO_STRING_FORMAT.toString(), id, name);
    }

    public static HashMap<Integer, Fungeon> getValidFungeons() {
        return Fungeon.validFungeons;
    }

    public static HashMap<Integer, Fungeon> getAllFungeons() {
        return Fungeon.allFungeons;
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

    public static void loadAllFungeons() throws SQLException {
        final PreparedStatement selStmt = Database.getConn().prepareStatement("SELECT row_id FROM `fungeons`");
        final ResultSet result = selStmt.executeQuery();
        while (result.next()) {
            new Fungeon(result.getInt("row_id"));
        }
        result.close();
        selStmt.close();
    }

}
