package net.wtako.WTAKOFungeon.Schedulers;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Fungeon;

import org.bukkit.scheduler.BukkitRunnable;

public class FungeonScheduler extends BukkitRunnable {

    private static FungeonScheduler instance;

    public FungeonScheduler() {
        FungeonScheduler.instance = this;
        runTaskTimer(Main.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        for (final Fungeon fungeon: Fungeon.getValidFungeons().values()) {
            fungeon.checkFungeon();
            fungeon.checkWaitingRoom();
        }
    }

    public static FungeonScheduler getInstance() {
        return FungeonScheduler.instance;
    }

}
