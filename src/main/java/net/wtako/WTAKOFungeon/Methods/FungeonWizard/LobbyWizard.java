package net.wtako.WTAKOFungeon.Methods.FungeonWizard;

import java.sql.SQLException;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Validity;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LobbyWizard extends BaseWizard {

    public LobbyWizard(Player player, Fungeon fungeon) {
        super(player, fungeon);
        player.sendMessage(Lang.BREAK_SOME_BLOCKS_TO_CONTINUE.toString());
    }

    @Override
    public Validity setValue(Object value) throws SQLException {
        final Location location = (Location) value;
        final Validity result = fungeon.setLobby(location);
        if (result == Validity.VALID) {
            fungeon.saveLocations();
        }
        return result;
    }

}
