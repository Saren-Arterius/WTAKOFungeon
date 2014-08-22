package net.wtako.WTAKOFungeon.Events;

import java.util.ArrayList;

import net.wtako.WTAKOFungeon.Methods.Fungeon;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class FungeonStartEvent extends Event implements Cancellable {

    private static final HandlerList   handlers = new HandlerList();
    private boolean                    cancelled;
    private final Fungeon              fungeon;
    private final ArrayList<ItemStack> costs;
    private Integer                    cashCost;

    public FungeonStartEvent(Fungeon fungeon, ArrayList<ItemStack> costs, Integer cashCost) {
        this.fungeon = fungeon;
        this.costs = costs;
        setCashCost(cashCost);
    }

    public Integer getCashCost() {
        return cashCost;
    }

    public ArrayList<ItemStack> getCosts() {
        return costs;
    }

    public Fungeon getFungeon() {
        return fungeon;
    }

    @Override
    public HandlerList getHandlers() {
        return FungeonStartEvent.handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setCashCost(Integer cashCost) {
        this.cashCost = cashCost;
    }

}
