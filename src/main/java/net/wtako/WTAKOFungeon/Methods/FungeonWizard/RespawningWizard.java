package net.wtako.WTAKOFungeon.Methods.FungeonWizard;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Validity;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.entity.Player;

public class RespawningWizard extends BaseWizard {

    public RespawningWizard(Player player, Fungeon fungeon) {
        super(player, fungeon);
        player.sendMessage(MessageFormat.format(Lang.TYPE_SOMETHING_TO_CONTINUE.toString(), "true/false"));
    }

    @Override
    public Validity setValue(Object value) throws SQLException {
        final String msg = ((String) value);
        boolean respawning = false;
        if (msg.toLowerCase().startsWith("t")) {
            respawning = true;
        } else if (msg.toLowerCase().startsWith("f")) {
            respawning = false;
        } else {
            return Validity.PARSE_FAIL;
        }
        final Validity result = fungeon.setRespawning(respawning);
        if (result == Validity.VALID) {
            fungeon.save();
        }
        return result;
    }

}
