package net.wtako.WTAKOFungeon.Schedulers;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.FungeonSign;
import net.wtako.WTAKOFungeon.Utils.Config;

import org.bukkit.scheduler.BukkitRunnable;

public class SignUpdateScheduler extends BukkitRunnable {

    private final SignUpdateScheduler instance;

    public SignUpdateScheduler() {
        instance = this;
        runTaskTimer(Main.getInstance(), 0L, Config.SIGNS_UPDATE_INTERVAL.getLong());
    }

    @Override
    public void run() {
        for (final FungeonSign sign: FungeonSign.getValidFungeonSigns().values()) {
            sign.updateSignText();
        }
    }

    public SignUpdateScheduler getInstance() {
        return instance;
    }

}
