package net.wtako.WTAKOFungeon.Schedulers;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.FungeonSign;
import net.wtako.WTAKOFungeon.Utils.Config;

import org.bukkit.scheduler.BukkitRunnable;

public class SignUpdateScheduler extends BukkitRunnable {

    private SignUpdateScheduler instance;

    public SignUpdateScheduler() {
        instance = this;
        this.runTaskTimer(Main.getInstance(), 0L, Config.SIGNS_UPDATE_INTERVAL.getLong());
    }

    @Override
    public void run() {
        for (FungeonSign sign: FungeonSign.getValidFungeonSigns().values()) {
            sign.updateSignText();
        }
    }

    public SignUpdateScheduler getInstance() {
        return instance;
    }

}
