package org.nott.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.nott.global.GlobalFactory;
import org.nott.utils.SwUtil;

/**
 * @author Nott
 * @date 2024-9-18
 */
public class SwClickListener implements Listener {

    static final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

    static final Plugin plugin = SwUtil.getPlugin(GlobalFactory.PLUGIN_NAME);

    //TODO listening player click shop event.
}
