package org.nott.listener;

import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.nott.event.FlagWarOpenEvent;
import org.nott.executor.FlagWarExecutor;
import org.nott.global.GlobalFactory;
import org.nott.global.KeyWord;
import org.nott.manager.FlagWarManager;
import org.nott.model.Location;
import org.nott.model.War;
import org.nott.utils.SwUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nott
 * @date 2024-9-12
 */
public class SwFlagWarListener implements Listener {

    final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

    final Plugin plugin = SwUtil.getPlugin(GlobalFactory.PLUGIN_NAME);

    private ConcurrentHashMap<UUID,String> PLAYER_IN_WAR_ZONE = new ConcurrentHashMap<>();

    // Listening Flag war game open and broadcast remind message.
    @EventHandler(priority = EventPriority.NORMAL)
    public void onFlagWarOpenEvent(FlagWarOpenEvent flagWarOpenEvent){
        War war = flagWarOpenEvent.getWar();
        String start = war.getStart();
        String end = war.getEnd();
        List<Location> locations = war.getLocations();

        String tipMsg = SwUtil.retMessage(FlagWarExecutor.MESSAGE_YML_FILE, KeyWord.WAR.WAR_STARTING_TIP);

        locations.forEach(loc ->{
//            SwUtil.makeCircle(loc,FlagWarExecutor.CONFIG_YML_FILE.getInt("flagWar.game_radius",10), Material.BEDROCK);
            Chunk chunk = SwUtil.makeRegion(loc, Material.BEDROCK);
            war.setChunk(chunk);
            String formatMsg = String.format(tipMsg, start, loc, end);
            SwUtil.broadcast(
                    formatMsg,
                    ChatColor.GOLD
            );
            FlagWarManager.loadWarGame2Map(war);
        });
    }

    // Check if player enter opening flag war game zone.
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerWalkEvent(PlayerMoveEvent event){
        if(FlagWarManager.STARTED_WAR_MAP.isEmpty()){
            return;
        }
        scheduler.runTaskAsynchronously(plugin, () -> {
            org.bukkit.Location eventTo = event.getTo();
            org.bukkit.Location from = event.getFrom();
            if (eventTo == null) return;
            Player player = event.getPlayer();
            Set<String> keySet = FlagWarManager.STARTED_WAR_MAP.keySet();
            Iterator<String> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                War war = FlagWarManager.STARTED_WAR_MAP.get(key);
                Chunk chunk = war.getChunk();
                if (!SwUtil.isInChunkZone(from,chunk) && SwUtil.isInChunkZone(eventTo, chunk)) {
                    PLAYER_IN_WAR_ZONE.put(player.getUniqueId(),"");
                    player.sendTitle(null,SwUtil.retMessage(FlagWarExecutor.MESSAGE_YML_FILE,"enter_zone"),20,70,20);
//                    SwUtil.spigotTextMessage(player.spigot(),SwUtil.retMessage(FlagWarExecutor.MESSAGE_YML_FILE,"enter_zone"),ChatColor.GOLD);
                }
                if (!SwUtil.isInChunkZone(eventTo,chunk) && SwUtil.isInChunkZone(from, chunk)) {
                    PLAYER_IN_WAR_ZONE.remove(player.getUniqueId());
                    player.sendTitle(null,SwUtil.retMessage(FlagWarExecutor.MESSAGE_YML_FILE,"leave_zone"),20,70,20);
//                    SwUtil.spigotTextMessage(player.spigot(),SwUtil.retMessage(FlagWarExecutor.MESSAGE_YML_FILE,"leave_zone"), ChatColor.DARK_GREEN);
                }
            }
        });
    }

    // Check if player place occupy block on flag war game zone.
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlaceEvent(BlockPlaceEvent event){
        if(!PLAYER_IN_WAR_ZONE.containsKey(event.getPlayer().getUniqueId())){
            return;
        }
        Block block = event.getBlock();
        //todo When Player on Flag war game zone place an item and starting to count down time.
    }

    // Drop player head probability while death.
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeathEvent(PlayerDeathEvent event){
        YamlConfiguration config = FlagWarExecutor.CONFIG_YML_FILE;
        double probability = config.getDouble("drop_head.probability", 0.5);
        if (probability < RandomUtils.nextDouble(0.0, probability)) {
            return;
        }
        // Drop player's head
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta sm = (SkullMeta) item.getItemMeta();
        assert sm != null;
        sm.setOwningPlayer(event.getEntity());
        item.setItemMeta(sm);
        event.getDrops().add(item);
    }

}
