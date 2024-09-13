package org.nott.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.nott.event.FlagWarOpenEvent;
import org.nott.executor.FlagWarExecutor;
import org.nott.global.KeyWord;
import org.nott.manager.FlagWarManager;
import org.nott.model.Location;
import org.nott.model.War;
import org.nott.utils.SwUtil;

import java.util.List;

/**
 * @author Nott
 * @date 2024-9-12
 */
public class SwFlagWarListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFlagWarOpenEvent(FlagWarOpenEvent flagWarOpenEvent){
        War war = flagWarOpenEvent.getWar();
        String start = war.getStart();
        String end = war.getEnd();
        List<Location> locations = war.getLocations();

        String tipMsg = SwUtil.retMessage(FlagWarExecutor.MESSAGE_YML_FILE, KeyWord.WAR.WAR_STARTING_TIP);

        locations.forEach(loc ->{
            SwUtil.makeCircle(loc,FlagWarExecutor.CONFIG_YML_FILE.getInt("flagWar.game_radius",10), Material.BEDROCK);

            String formatMsg = String.format(tipMsg, start, loc, end);
            SwUtil.broadcast(
                    formatMsg,
                    ChatColor.GOLD
            );
            FlagWarManager.loadWarGame2Map(war);
        });
    }

    // TODO Check If Player enter opening flag war game zone.
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerWalkEvent(PlayerMoveEvent event){
        Player player = event.getPlayer();
        org.bukkit.Location location = player.getLocation();
    }

}
