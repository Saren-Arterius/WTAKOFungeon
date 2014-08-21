package net.wtako.WTAKOFungeon.Methods;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import me.confuser.barapi.BarAPI;
import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Utils.Config;
import net.wtako.WTAKOFungeon.Utils.ItemStackUtils;
import net.wtako.WTAKOFungeon.Utils.Lang;
import net.wtako.WTAKOFungeon.Utils.LocationUtils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Fungeon {

    private static HashMap<Integer, Fungeon> allFungeons            = new HashMap<Integer, Fungeon>();
    private final ArrayList<Player>          joinedPlayers          = new ArrayList<Player>();
    private boolean                          isPlaying              = false;
    private Integer                          id;
    private boolean                          enabled                = true;
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
    private Location                         signLocation;
    private Integer                          fungeonTimer;
    private Integer                          waitRoomTimer;
    private Integer                          nextWaveTimer;
    private Integer                          lastJoinedPlayersCount = 0;
    private Integer                          currentWave            = 0;
    private ArrayList<String>                waveCommands;

    public Fungeon(String fungeonName) throws SQLException {
        enabled = true;
        name = fungeonName;
        fungeonTimeLimit = Config.DEFAULT_FUNGEON_TIME_LIMIT_SECONDS.getInt();
        minPlayers = Config.DEFAULT_MIN_PLAYERS.getInt();
        maxPlayers = Config.DEFAULT_MAX_PLAYERS.getInt();
        waitRoomTimeLimit = Config.DEFAULT_WAITING_ROOM_TIME.getInt();
        final PreparedStatement insStmt = Database.getConn().prepareStatement(
                "INSERT INTO `fungeons` (`enabled`, `fungeon_name`, "
                        + "`time_limit`, `min_players`, `max_players`, `wait_time`) VALUES (?, ?, ?, ?, ?, ?)");
        insStmt.setInt(1, 1);
        insStmt.setString(2, fungeonName);
        insStmt.setInt(3, fungeonTimeLimit);
        insStmt.setInt(4, minPlayers);
        insStmt.setInt(5, maxPlayers);
        insStmt.setInt(6, waitRoomTimeLimit);
        insStmt.execute();
        insStmt.close();
        id = insStmt.getGeneratedKeys().getInt(1);
        Fungeon.allFungeons.put(id, this);
    }

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
        signLocation = LocationUtils.getLocation(result.getInt("sign_loc_id"));
        Fungeon.allFungeons.put(fungeonID, this);
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
        SIGN_LOCATION_IS_NULL,
        SIGN_LOCATION_HAS_NO_SIGN,
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
        NOT_TEAM_LEADER,
        SUCCESS
    }

    public enum Status {
        FUNGEON_NOT_VALID,
        IDLE,
        WAIT_COUNTDOWN,
        PLAYING,
    }

    public void updatePlayerBars() {
        String msg = "";
        float value = 100F;
        final Status status = getStatus();
        if (status == Status.IDLE) {
            msg = MessageFormat.format(Lang.BAR_WAITING_ROOM_IDLE_FORMAT.toString(), toString(),
                    MessageFormat.format(Lang.FUNGEON_PLAYERS_FORMAT.toString(), joinedPlayers.size(), minPlayers));
        } else if (status == Status.WAIT_COUNTDOWN) {
            value = (Float.valueOf(waitRoomTimer) / Float.valueOf(waitRoomTimeLimit)) * 100F;
            msg = MessageFormat.format(Lang.BAR_WAITING_ROOM_COUNTDOWN_FORMAT.toString(), toString(),
                    MessageFormat.format(Lang.FUNGEON_PLAYERS_FORMAT.toString(), joinedPlayers.size(), maxPlayers));
        } else if (status == Status.PLAYING) {
            if (currentWave != 0) {
                final int enemiesCount = getEnemiesLeft().size();
                if (enemiesCount > 0) {
                    value = (Float.valueOf(fungeonTimer) / Float.valueOf(fungeonTimeLimit)) * 100F;
                    msg = MessageFormat.format(Lang.BAR_FUNGEON_COUNTDOWN_FORMAT.toString(), toString(), currentWave,
                            getEnemiesLeft().size());
                } else {
                    value = (Float.valueOf(nextWaveTimer) / Float.valueOf(Config.NO_ENEMIES_WAVE_INTERVAL.getInt())) * 100F;
                    msg = MessageFormat.format(Lang.BAR_FUNGEON_WAVE_END_FORMAT.toString(), toString(), currentWave);
                }
            } else {
                value = (Float.valueOf(nextWaveTimer) / Float.valueOf(Config.NO_ENEMIES_WAVE_INTERVAL.getInt())) * 100F;
                msg = MessageFormat.format(Lang.BAR_FUNGEON_FIRST_WAVE_COMING_FORMAT.toString(), toString());
            }
        }
        for (final Player joinedPlayer: joinedPlayers) {
            BarAPI.setMessage(joinedPlayer, msg, value < 0 ? 0 : value);
        }
    }

    public void updateSign() {
        if (signLocation == null) {
            return;
        }
        final BlockState bs = signLocation.getBlock().getState();
        if (!(bs instanceof Sign)) {
            return;
        }
        final Sign sign = (Sign) bs;
        sign.setLine(0, Lang.FUNGEON.toString());
        sign.setLine(1, toString());
        sign.setLine(2, getStatus().name());
        sign.setLine(3, MessageFormat.format(Lang.FUNGEON_PLAYERS_FORMAT.toString(), getJoinedPlayers().size(),
                getMaxPlayers()));
        sign.update();
    }

    public void waitingRoomTick() {
        if (getStatus() != Status.WAIT_COUNTDOWN || joinedPlayers.size() != lastJoinedPlayersCount) {
            waitRoomTimer = waitRoomTimeLimit;
            lastJoinedPlayersCount = joinedPlayers.size();
            return;
        }
        lastJoinedPlayersCount = joinedPlayers.size();
        if (waitRoomTimer-- <= 0) {
            start(false);
        }
    }

    public void fungeonTick() {
        if (getStatus() != Status.PLAYING) {
            nextWaveTimer = Config.NO_ENEMIES_WAVE_INTERVAL.getInt();
            fungeonTimer = fungeonTimeLimit;
            return;
        }
        if (waveCommands == null) {
            try {
                waveCommands = Fungeon.getCommandWaves(id);
                return;
            } catch (final SQLException e) {
                for (final Player player: new ArrayList<Player>(joinedPlayers)) {
                    player.sendMessage(Lang.DB_EXCEPTION.toString());
                }
                forceResetAll();
                e.printStackTrace();
            }
        }
        if (currentWave != 0 && getEnemiesLeft().size() > 0) {
            nextWaveTimer = Config.NO_ENEMIES_WAVE_INTERVAL.getInt();
            if (fungeonTimer-- <= 0) {
                lose();
            }
            return;
        }
        if (nextWaveTimer-- <= 0) {
            if (waveCommands.size() == 0) {
                for (final Player player: joinedPlayers) {
                    player.sendMessage(Lang.FUNGEON_HAS_NO_WAVES.toString());
                }
                lose();
            } else if (currentWave >= waveCommands.size()) {
                win();
            } else {
                for (final Entity mob: getEnemiesLeft()) {
                    mob.remove();
                }
                Main.getInstance()
                        .getServer()
                        .dispatchCommand(Main.getInstance().getServer().getConsoleSender(),
                                waveCommands.get(currentWave));
                currentWave++;
            }
        }
    }

    public void forceResetAll() {
        reset();
        for (final Player player: new ArrayList<Player>(joinedPlayers)) {
            player.teleport(lobby);
            player.sendMessage(MessageFormat.format(Lang.FORCE_LEAVE_FUNGEON.toString(), Lang.SYSTEM_WORD.toString()));
            BarAPI.removeBar(player);
        }
        isPlaying = false;
        joinedPlayers.clear();
    }

    public Error joinPlayer(Player player) {
        final Fungeon joinedFungeon = Fungeon.getJoinedFungeon(player);
        if (joinedFungeon != null) {
            player.sendMessage(MessageFormat.format(Lang.ALREADY_JOINED_FUNGEON.toString(), joinedFungeon.toString()));
            return Error.PLAYER_ALREADY_JOINED;
        }
        if (checkValidity() != Validity.VALID) {
            return Error.FUNGEON_NOT_VALID;
        }
        if (isPlaying) {
            return Error.FUNGEON_HAS_ALREADY_STARTED;
        }
        if (joinedPlayers.contains(player)) {
            return Error.PLAYER_ALREADY_JOINED;
        }
        if (joinedPlayers.size() >= maxPlayers) {
            return Error.PLAYER_LIST_IS_FULL;
        }
        player.teleport(waitRoom);
        player.sendMessage(MessageFormat.format(Lang.FUNGEON_JOIN.toString(), toString(), minPlayers,
                MessageFormat.format(Lang.FUNGEON_PLAYERS_FORMAT.toString(), joinedPlayers.size() + 1, maxPlayers)));
        for (final Player joinedPlayer: joinedPlayers) {
            joinedPlayer.sendMessage(MessageFormat.format(Lang.PLAYER_JOINED.toString(), player.getName()));
        }
        joinedPlayers.add(player);
        return Error.SUCCESS;
    }

    public Error playerLeave(Player player) {
        if (checkValidity() != Validity.VALID) {
            return Error.FUNGEON_NOT_VALID;
        }
        if (!joinedPlayers.contains(player)) {
            return Error.PLAYER_NOT_IN_LIST;
        }
        joinedPlayers.remove(player);
        player.teleport(lobby);
        player.sendMessage(Lang.FUNGEON_LEAVE.toString());
        BarAPI.removeBar(player);
        for (final Player joinedPlayer: joinedPlayers) {
            joinedPlayer.sendMessage(MessageFormat.format(Lang.PLAYER_LEFT.toString(), player.getName()));
        }
        return Error.SUCCESS;
    }

    public Error kickPlayer(Player player) {
        if (checkValidity() != Validity.VALID) {
            return Error.FUNGEON_NOT_VALID;
        }
        if (isPlaying) {
            return Error.FUNGEON_HAS_ALREADY_STARTED;
        }
        if (!joinedPlayers.contains(player)) {
            return Error.PLAYER_NOT_IN_LIST;
        }
        joinedPlayers.remove(player);
        player.teleport(lobby);
        player.sendMessage(MessageFormat.format(Lang.FUNGEON_LEAVE.toString(), Lang.SYSTEM_WORD.toString()));
        BarAPI.removeBar(player);
        for (final Player joinedPlayer: joinedPlayers) {
            joinedPlayer.sendMessage(MessageFormat.format(Lang.PLAYER_LEFT.toString(), player.getName()));
        }
        return Error.SUCCESS;
    }

    public Error kickPlayer(Player kicker, Player kickee) {
        if (checkValidity() != Validity.VALID) {
            return Error.FUNGEON_NOT_VALID;
        }
        if (joinedPlayers.get(0) != kicker) {
            return Error.NOT_TEAM_LEADER;
        }
        if (isPlaying) {
            return Error.FUNGEON_HAS_ALREADY_STARTED;
        }
        if (!joinedPlayers.contains(kickee)) {
            return Error.PLAYER_NOT_IN_LIST;
        }
        joinedPlayers.remove(kickee);
        kickee.teleport(lobby);
        kickee.sendMessage(MessageFormat.format(Lang.FUNGEON_LEAVE.toString(), kicker.getName()));
        BarAPI.removeBar(kickee);
        for (final Player joinedPlayer: joinedPlayers) {
            joinedPlayer.sendMessage(MessageFormat.format(Lang.PLAYER_LEFT.toString(), kickee.getName()));
        }
        return Error.SUCCESS;
    }

    public Error kickAll() {
        if (checkValidity() != Validity.VALID) {
            return Error.FUNGEON_NOT_VALID;
        }
        if (isPlaying) {
            return Error.FUNGEON_HAS_ALREADY_STARTED;
        }
        for (final Player player: new ArrayList<Player>(joinedPlayers)) {
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
        if (!force && joinedPlayers.size() < minPlayers) {
            return Error.NOT_ENOUGH_PLAYERS;
        }
        for (final Player player: joinedPlayers) {
            player.sendMessage(Lang.FUNGEON_START.toString());
            player.teleport(startPoint);
        }
        isPlaying = true;
        return Error.SUCCESS;
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
                    final ArrayList<ItemStack> itemPrizes = Prize.getItemPrizes(id);
                    final int cashPrize = Prize.getCashPrize(id);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            reset();
                            for (final Player player: new ArrayList<Player>(joinedPlayers)) {
                                player.sendMessage(Lang.YOU_WIN.toString());
                                playerLeave(player);
                                Fungeon.awardPlayer(player, itemPrizes);
                                Fungeon.awardPlayer(player, cashPrize);
                            }
                        }
                    }.runTask(Main.getInstance());
                } catch (final SQLException e) {
                    for (final Player player: new ArrayList<Player>(joinedPlayers)) {
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
        reset();
        for (final Player player: new ArrayList<Player>(joinedPlayers)) {
            player.sendMessage(Lang.YOU_LOSE.toString());
            playerLeave(player);
        }
        return Error.SUCCESS;
    }

    private void reset() {
        for (final Entity mob: getEnemiesLeft()) {
            mob.remove();
        }
        waveCommands = null;
        currentWave = 0;
    }

    public ArrayList<Entity> getEnemiesLeft() {
        final ArrayList<Entity> enemies = new ArrayList<Entity>();
        if (startPoint == null) {
            return enemies;
        }
        for (final Entity mob: startPoint.getWorld().getEntities()) {
            if ((mob instanceof Monster || mob instanceof Animals)
                    && Fungeon.isInRegion(areaP1, areaP2, mob.getLocation())) {
                enemies.add(mob);
            }
        }
        return enemies;
    }

    public Status getStatus() {
        if (checkValidity() != Validity.VALID) {
            return Status.FUNGEON_NOT_VALID;
        }
        if (isPlaying) {
            return Status.PLAYING;
        }
        if (joinedPlayers.size() >= minPlayers) {
            return Status.WAIT_COUNTDOWN;
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
        if (signLocation == null) {
            return Validity.SIGN_LOCATION_IS_NULL;
        }
        if (!(signLocation.getBlock().getState() instanceof Sign)) {
            return Validity.SIGN_LOCATION_HAS_NO_SIGN;
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

    public void save() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement upStmt = Database.getConn().prepareStatement(
                            "UPDATE `fungeons` SET `fungeon_name` = ?, `time_limit` = ?, "
                                    + "`min_players` = ?, `max_players` = ?, `wait_time` = ?, "
                                    + "`enabled` = ? WHERE `row_id` = ?");
                    upStmt.setString(1, name);
                    upStmt.setInt(2, fungeonTimeLimit);
                    upStmt.setInt(3, minPlayers);
                    upStmt.setInt(4, maxPlayers);
                    upStmt.setInt(5, waitRoomTimeLimit);
                    upStmt.setInt(6, enabled ? 1 : 0);
                    upStmt.setInt(7, id);
                    upStmt.execute();
                    upStmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

    public void saveLocations() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
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
                    Integer signLocID = LocationUtils.getLocationID(signLocation);
                    if (signLocID == null) {
                        signLocID = LocationUtils.saveLocation(signLocation);
                    }
                    final PreparedStatement upStmt = Database
                            .getConn()
                            .prepareStatement(
                                    "UPDATE `fungeons` SET `lobby_loc_id` = ?, `wait_rm_loc_id` = ?, `area_p1_loc_id` = ?, "
                                            + "`area_p2_loc_id` = ?, `start_pt_loc_id` = ?, `sign_loc_id` = ? WHERE `row_id` = ?");
                    upStmt.setInt(1, lobbyID);
                    upStmt.setInt(2, waitRoomID);
                    upStmt.setInt(3, areaP1ID);
                    upStmt.setInt(4, areaP2ID);
                    upStmt.setInt(5, startPtID);
                    upStmt.setInt(6, signLocID);
                    upStmt.setInt(7, id);
                    upStmt.execute();
                    upStmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

    public Validity setSignLocation(Location location) {
        if (location == null) {
            return Validity.SIGN_LOCATION_IS_NULL;
        }
        if (!(location.getBlock().getState() instanceof Sign)) {
            return Validity.SIGN_LOCATION_HAS_NO_SIGN;
        }
        signLocation = location;
        return Validity.VALID;
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

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getSignLocation() {
        return signLocation;
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

    public ArrayList<Player> getJoinedPlayers() {
        return joinedPlayers;
    }

    @Override
    public String toString() {
        return MessageFormat.format(Lang.FUNGEON_TO_STRING_FORMAT.toString(), id, name);
    }

    public static HashMap<Integer, Fungeon> getValidFungeons() {
        final HashMap<Integer, Fungeon> validFungeons = new HashMap<Integer, Fungeon>();
        for (final Entry<Integer, Fungeon> entry: Fungeon.getAllFungeons().entrySet()) {
            if (entry.getValue().checkValidity() == Validity.VALID) {
                validFungeons.put(entry.getKey(), entry.getValue());
            }
        }
        return validFungeons;
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

    public static ArrayList<String> getCommandWaves(int fungeonID) throws SQLException {
        final ArrayList<String> commands = new ArrayList<String>();
        final PreparedStatement selStmt = Database.getConn().prepareStatement(
                "SELECT * FROM invoke_commands WHERE fungeon_id = ?");
        selStmt.setInt(1, fungeonID);
        final ResultSet result = selStmt.executeQuery();
        while (result.next()) {
            commands.add(result.getString("command"));
        }
        result.close();
        selStmt.close();
        return commands;
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

    public static Fungeon getFungeonFromSignBlock(Block block) {
        final BlockState bs = block.getState();
        if (!(bs instanceof Sign)) {
            return null;
        }
        for (final Fungeon fungeon: Fungeon.getAllFungeons().values()) {
            if (block.getLocation().equals(fungeon.getSignLocation())) {
                return fungeon;
            }
        }
        return null;
    }

    public static Fungeon getJoinedFungeon(Player player) {
        for (final Fungeon fungeon: Fungeon.getAllFungeons().values()) {
            if (fungeon.getJoinedPlayers().contains(player)) {
                return fungeon;
            }
        }
        return null;
    }

}
