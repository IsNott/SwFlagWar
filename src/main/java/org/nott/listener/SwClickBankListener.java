package org.nott.listener;

import com.ghostchu.quickshop.api.event.ShopClickEvent;
import com.ghostchu.quickshop.api.event.ShopControlPanelOpenEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Nott
 * @date 2024-9-18
 */
public class SwClickBankListener implements Listener {

    static final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

    static final Plugin plugin = SwUtil.getPlugin(GlobalFactory.PLUGIN_NAME);

    // listening player click shop event.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerClickEvent(ShopClickEvent e) {
        Player player = e.getClicker();
        Shop shop = e.getShop();
        World world = player.getWorld();
        String name = world.getName();
        List<String> disableWorlds = AbstractExecutor.CONFIG_YML_FILE.getStringList("bank.disable_world");
        if (SwUtil.isNull(shop) || disableWorlds.contains(name)) return;
        if (SwUtil.isNotNull(shop)) {
            Location clickedBlock = shop.getLocation();
            SimpleChunkLocation chunk = new SimpleChunkLocation(name, (int)clickedBlock.getX() >> 4, (int)clickedBlock.getZ() >> 4);
            Land land = chunk.getLand();
            if (SwUtil.isNull(land)) {
                SwUtil.spigotTextMessage(player.spigot(), SwUtil.retMessage(AbstractExecutor.MESSAGE_YML_FILE, "sw_bank.wild_shop"), ChatColor.DARK_RED);
                e.setCancelled(true,"");
                return;
            }
            UUID claimedBy = land.getKingdomId();
            ItemStack item = shop.getItem();
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
            NamespacedKey ecoKey = new NamespacedKey("ecoitems", "item");
            // 判断出售的是否为银行商品
            if (persistentDataContainer.has(ecoKey)) {
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
            }
        }
    }
}
