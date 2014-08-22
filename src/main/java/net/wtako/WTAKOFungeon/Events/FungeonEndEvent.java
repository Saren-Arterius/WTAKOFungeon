package net.wtako.WTAKOFungeon.Events;

import java.util.ArrayList;

import net.wtako.WTAKOFungeon.Methods.Fungeon;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class FungeonEndEvent extends Event {

    private static final HandlerList   handlers = new HandlerList();
    private final Fungeon              fungeon;
    private final ArrayList<ItemStack> prizes;
    private Integer                    cashPrize;

    public FungeonEndEvent(Fungeon fungeon, ArrayList<ItemStack> prizes, Integer cashPrize) {
        this.fungeon = fungeon;
        this.prizes = prizes;
        this.cashPrize = cashPrize;
    }

    public Fungeon getFungeon() {
        return fungeon;
    }

    @Override
    public HandlerList getHandlers() {
        return FungeonEndEvent.handlers;
    }

    public ArrayList<ItemStack> getPrizes() {
        return prizes;
    }

    public int getCashPrize() {
        return cashPrize;
    }

    public void setCashPrize(int cashPrize) {
        this.cashPrize = cashPrize;
    }

}
