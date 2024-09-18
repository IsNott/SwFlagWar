package org.nott.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.player.KingdomPlayer;
import org.nott.SwFlagWar;
import org.nott.global.GlobalFactory;
import org.nott.utils.SwUtil;

/**
 * @author Nott
 * @date 2024-9-18
 */
public class SwClickListener implements Listener {

    static final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

    static final Plugin plugin = SwUtil.getPlugin(GlobalFactory.PLUGIN_NAME);

    // listening player click shop event.
    @EventHandler
    public void onPlayerClickEvent(PlayerInteractEvent e){
        Block clickedBlock = e.getClickedBlock();
        if (SwUtil.isNull(clickedBlock))return;
        Location location = clickedBlock.getLocation();
        if (SwUtil.isNull(location))return;
        Player player = e.getPlayer();
        Shop shop = SwFlagWar.quickShopApi.getShopManager().getShopIncludeAttached(location);
        if(SwUtil.isNotNull(shop)){
            KingdomPlayer kingdomPlayer = KingdomPlayer.getKingdomPlayer(player);
            //TODO If land claim by player
            if(kingdomPlayer.hasKingdom()){

            }
        }
    }
}
