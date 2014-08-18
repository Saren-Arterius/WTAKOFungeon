package net.wtako.WTAKOFungeon.Schedulers;

import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Methods.Fungeon;

import org.bukkit.scheduler.BukkitRunnable;

public class FungeonScheduler extends BukkitRunnable {

    private final FungeonScheduler instance;

    public FungeonScheduler() {
        instance = this;
        runTaskTimer(Main.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        for (final Fungeon fungeon: Fungeon.getValidFungeons().values()) {
            fungeon.checkFungeon();
            fungeon.checkWaitingRoom();
        }
    }

    public FungeonScheduler getInstance() {
        return instance;
    }

}
