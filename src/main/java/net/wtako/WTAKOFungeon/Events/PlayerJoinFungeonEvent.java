package net.wtako.WTAKOFungeon.Events;

import net.wtako.WTAKOFungeon.Methods.Fungeon;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerJoinFungeonEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean                  cancelled;
    private final Player             player;
    private final Fungeon            fungeon;

    public PlayerJoinFungeonEvent(Player player, Fungeon fungeon) {
        this.player = player;
        this.fungeon = fungeon;
    }

    public Player getPlayer() {
        return player;
    }

    public Fungeon getFungeon() {
        return fungeon;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return PlayerJoinFungeonEvent.handlers;
    }

}
