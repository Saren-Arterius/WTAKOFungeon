package net.wtako.WTAKOFungeon.Methods.FungeonWizard;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Validity;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.entity.Player;

public class MinMaxPlayersWizard extends BaseWizard {

    public Integer minPlayers;
    public Integer maxPlayers;

    public MinMaxPlayersWizard(Player player, Fungeon fungeon) {
        super(player, fungeon);
        player.sendMessage(MessageFormat.format(Lang.TYPE_SOMETHING_TO_CONTINUE.toString(), "3 -> ENTER -> 8"));
        sendMessage();
    }

    public void sendMessage() {
        if (minPlayers == null) {
            player.sendMessage("Minimum players?");
            return;
        }
        if (maxPlayers == null) {
            player.sendMessage("Maximum players?");
            return;
        }
    }

    @Override
    public Validity setValue(Object value) throws SQLException {
        try {
            final int val = Integer.parseInt((String) value);
            if (minPlayers == null) {
                if (val < 1) {
                    return Validity.MIN_PLAYERS_IS_LESS_THAN_1;
                }
                minPlayers = val;
                sendMessage();
                return Validity.PENDING;
            }
            if (maxPlayers == null) {
                if (val < minPlayers) {
                    return Validity.MAX_PLAYERS_IS_LESSER_THAN_MIN_PLAYERS;
                }
                maxPlayers = val;
                sendMessage();
            }
            final Validity result = fungeon.setMinMaxPlayers(minPlayers, maxPlayers);
            if (result == Validity.VALID) {
                fungeon.save();
            }
            return result;
        } catch (final NumberFormatException e) {
            return Validity.PARSE_FAIL;
        }

    }

}
