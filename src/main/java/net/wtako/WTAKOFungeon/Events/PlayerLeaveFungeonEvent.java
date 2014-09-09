package net.wtako.WTAKOFungeon.Events;

import net.wtako.WTAKOFungeon.Methods.Fungeon;
import net.wtako.WTAKOFungeon.Methods.Fungeon.LeaveCause;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLeaveFungeonEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player             player;
    private final Player             kicker;
    private final Fungeon            fungeon;
    private final LeaveCause         cause;

    public PlayerLeaveFungeonEvent(Player player, Player kicker, Fungeon fungeon, LeaveCause cause) {
        this.player = player;
        this.kicker = kicker;
        this.fungeon = fungeon;
        this.cause = cause;
    }

    public LeaveCause getCause() {
        return cause;
    }

    public Fungeon getFungeon() {
        return fungeon;
    }

    public Player getKicker() {
        return kicker;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return PlayerLeaveFungeonEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return PlayerLeaveFungeonEvent.handlers;
    }

}
