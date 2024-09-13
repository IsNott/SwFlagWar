package org.nott.event;

import lombok.Data;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.nott.model.War;


/**
 * @author Nott
 * @date 2024-9-13
 */
@Data
public class FlagWarOpenEvent extends Event implements Cancellable {

    private War war;

    private boolean isCancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    public FlagWarOpenEvent(War war){
        this.war = war;
    }


    @NotNull
    @Override
    public HandlerList getHandlers() {
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
