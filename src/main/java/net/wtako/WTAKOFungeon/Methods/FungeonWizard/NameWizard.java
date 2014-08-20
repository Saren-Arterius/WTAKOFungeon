package net.wtako.WTAKOFungeon.Methods.FungeonWizard;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Validity;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.entity.Player;

public class NameWizard extends BaseWizard {

    public NameWizard(Player player, Fungeon fungeon) {
        super(player, fungeon);
        player.sendMessage(MessageFormat.format(Lang.TYPE_SOMETHING_TO_CONTINUE.toString(), "My_fungeon"));
    }

    @Override
    public Validity setValue(Object value) throws SQLException {
        final Validity result = fungeon.setName((String) value);
        if (result == Validity.VALID) {
            fungeon.save();
        }
        return result;
    }

}
