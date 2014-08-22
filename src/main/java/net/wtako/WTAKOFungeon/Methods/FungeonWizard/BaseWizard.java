package net.wtako.WTAKOFungeon.Methods.FungeonWizard;

import java.sql.SQLException;
import java.text.MessageFormat;

import net.wtako.WTAKOFungeon.EventHandlers.FungeonWizardListener;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.Validity;
import net.wtako.WTAKOFungeon.Utils.Lang;

import org.bukkit.entity.Player;

public abstract class BaseWizard {

    protected Fungeon fungeon;
    protected Player  player;

    public BaseWizard(Player player, Fungeon fungeon) {
        this.player = player;
        this.fungeon = fungeon;
        player.sendMessage(MessageFormat.format(Lang.CONFIG_WIZARD.toString(), FungeonWizardListener.FungeonConfig
                .getConfig(getClass()).name().toLowerCase().replace("_", "-")));
    }

    abstract public Validity setValue(Object value) throws SQLException;
}
