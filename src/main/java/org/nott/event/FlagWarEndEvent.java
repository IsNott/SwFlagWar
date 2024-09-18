package org.nott.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.nott.model.War;

/**
 * @author Nott
 * @date 2024-9-18
 */
public class FlagWarEndEvent extends Event implements Cancellable {

    private War war;

    private boolean isCancelled;

    private boolean async = true;

    private boolean isOccupy;

    private String occupiedKingdomName;

    private static final HandlerList HANDLERS = new HandlerList();

    public FlagWarEndEvent() {
        super(true);
    }

    @NotNull
    @Override
    public String getEventName() {
        return super.getEventName();
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }
}
