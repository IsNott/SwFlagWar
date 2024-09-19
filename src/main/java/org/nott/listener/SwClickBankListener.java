package org.nott.listener;

import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.land.Land;
import org.kingdoms.constants.land.location.SimpleChunkLocation;
import org.kingdoms.constants.player.KingdomPlayer;
import org.nott.SwFlagWar;
import org.nott.executor.AbstractExecutor;
import org.nott.global.GlobalFactory;
import org.nott.utils.SwUtil;

import java.util.List;
import java.util.UUID;

/**
 * @author Nott
 * @date 2024-9-18
 */
public class SwClickBankListener implements Listener {

    static final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

    static final Plugin plugin = SwUtil.getPlugin(GlobalFactory.PLUGIN_NAME);

    // listening player click shop event.
    @EventHandler
    public void onPlayerClickEvent(PlayerInteractEvent e){
        Block clickedBlock = e.getClickedBlock();
        World world = e.getPlayer().getWorld();
        String name = world.getName();
        List<String> disableWorlds = AbstractExecutor.CONFIG_YML_FILE.getStringList("bank.disable_world");
        if (SwUtil.isNull(clickedBlock) || disableWorlds.contains(name))return;
        Location location = clickedBlock.getLocation();
        if (SwUtil.isNull(location))return;
        Player player = e.getPlayer();
        Shop shop = SwFlagWar.quickShopApi.getShopManager().getShopIncludeAttached(location);
        if(SwUtil.isNotNull(shop)){
            SimpleChunkLocation chunk = SimpleChunkLocation.of(location);
            Land land = chunk.getLand();
            if(SwUtil.isNull(land))return;
            ItemStack item = shop.getItem();
            ItemMeta itemMeta = item.getItemMeta();
            //TODO 判断出售的是否为银行商品
            if (land.isClaimed()) {
                UUID claimedBy = land.getClaimedBy();
                Kingdom kingdom = Kingdom.getKingdom(claimedBy);
                String kingdomName = kingdom.getName();
                KingdomPlayer kingdomPlayer = KingdomPlayer.getKingdomPlayer(player);
                // If land claim by player
                if (!kingdomPlayer.hasKingdom()) {
                    SwUtil.spigotTextMessage(player.spigot(), String.format(SwUtil.retMessage(AbstractExecutor.MESSAGE_YML_FILE, "sw_bank.not_belong_shop"), kingdomName), ChatColor.DARK_RED);
                    e.setCancelled(true);
                } else if (!kingdomName.equals(kingdomPlayer.getKingdom().getName())) {
                    SwUtil.spigotTextMessage(player.spigot(), String.format(SwUtil.retMessage(AbstractExecutor.MESSAGE_YML_FILE, "sw_bank.not_claim_shop"), kingdomName), ChatColor.DARK_RED);
                    e.setCancelled(true);
                }
            } else {
                SwUtil.spigotTextMessage(player.spigot(), SwUtil.retMessage(AbstractExecutor.MESSAGE_YML_FILE, "sw_bank.wild_shop"), ChatColor.DARK_RED);
                e.setCancelled(true);
            }
        }
    }
}
