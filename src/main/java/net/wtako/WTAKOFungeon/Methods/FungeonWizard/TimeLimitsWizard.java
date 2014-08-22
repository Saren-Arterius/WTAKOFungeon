package net.wtako.WTAKOFungeon.Methods.FungeonWizard;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Validity;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.entity.Player;

public class TimeLimitsWizard extends BaseWizard {

    public Integer fungeonTimeLimit;
    public Integer waitRoomTimeLimit;

    public TimeLimitsWizard(Player player, Fungeon fungeon) {
        super(player, fungeon);
        player.sendMessage(MessageFormat.format(Lang.TYPE_SOMETHING_TO_CONTINUE.toString(), "900 -> ENTER -> 15"));
        sendMessage();
    }

    public void sendMessage() {
        if (fungeonTimeLimit == null) {
            player.sendMessage("Fungeon time limit?");
            return;
        }
        if (waitRoomTimeLimit == null) {
            player.sendMessage("Wait room time limit?");
            return;
        }
    }

    @Override
    public Validity setValue(Object value) throws SQLException {
        try {
            final int val = Integer.parseInt((String) value);
            if (fungeonTimeLimit == null) {
                fungeonTimeLimit = val;
                sendMessage();
                return Validity.PENDING;
            }
            if (waitRoomTimeLimit == null) {
                waitRoomTimeLimit = val;
            }
            Validity result = fungeon.setFungeonTimeLimit(fungeonTimeLimit);
            if (result != Validity.VALID) {
                fungeonTimeLimit = null;
                waitRoomTimeLimit = null;
                sendMessage();
                return result;
            }
            result = fungeon.setWaitRoomTimeLimit(waitRoomTimeLimit);
            if (result != Validity.VALID) {
                fungeonTimeLimit = null;
                waitRoomTimeLimit = null;
                sendMessage();
                return result;
            }
            fungeon.save();
            return result;
        } catch (final NumberFormatException e) {
            return Validity.PARSE_FAIL;
        }

    }

}
