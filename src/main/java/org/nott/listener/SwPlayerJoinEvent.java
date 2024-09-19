package org.nott.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.player.KingdomPlayer;
import org.nott.executor.AbstractExecutor;
import org.nott.global.GlobalFactory;
import org.nott.utils.SwUtil;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nott
 * @date 2024-9-19
 */
public class SwPlayerJoinEvent implements Listener {

    static final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

    static final Plugin plugin = SwUtil.getPlugin(GlobalFactory.PLUGIN_NAME);

    private static final ConcurrentHashMap<UUID,BukkitTask> PLAYER_ONLINE_TASKS = new ConcurrentHashMap<>(16);

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoinEvent(PlayerJoinEvent event){
        Player player = event.getPlayer();
        long timePeriod = 3600 * 1000 / 20L;
        int rp = AbstractExecutor.CONFIG_YML_FILE.getInt("online.rp", 300);
        // Online reward
        if (player.isOnline()) {
            BukkitTask task = scheduler.runTaskTimer(plugin, () -> {
                KingdomPlayer kingdomPlayer = KingdomPlayer.getKingdomPlayer(player);
                Kingdom kingdom = kingdomPlayer.getKingdom();
                if (kingdomPlayer.hasKingdom() && kingdom != null) {
                    kingdom.addResourcePoints(rp);
                    SwUtil.spigotTextMessage(player.spigot(), SwUtil.retMessage(AbstractExecutor.MESSAGE_YML_FILE, "online.rp_reward") + rp, ChatColor.GOLD);
                }else {
                    PlayerInventory inventory = player.getInventory();
                    long leftCount = Arrays.stream(inventory.getStorageContents()).filter(Objects::isNull).count();
                    if(leftCount == 0){
                        double expDob = AbstractExecutor.CONFIG_YML_FILE.getDouble("online.exp", 200);
                        float exp = (float) expDob;
                        player.setExp(exp);
                        SwUtil.spigotTextMessage(player.spigot(), SwUtil.retMessage(AbstractExecutor.MESSAGE_YML_FILE, "online.exp_reward") + expDob, ChatColor.GOLD);
                    }else {
                        int itemInt = AbstractExecutor.CONFIG_YML_FILE.getInt("online.diamond", 5);
                        ItemStack itemStack = new ItemStack(Material.DIAMOND, itemInt);
                        inventory.addItem(itemStack);
                        SwUtil.spigotTextMessage(player.spigot(), SwUtil.retMessage(AbstractExecutor.MESSAGE_YML_FILE, "online.item_reward") + itemInt, ChatColor.GOLD);
                    }
                }
            }, timePeriod, timePeriod);
            PLAYER_ONLINE_TASKS.put(player.getUniqueId(),task);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuitEvent(PlayerQuitEvent event){
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();
        if(PLAYER_ONLINE_TASKS.containsKey(uniqueId)){
            PLAYER_ONLINE_TASKS.get(uniqueId).cancel();
            PLAYER_ONLINE_TASKS.remove(uniqueId);
        }
    }

}
