package net.wtako.WTAKOFungeon.Methods;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;

import net.wtako.WTAKOFungeon.Utils.Lang;
import net.wtako.WTAKOFungeon.Utils.LocationUtils;

import org.bukkit.Location;
import org.bukkit.block.Sign;

public class FungeonSign {

    private static HashMap<Location, FungeonSign> validFungeonSigns = new HashMap<Location, FungeonSign>();
    private Fungeon                               targetFungeon;
    private Location                              location;

    public FungeonSign(int signID) throws SQLException {
        final PreparedStatement selStmt = Database.getConn().prepareStatement(
                "SELECT * FROM fungeon_signs WHERE row_id = ?");
        selStmt.setInt(1, signID);
        final ResultSet result = selStmt.executeQuery();
        if (!result.next()) {
            result.close();
            selStmt.close();
            return;
        }
        targetFungeon = Fungeon.getValidFungeons().get(result.getInt("fungeon_id"));
        location = LocationUtils.getLocation(result.getInt("loc_id"));
        if (location != null) {
            FungeonSign.validFungeonSigns.put(location, this);
        }
    }

    public Sign getSign() {
        return (Sign) location.getBlock().getState();
    }

    public void updateSignText() {
        final Sign sign = getSign();
        sign.setLine(0, Lang.FUNGEON.toString());
        sign.setLine(1, targetFungeon.toString());
        sign.setLine(2, targetFungeon.getStatus().name());
        sign.setLine(3, MessageFormat.format(Lang.FUNGEON_PLAYERS_FORMAT.toString(), targetFungeon.getMaxPlayers(),
                targetFungeon.getPlayers().size()));
    }

    public static HashMap<Location, FungeonSign> getValidFungeonSigns() {
        return FungeonSign.validFungeonSigns;
    }
}
