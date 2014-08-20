package net.wtako.WTAKOFungeon.Methods.FungeonWizard;

import java.sql.SQLException;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Validity;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class WaitRoomWizard extends BaseWizard {

    public WaitRoomWizard(Player player, Fungeon fungeon) {
        super(player, fungeon);
        player.sendMessage(Lang.BREAK_SOME_BLOCKS_TO_CONTINUE.toString());
    }

    @Override
    public Validity setValue(Object value) throws SQLException {
        final Location location = (Location) value;
        final Validity result = fungeon.setWaitRoom(location);
        if (result == Validity.VALID) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        fungeon.saveLocations();
                    } catch (final SQLException e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(Main.getInstance());
        }
        return result;
    }

}
