package net.wtako.WTAKOFungeon.Methods.SetWizard;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Validity;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.entity.Player;

public class EnabledWizard extends BaseWizard {

    public EnabledWizard(Player player, Fungeon fungeon) {
        super(player, fungeon);
        player.sendMessage(MessageFormat.format(Lang.TYPE_SOMETHING_TO_CONTINUE.toString(), "true/false"));
    }

    @Override
    public Validity setValue(Object value) throws SQLException {
        final String msg = ((String) value);
        boolean enabled = false;
        if (msg.toLowerCase().startsWith("t")) {
            enabled = true;
        } else if (msg.toLowerCase().startsWith("f")) {
            enabled = false;
        } else {
            return Validity.PARSE_FAIL;
        }
        final Validity result = fungeon.setEnabled(enabled);
        if (result == Validity.VALID) {
            if (!enabled && fungeon.kickAll() == Fungeon.Error.FUNGEON_HAS_ALREADY_STARTED) {
                fungeon.forceEnd();
            }
            fungeon.save();
        }
        return result;
    }

}
