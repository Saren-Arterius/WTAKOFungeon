package net.wtako.WTAKOFungeon.Schedulers;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Utils.Config;

import org.bukkit.scheduler.BukkitRunnable;

public class SignUpdateScheduler extends BukkitRunnable {

    private static SignUpdateScheduler instance;

    public SignUpdateScheduler() {
        SignUpdateScheduler.instance = this;
        runTaskTimer(Main.getInstance(), 0L, Config.SIGNS_UPDATE_INTERVAL.getLong());
    }

    @Override
    public void run() {
        for (final Fungeon fungeon: Fungeon.getValidFungeons().values()) {
            fungeon.updateSign();
        }
    }

    public static SignUpdateScheduler getInstance() {
        return SignUpdateScheduler.instance;
    }

}
