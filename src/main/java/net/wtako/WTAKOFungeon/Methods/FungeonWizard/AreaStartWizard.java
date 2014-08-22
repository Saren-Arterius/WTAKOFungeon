package net.wtako.WTAKOFungeon.Methods.FungeonWizard;

import java.sql.SQLException;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Validity;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AreaStartWizard extends BaseWizard {

    private Location areaP1;
    private Location areaP2;

    public AreaStartWizard(Player player, Fungeon fungeon) {
        super(player, fungeon);
        player.sendMessage(Lang.BREAK_SOME_BLOCKS_TO_CONTINUE.toString());
        sendMessage();
    }

    public void sendMessage() {
        if (areaP1 == null) {
            player.sendMessage("Area point 1?");
            return;
        }
        if (areaP2 == null) {
            player.sendMessage("Area point 2?");
            return;
        }
        player.sendMessage("Fungeon start point?");
    }

    @Override
    public Validity setValue(Object value) throws SQLException {
        final Location location = (Location) value;
        if (areaP1 == null) {
            areaP1 = location;
            sendMessage();
            return Validity.PENDING;
        }
        if (areaP2 == null) {
            areaP2 = location;
            sendMessage();
            return Validity.PENDING;
        }
        final Validity result = fungeon.setAreaStartPoint(areaP1, areaP2, location);
        if (result == Validity.VALID) {
            fungeon.saveLocations();
        } else {
            areaP1 = null;
            areaP2 = null;
            sendMessage();
        }
        return result;
    }

}
