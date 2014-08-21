package net.wtako.WTAKOFungeon.Methods.FungeonWizard;

import java.sql.SQLException;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Validity;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class SignWizard extends BaseWizard {

    public SignWizard(Player player, Fungeon fungeon) {
        super(player, fungeon);
        player.sendMessage(Lang.BREAK_SOME_BLOCKS_TO_CONTINUE.toString());
    }

    @Override
    public Validity setValue(Object value) throws SQLException {
        final Location location = (Location) value;
        final Location oldSignLocation = fungeon.getSignLocation();
        final Validity result = fungeon.setSignLocation(location);
        if (result == Validity.VALID) {
            fungeon.saveLocations();
            if (oldSignLocation != null && (oldSignLocation.getBlock().getState() instanceof Sign)) {
                final Sign sign = (Sign) oldSignLocation.getBlock().getState();
                for (int i = 0; i < 4; i++) {
                    sign.setLine(i, "");
                }
                sign.update();
            }
        }
        return result;
    }

}
