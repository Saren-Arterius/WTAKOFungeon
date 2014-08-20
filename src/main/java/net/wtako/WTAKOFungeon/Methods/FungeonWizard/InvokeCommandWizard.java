package net.wtako.WTAKOFungeon.Methods.FungeonWizard;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Validity;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.entity.Player;

public class InvokeCommandWizard extends BaseWizard {

    public InvokeCommandWizard(Player player, Fungeon fungeon) {
        super(player, fungeon);
        player.sendMessage(MessageFormat.format(Lang.TYPE_SOMETHING_TO_CONTINUE.toString(),
                "mm mobs spawn danger_boss 1 rpg_world,1000,70,-1000"));
    }

    @Override
    public Validity setValue(Object value) throws SQLException {
        final Validity result = fungeon.setInvokeCommand((String) value);
        if (result == Validity.VALID) {
            fungeon.save();
        }
        return result;
    }

}
