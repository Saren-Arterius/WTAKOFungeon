package net.wtako.WTAKOFungeon.EventHandlers;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Validity;
import net.wtako.WTAKOFungeon.Methods.FungeonWizard.AreaStartWizard;
import net.wtako.WTAKOFungeon.Methods.FungeonWizard.BaseWizard;
import net.wtako.WTAKOFungeon.Methods.FungeonWizard.EnabledWizard;
import net.wtako.WTAKOFungeon.Methods.FungeonWizard.LobbyWizard;
import net.wtako.WTAKOFungeon.Methods.FungeonWizard.MinMaxPlayersWizard;
import net.wtako.WTAKOFungeon.Methods.FungeonWizard.NameWizard;
import net.wtako.WTAKOFungeon.Methods.FungeonWizard.SignWizard;
import net.wtako.WTAKOFungeon.Methods.FungeonWizard.TimeLimitsWizard;
import net.wtako.WTAKOFungeon.Methods.FungeonWizard.WaitRoomWizard;
import net.wtako.WTAKOFungeon.Utils.Lang;
import net.wtako.WTAKOFungeon.Utils.LocationUtils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class FungeonWizardListener implements Listener {

    private static HashMap<Player, BaseWizard> inWizards      = new HashMap<Player, BaseWizard>();
    private static HashMap<Player, Fungeon>    inStartWizards = new HashMap<Player, Fungeon>();

    public enum WizardType {
        INPUT,
        BLOCK_BREAK,
    }

    public enum FungeonConfig {
        AREA_START(WizardType.BLOCK_BREAK, AreaStartWizard.class),
        LOBBY(WizardType.BLOCK_BREAK, LobbyWizard.class),
        WAIT_ROOM(WizardType.BLOCK_BREAK, WaitRoomWizard.class),
        SIGN(WizardType.BLOCK_BREAK, SignWizard.class),
        NAME(WizardType.INPUT, NameWizard.class),
        ENABLED(WizardType.INPUT, EnabledWizard.class),
        TIME_LIMITS(WizardType.INPUT, TimeLimitsWizard.class),
        MIN_MAX_PLAYERS(WizardType.INPUT, MinMaxPlayersWizard.class);

        private final Class<?>   targetClass;
        private final WizardType type;

        private FungeonConfig(WizardType type, Class<?> targetClass) {
            this.type = type;
            this.targetClass = targetClass;
        }

        public WizardType getWizardType() {
            return type;
        }

        public Class<?> getTargetClass() {
            return targetClass;
        }

        public static FungeonConfig getConfig(Class<?> targetClass) {
            for (final FungeonConfig config: FungeonConfig.values()) {
                if (config.getTargetClass() == targetClass) {
                    return config;
                }
            }
            return null;
        }
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) throws SQLException {
        if (!FungeonWizardListener.inWizards.containsKey(event.getPlayer())) {
            return;
        }
        final BaseWizard wizard = FungeonWizardListener.inWizards.get(event.getPlayer());
        if (FungeonConfig.getConfig(wizard.getClass()).getWizardType() != WizardType.BLOCK_BREAK) {
            return;
        }
        event.setCancelled(true);
        final Validity result = wizard.setValue(event.getBlock().getLocation());
        if (result == Validity.VALID) {
            event.getPlayer().sendMessage(
                    MessageFormat.format(Lang.CONFIG_SET.toString(), FungeonConfig.getConfig(wizard.getClass()),
                            LocationUtils.toHumanReadable(event.getBlock().getLocation())));
            FungeonWizardListener.inWizards.remove(event.getPlayer());
        } else if (result != Validity.PENDING) {
            event.getPlayer().sendMessage(
                    MessageFormat.format(Lang.CONFIG_SET_FAIL.toString(), FungeonConfig.getConfig(wizard.getClass()),
                            LocationUtils.toHumanReadable(event.getBlock().getLocation()), result.name()));
        }

    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) throws SQLException {
        if ((FungeonWizardListener.inStartWizards.containsKey(event.getPlayer()) || FungeonWizardListener.inWizards
                .containsKey(event.getPlayer())) && event.getMessage().equalsIgnoreCase("exit")) {
            event.setCancelled(true);
            FungeonWizardListener.inStartWizards.remove(event.getPlayer());
            FungeonWizardListener.inWizards.remove(event.getPlayer());
            event.getPlayer().sendMessage(Lang.EXIT_WIZARD.toString());
        }
        if (FungeonWizardListener.inStartWizards.containsKey(event.getPlayer())) {
            event.setCancelled(true);
            try {
                final FungeonConfig targetConfig = FungeonConfig.valueOf(event.getMessage().toUpperCase());
                FungeonWizardListener.inWizards.put(
                        event.getPlayer(),
                        (BaseWizard) targetConfig
                                .getTargetClass()
                                .getDeclaredConstructor(Player.class, Fungeon.class)
                                .newInstance(event.getPlayer(),
                                        FungeonWizardListener.inStartWizards.get(event.getPlayer())));
                FungeonWizardListener.inStartWizards.remove(event.getPlayer());
            } catch (final IllegalArgumentException e) {
                event.getPlayer().sendMessage(
                        MessageFormat.format(Lang.NO_SUCH_A_CONFIG.toString(), event.getMessage().toUpperCase()));
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | InvocationTargetException e) {
                e.printStackTrace();
            }
            return;
        }
        if (FungeonWizardListener.inWizards.containsKey(event.getPlayer())) {
            final BaseWizard wizard = FungeonWizardListener.inWizards.get(event.getPlayer());
            if (FungeonConfig.getConfig(wizard.getClass()).getWizardType() != WizardType.INPUT) {
                return;
            }
            event.setCancelled(true);
            final Validity result = wizard.setValue(event.getMessage());
            if (result == Validity.VALID) {
                event.getPlayer().sendMessage(
                        MessageFormat.format(Lang.CONFIG_SET.toString(), FungeonConfig.getConfig(wizard.getClass()),
                                event.getMessage()));
                FungeonWizardListener.inWizards.remove(event.getPlayer());
            } else if (result == Validity.PENDING) {
                event.getPlayer().sendMessage(
                        MessageFormat.format(Lang.CONFIG_SET_PENDING.toString(),
                                FungeonConfig.getConfig(wizard.getClass()), event.getMessage()));
            } else {
                event.getPlayer().sendMessage(
                        MessageFormat.format(Lang.CONFIG_SET_FAIL.toString(),
                                FungeonConfig.getConfig(wizard.getClass()), event.getMessage(), result.name()));
            }
        }

    }

    public static boolean goToWizard(Player player, Fungeon fungeon) {
        if (FungeonWizardListener.inStartWizards.containsKey(player)
                || FungeonWizardListener.inWizards.containsKey(player)) {
            player.sendMessage(Lang.ALREADY_IN_WIZARD.toString());
            return false;
        }
        FungeonWizardListener.inStartWizards.put(player, fungeon);
        player.sendMessage(MessageFormat.format(Lang.WELCOME_TO_WIZARD.toString(), fungeon.toString()));
        String msg = "";
        for (int i = 0; i < FungeonConfig.values().length; i++) {
            msg += FungeonConfig.values()[i].name();
            if (i < FungeonConfig.values().length - 1) {
                msg += ", ";
            }
        }
        player.sendMessage(MessageFormat.format(Lang.AVAILABLE_CONFIGS.toString(), msg));
        return true;
    }

    public static HashMap<Player, BaseWizard> getInWizards() {
        return FungeonWizardListener.inWizards;
    }

}
