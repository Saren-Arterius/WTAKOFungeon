package net.wtako.WTAKOFungeon.Methods;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import me.confuser.barapi.BarAPI;
import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Events.FungeonEndEvent;
import net.wtako.WTAKOFungeon.Events.FungeonStartEvent;
import net.wtako.WTAKOFungeon.Events.PlayerJoinFungeonEvent;
import net.wtako.WTAKOFungeon.Events.PlayerLeaveFungeonEvent;
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
    private final HashMap<Player, Long>      ooaPlayers             = new HashMap<Player, Long>();
    private final HashMap<Player, Integer>   kickTimes              = new HashMap<Player, Integer>();
    private final HashMap<Player, Long>      lastKicked             = new HashMap<Player, Long>();
    private boolean                          isPlaying              = false;
    private Integer                          id;
    private boolean                          enabled                = true;
    private boolean                          respawning             = false;
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
    private ArrayList<ItemStack>             itemCosts;
    private Integer                          cashCost;

    public Fungeon(String fungeonName) throws SQLException {
        enabled = true;
        name = fungeonName;
        fungeonTimeLimit = Config.DEFAULT_FUNGEON_TIME_LIMIT_SECONDS.getInt();
        minPlayers = Config.DEFAULT_MIN_PLAYERS.getInt();
        maxPlayers = Config.DEFAULT_MAX_PLAYERS.getInt();
        waitRoomTimeLimit = Config.DEFAULT_WAITING_ROOM_TIME.getInt();
        final PreparedStatement insStmt = Database.getConn().prepareStatement(
                "INSERT INTO `fungeons` (`enabled`, `fungeon_name`, `time_limit`, `min_players`, `max_players`, "
                        + "`wait_time`, `respawning`) VALUES (?, ?, ?, ?, ?, ?, ?)");
        insStmt.setInt(1, 1);
        insStmt.setString(2, fungeonName);
        insStmt.setInt(3, fungeonTimeLimit);
        insStmt.setInt(4, minPlayers);
        insStmt.setInt(5, maxPlayers);
        insStmt.setInt(6, waitRoomTimeLimit);
        insStmt.setInt(7, 0);
        insStmt.execute();
        insStmt.close();
        id = insStmt.getGeneratedKeys().getInt(1);
        updateCosts();
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
        respawning = result.getInt("respawning") == 0 ? false : true;
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
        updateCosts();
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
        START_POINT_NOT_IN_FUNGEON_AREA,
        LOCATION_IN_FUNGEON_AREA,
        COSTS_NOT_READY,
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
        CANNOT_AFFORD,
        CANNOT_KICK_ADMIN,
        EVENT_CANCELLED,
        KICKED_TOO_MANY_TIMES,
        JUST_BEEN_KICKED,
        SUCCESS,
    }

    public enum Status {
        FUNGEON_NOT_VALID,
        IDLE,
        WAIT_COUNTDOWN,
        PLAYING,
    }

    public enum LeaveCause {
        COMMAND,
        DEATH,
        DISCONNECT,
        OUT_OF_AREA,
        PLAYER_KICK,
        ITEM_FAIL,
        SYSTEM_KICK,
        GAME_END,
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

    public void updateCosts() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    itemCosts = Cost.getItemCosts(id);
                    cashCost = Cost.getCashCost(id);
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Main.getInstance());
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

    public void checkPlayerLocations() {
        final Status status = getStatus();
        if (status == Status.FUNGEON_NOT_VALID) {
            return;
        }
        if (status != Status.PLAYING) {
            for (final Player player: Main.getInstance().getServer().getOnlinePlayers()) {
                if (player.hasPermission(Main.artifactId + ".admin")) {
                    continue;
                }
                if (LocationUtils.isInRegion(areaP1, areaP2, player.getLocation())) {
                    player.teleport(lobby);
                }
            }
        } else {
            for (final Player player: Main.getInstance().getServer().getOnlinePlayers()) {
                if (player.hasPermission(Main.artifactId + ".admin")) {
                    continue;
                }
                if (!joinedPlayers.contains(player) && LocationUtils.isInRegion(areaP1, areaP2, player.getLocation())) {
                    player.teleport(lobby);
                    continue;
                }
                if (joinedPlayers.contains(player) && !LocationUtils.isInRegion(areaP1, areaP2, player.getLocation())) {
                    if (!ooaPlayers.containsKey(player)) {
                        player.sendMessage(MessageFormat.format(Lang.YOU_LEFT_FUNGEON_AREA.toString(),
                                Config.OUT_OF_AREA_KICK_TIMEOUT.getInt()));
                        ooaPlayers.put(player, System.currentTimeMillis());
                    } else if (System.currentTimeMillis() - ooaPlayers.get(player) >= Config.OUT_OF_AREA_KICK_TIMEOUT
                            .getLong() * 1000L) {
                        final PlayerLeaveFungeonEvent event = new PlayerLeaveFungeonEvent(player, null, this,
                                LeaveCause.OUT_OF_AREA);
                        Main.getInstance().getServer().getPluginManager().callEvent(event);
                        kickPlayer(player);
                        ooaPlayers.remove(player);
                    }
                } else if (joinedPlayers.contains(player)
                        && LocationUtils.isInRegion(areaP1, areaP2, player.getLocation())) {
                    ooaPlayers.remove(player);
                }
            }
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
                waveCommands = InvokeCommand.getCommands(id);
                return;
            } catch (final SQLException e) {
                for (final Player player: new ArrayList<Player>(joinedPlayers)) {
                    player.sendMessage(Lang.DB_EXCEPTION.toString());
                }
                forceResetAll();
                e.printStackTrace();
            }
        }
        if (joinedPlayers.size() == 0) {
            lose();
            return;
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
                for (String command: waveCommands.get(currentWave).split("; *")) {
                    if (command.length() == 0 || command.equalsIgnoreCase(" ")) {
                        continue;
                    }
                    final Player randomPlayer = joinedPlayers.get(new Random().nextInt(joinedPlayers.size()));
                    Location spawnLocation = randomPlayer.getLocation();
                    if (!LocationUtils.isInRegion(areaP1, areaP2, spawnLocation)) {
                        spawnLocation = startPoint;
                    }
                    command = MessageFormat.format(command, randomPlayer.getName(), randomPlayer.getWorld().getName(),
                            spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());
                    Main.log.info(MessageFormat.format("Fungeon ({0}) - executing command: /{1}", toString(), command));
                    Main.getInstance().getServer()
                            .dispatchCommand(Main.getInstance().getServer().getConsoleSender(), command);
                }
                currentWave++;
            }
        }
    }

    public void forceResetAll() {
        reset();
        for (final Player player: new ArrayList<Player>(joinedPlayers)) {
            final PlayerLeaveFungeonEvent event = new PlayerLeaveFungeonEvent(player, null, this,
                    LeaveCause.SYSTEM_KICK);
            Main.getInstance().getServer().getPluginManager().callEvent(event);
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
        if ((cashCost > 0 && !Cost.hasAtLeast(player, cashCost)) || !Cost.hasItems(player.getInventory(), itemCosts)) {
            for (final ItemStack stack: itemCosts) {
                if (!(player.getInventory().containsAtLeast(stack, stack.getAmount()))) {
                    player.sendMessage(MessageFormat.format(Lang.YOU_DONT_HAVE.toString(),
                            ItemStackUtils.toHumanReadable(stack)));
                }
            }
            if (!Cost.hasAtLeast(player, cashCost)) {
                player.sendMessage(MessageFormat.format(Lang.YOU_DONT_HAVE_MONEY.toString(), cashCost));
            }
            player.sendMessage(MessageFormat.format(Lang.YOU_CANT_AFFORD_COST.toString(), id, toString()));
            return Error.CANNOT_AFFORD;
        }
        if (joinedPlayers.size() >= maxPlayers) {
            return Error.PLAYER_LIST_IS_FULL;
        }
        if (lastKicked.containsKey(player)
                && System.currentTimeMillis() - lastKicked.get(player) < Config.KICKED_DELAY_SECONDS.getLong() * 1000L) {
            return Error.JUST_BEEN_KICKED;
        }
        final PlayerJoinFungeonEvent event = new PlayerJoinFungeonEvent(player, this);
        Main.getInstance().getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            player.teleport(waitRoom);
            player.sendMessage(MessageFormat.format(Lang.FUNGEON_JOIN.toString(), toString(), minPlayers,
                    MessageFormat.format(Lang.FUNGEON_PLAYERS_FORMAT.toString(), joinedPlayers.size() + 1, maxPlayers)));
            for (final Player joinedPlayer: joinedPlayers) {
                joinedPlayer.sendMessage(MessageFormat.format(Lang.PLAYER_JOINED.toString(), player.getName()));
            }
            joinedPlayers.add(player);
            return Error.SUCCESS;
        }
        return Error.EVENT_CANCELLED;
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
        if (!joinedPlayers.contains(player)) {
            return Error.PLAYER_NOT_IN_LIST;
        }
        joinedPlayers.remove(player);
        player.teleport(lobby);
        player.sendMessage(MessageFormat.format(Lang.FORCE_LEAVE_FUNGEON.toString(), Lang.SYSTEM_WORD.toString()));
        BarAPI.removeBar(player);
        for (final Player joinedPlayer: joinedPlayers) {
            joinedPlayer.sendMessage(MessageFormat.format(Lang.PLAYER_LEFT.toString(), player.getName()));
        }
        return Error.SUCCESS;
    }

    public Error kickPlayer(Player kicker, Player kickee, boolean force) {
        if (checkValidity() != Validity.VALID) {
            return Error.FUNGEON_NOT_VALID;
        }
        if (!force) {
            if (joinedPlayers.get(0) != kicker) {
                return Error.NOT_TEAM_LEADER;
            }
            if (isPlaying) {
                return Error.FUNGEON_HAS_ALREADY_STARTED;
            }
            if (kickee.hasPermission(Main.artifactId + ".admin")) {
                return Error.CANNOT_KICK_ADMIN;
            }
            if (kickTimes.get(kicker) >= Config.MAX_KICKS_PER_PERSON_AND_ROUND.getInt()) {
                return Error.KICKED_TOO_MANY_TIMES;
            }
        }
        if (!joinedPlayers.contains(kickee)) {
            return Error.PLAYER_NOT_IN_LIST;
        }
        if (kickTimes.containsKey(kicker)) {
            kickTimes.put(kicker, kickTimes.get(kicker) + 1);
        } else {
            kickTimes.put(kicker, 1);
        }
        lastKicked.put(kickee, System.currentTimeMillis());
        final PlayerLeaveFungeonEvent event = new PlayerLeaveFungeonEvent(kickee, kicker, this, LeaveCause.PLAYER_KICK);
        Main.getInstance().getServer().getPluginManager().callEvent(event);
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
            final PlayerLeaveFungeonEvent event = new PlayerLeaveFungeonEvent(player, null, this,
                    LeaveCause.SYSTEM_KICK);
            Main.getInstance().getServer().getPluginManager().callEvent(event);
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
        final FungeonStartEvent startEvent = new FungeonStartEvent(this, new ArrayList<ItemStack>(itemCosts), cashCost);
        Main.getInstance().getServer().getPluginManager().callEvent(startEvent);
        if (startEvent.isCancelled()) {
            kickAll();
            return Error.EVENT_CANCELLED;
        }
        if (!force) {
            if (joinedPlayers.size() < minPlayers) {
                return Error.NOT_ENOUGH_PLAYERS;
            }
            for (final Player player: new ArrayList<Player>(joinedPlayers)) {
                if ((startEvent.getCashCost() > 0 && !Cost.hasAtLeast(player, startEvent.getCashCost()))
                        || !Cost.hasItems(player.getInventory(), startEvent.getCosts())) {
                    final PlayerLeaveFungeonEvent event = new PlayerLeaveFungeonEvent(player, null, this,
                            LeaveCause.ITEM_FAIL);
                    Main.getInstance().getServer().getPluginManager().callEvent(event);
                    player.sendMessage(MessageFormat.format(Lang.YOU_CANT_AFFORD_COST.toString(), id, toString()));
                    kickPlayer(player);
                }
            }
            if (joinedPlayers.size() >= minPlayers) {
                for (final Player player: new ArrayList<Player>(joinedPlayers)) {
                    if ((startEvent.getCashCost() > 0 && !Cost.chargeMoney(player, startEvent.getCashCost()))
                            || !Cost.chargeItems(player.getInventory(), startEvent.getCosts())) {
                        final PlayerLeaveFungeonEvent event = new PlayerLeaveFungeonEvent(player, null, this,
                                LeaveCause.ITEM_FAIL);
                        Main.getInstance().getServer().getPluginManager().callEvent(event);
                        player.sendMessage(MessageFormat.format(Lang.YOU_CANT_AFFORD_COST.toString(), id, toString()));
                        kickPlayer(player);
                    } else {
                        player.sendMessage(Lang.FUNGEON_START.toString());
                        player.teleport(startPoint);
                    }
                }
            } else {
                for (final Player player: joinedPlayers) {
                    player.sendMessage(Lang.FUNGEON_PLAYERS_NOT_ENOUGH.toString());
                }
                kickAll();
                return Error.NOT_ENOUGH_PLAYERS;
            }
        }
        isPlaying = true;
        return Error.SUCCESS;
    }

    private Error win() {
        if (getStatus() != Status.PLAYING) {
            return Error.FUNGEON_HAS_NOT_STARTED;
        }
        reset();
        final Fungeon fungeon = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    final ArrayList<ItemStack> itemPrizes = Prize.getItemPrizes(id);
                    final int cashPrize = Prize.getCashPrize(id);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            final FungeonEndEvent winEvent = new FungeonEndEvent(fungeon, itemPrizes, cashPrize);
                            Main.getInstance().getServer().getPluginManager().callEvent(winEvent);
                            for (final Player player: new ArrayList<Player>(joinedPlayers)) {
                                final PlayerLeaveFungeonEvent event = new PlayerLeaveFungeonEvent(player, null,
                                        fungeon, LeaveCause.GAME_END);
                                Main.getInstance().getServer().getPluginManager().callEvent(event);
                                player.sendMessage(Lang.YOU_WIN.toString());
                                playerLeave(player);
                                Fungeon.awardPlayer(player, winEvent.getPrizes());
                                Fungeon.awardPlayer(player, winEvent.getCashPrize());
                            }
                        }
                    }.runTask(Main.getInstance());
                } catch (final SQLException e) {
                    final FungeonEndEvent winEvent = new FungeonEndEvent(fungeon, null, null);
                    Main.getInstance().getServer().getPluginManager().callEvent(winEvent);
                    for (final Player player: new ArrayList<Player>(joinedPlayers)) {
                        final PlayerLeaveFungeonEvent event = new PlayerLeaveFungeonEvent(player, null, fungeon,
                                LeaveCause.SYSTEM_KICK);
                        Main.getInstance().getServer().getPluginManager().callEvent(event);
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
        reset();
        final FungeonEndEvent loseEvent = new FungeonEndEvent(this, null, null);
        Main.getInstance().getServer().getPluginManager().callEvent(loseEvent);
        for (final Player player: new ArrayList<Player>(joinedPlayers)) {
            final PlayerLeaveFungeonEvent event = new PlayerLeaveFungeonEvent(player, null, this, LeaveCause.GAME_END);
            Main.getInstance().getServer().getPluginManager().callEvent(event);
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
        isPlaying = false;
        currentWave = 0;
        ooaPlayers.clear();
    }

    public ArrayList<Entity> getEnemiesLeft() {
        final ArrayList<Entity> enemies = new ArrayList<Entity>();
        if (startPoint == null) {
            return enemies;
        }
        for (final Entity mob: startPoint.getWorld().getEntities()) {
            if ((mob instanceof Monster || mob instanceof Animals)
                    && LocationUtils.isInRegion(areaP1, areaP2, mob.getLocation())) {
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
        if (fungeonTimeLimit == null || fungeonTimeLimit <= 0 || waitRoomTimeLimit == null || waitRoomTimeLimit <= 0) {
            return Validity.DEFAULT_VALUE_FAIL;
        }
        if (fungeonTimeLimit < Config.MIN_FUNGEON_TIME_LIMIT_SECONDS.getInt()) {
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
        if (!LocationUtils.isInRegion(areaP1, areaP2, startPoint)) {
            return Validity.START_POINT_NOT_IN_FUNGEON_AREA;
        }
        if (cashCost == null || itemCosts == null) {
            return Validity.COSTS_NOT_READY;
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
                                    + "`enabled` = ?, `respawning` = ? WHERE `row_id` = ?");
                    upStmt.setString(1, name);
                    upStmt.setInt(2, fungeonTimeLimit);
                    upStmt.setInt(3, minPlayers);
                    upStmt.setInt(4, maxPlayers);
                    upStmt.setInt(5, waitRoomTimeLimit);
                    upStmt.setInt(6, enabled ? 1 : 0);
                    upStmt.setInt(7, respawning ? 1 : 0);
                    upStmt.setInt(8, id);
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
                    final PreparedStatement upStmt = Database.getConn().prepareStatement(
                            "UPDATE `fungeons` SET `lobby_loc_id` = ?, `wait_rm_loc_id` = ?, "
                                    + "`area_p1_loc_id` = ?, `area_p2_loc_id` = ?, `start_pt_loc_id` = ?, "
                                    + "`sign_loc_id` = ? WHERE `row_id` = ?");
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
        if (LocationUtils.isInRegion(areaP1, areaP2, location)) {
            return Validity.LOCATION_IN_FUNGEON_AREA;
        }
        if (!(location.getBlock().getState() instanceof Sign)) {
            return Validity.SIGN_LOCATION_HAS_NO_SIGN;
        }
        signLocation = location;
        return Validity.VALID;
    }

    public Validity setWaitRoom(Location location) {
        if (LocationUtils.isInRegion(areaP1, areaP2, location)) {
            return Validity.LOCATION_IN_FUNGEON_AREA;
        }
        waitRoom = location;
        return Validity.VALID;
    }

    public Validity setLobby(Location location) {
        if (LocationUtils.isInRegion(areaP1, areaP2, location)) {
            return Validity.LOCATION_IN_FUNGEON_AREA;
        }
        lobby = location;
        return Validity.VALID;
    }

    public Validity setAreaStartPoint(Location p1, Location p2, Location startPt) {
        if (!LocationUtils.isInRegion(p1, p2, startPt)) {
            return Validity.START_POINT_NOT_IN_FUNGEON_AREA;
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
        if (sec <= 0) {
            return Validity.DEFAULT_VALUE_FAIL;
        }
        if (sec < Config.MIN_FUNGEON_TIME_LIMIT_SECONDS.getInt()) {
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

    public boolean isRespawning() {
        return respawning;
    }

    public Validity setRespawning(boolean respawning) {
        this.respawning = respawning;
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

    public Location getStartLocation() {
        return startPoint;
    }

    public Validity setName(String name) {
        if (name.equalsIgnoreCase("")) {
            return Validity.FUNGEON_NAME_IS_EMPTY;
        }
        this.name = name;
        return Validity.VALID;
    }

    public Integer getMinPlayers() {
        return minPlayers;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public ArrayList<Player> getJoinedPlayers() {
        return joinedPlayers;
    }

    public Location getAreaP1() {
        return areaP1;
    }

    public Location getAreaP2() {
        return areaP2;
    }

    public Location getWaitRoom() {
        return waitRoom;
    }

    public Location getLobby() {
        return lobby;
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
            player.sendMessage(MessageFormat.format(Lang.YOU_HAVE_BEEN_AWARDED_ITEM.toString(),
                    ItemStackUtils.toHumanReadable(itemStack)));
        }
    }

    private static void awardPlayer(Player player, int cashPrize) {
        if (Main.econ != null) {
            Main.econ.depositPlayer(player, cashPrize);
            player.sendMessage(MessageFormat.format(Lang.YOU_HAVE_BEEN_AWARDED_MONEY.toString(), cashPrize));
        } else {
            player.sendMessage(MessageFormat.format(Lang.ERROR_HOOKING.toString(), "Vault"));
        }
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
