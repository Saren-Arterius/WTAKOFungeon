package net.wtako.WTAKOFungeon.Methods.SetWizard;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Validity;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.entity.Player;

public class TimeLimitsWizard extends BaseWizard {

    public Integer fungeonTimeLimit;
    public Integer waitTimeLimit;

    public TimeLimitsWizard(Player player, Fungeon fungeon) {
        super(player, fungeon);
        player.sendMessage(MessageFormat.format(Lang.TYPE_SOMETHING_TO_CONTINUE.toString(), "900 -> ENTER -> 15"));
        sendMessage();
    }

    @Override
    public Validity setValue(Object value) throws SQLException {
        try {
            Integer.parseInt((String) value);
            sendMessage();
            return null;
        } catch (final NumberFormatException e) {
            return Validity.PARSE_FAIL;
        }

    }

    public void sendMessage() {
        if (fungeonTimeLimit == null) {
            player.sendMessage("Fungeon time limit?");
            return;
        }
        if (waitTimeLimit == null) {
            player.sendMessage("Wait room time limit?");
            return;
        }
    }

}
