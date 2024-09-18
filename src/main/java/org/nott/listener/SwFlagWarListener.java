package org.nott.listener;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.kingdoms.constants.player.KingdomPlayer;
import org.nott.component.CountDownBar;
import org.nott.event.FlagWarOpenEvent;
import org.nott.executor.AbstractExecutor;
import org.nott.executor.FlagWarExecutor;
import org.nott.global.GlobalFactory;
import org.nott.global.KeyWord;
import org.nott.manager.FlagWarManager;
import org.nott.model.Location;
import org.nott.model.War;
import org.nott.utils.SwUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Nott
 * @date 2024-9-12
 */
public class SwFlagWarListener implements Listener {

    static final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

    static final Plugin plugin = SwUtil.getPlugin(GlobalFactory.PLUGIN_NAME);

    private static ConcurrentHashMap<UUID, War> PLAYER_IN_WAR_ZONE = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, CountDownBar> playerCountDowBar = new ConcurrentHashMap<>();

    private static Vector<String> processingWars = new Vector<>();

    // Listening Flag war game open and broadcast remind message.
    @EventHandler(priority = EventPriority.NORMAL)
    public void onFlagWarOpenEvent(FlagWarOpenEvent flagWarOpenEvent) {
        War war = flagWarOpenEvent.getWar();
        String end = war.getEnd();
        List<Location> locations = war.getLocations();

        String tipMsg = SwUtil.retMessage(FlagWarExecutor.MESSAGE_YML_FILE, KeyWord.CONFIG.WAR_STARTING_TIP);

        locations.forEach(loc -> {
//            SwUtil.makeCircle(loc,FlagWarExecutor.CONFIG_YML_FILE.getInt("flagWar.game_radius",10), Material.BEDROCK);
//            SwUtil.makeSquare(loc, FlagWarExecutor.CONFIG_YML_FILE.getInt("flag_war.game_radius"), Material.BEDROCK);
            Chunk chunk = SwUtil.makeChunkZone(loc, Material.BEDROCK);
            war.setChunk(chunk);
            String formatMsg = String.format(tipMsg, loc, end);
            SwUtil.broadcast(
                    formatMsg,
                    ChatColor.GOLD);
            FlagWarManager.loadWarGame2Map(war);

        });
    }

    // Check if player enter opening flag war game zone.
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerWalkEvent(PlayerMoveEvent event) {
        if (FlagWarManager.STARTED_WAR_MAP.isEmpty()) {
            return;
        }
        scheduler.runTaskAsynchronously(plugin, () -> {
            org.bukkit.Location eventTo = event.getTo();
            org.bukkit.Location from = event.getFrom();
            if (eventTo == null) return;
            Player player = event.getPlayer();
            Set<String> keySet = FlagWarManager.STARTED_WAR_MAP.keySet();
            Iterator<String> iterator = keySet.iterator();
            int gameRadius = FlagWarExecutor.CONFIG_YML_FILE.getInt("flag_war.game_radius");
            while (iterator.hasNext()) {
                String key = iterator.next();
                War war = FlagWarManager.STARTED_WAR_MAP.get(key);
                Location location = war.getLocations().get(0);
                // Get War Game Info By Location
                // Check if player entering/leaving game zone.
                // enter
                Chunk toChunk = eventTo.getChunk();
                Chunk fromChunk = from.getChunk();
                Chunk chunk = war.getChunk();
                if (chunk == null) {
                    continue;
                }
//                if (SwUtil.isInGameSquare(eventTo, location,gameRadius) && !SwUtil.isInGameSquare(from,location,gameRadius)) {
                if (toChunk.equals(chunk) && !fromChunk.equals(chunk)) {
                    PLAYER_IN_WAR_ZONE.put(player.getUniqueId(), war);
                    player.sendTitle("", ChatColor.GOLD + SwUtil.retMessage(FlagWarExecutor.MESSAGE_YML_FILE, "war.enter_zone"), 20, 30, 20);
                }

                // leaving
//                if (!SwUtil.isInGameSquare(eventTo, location,gameRadius) && SwUtil.isInGameSquare(from,location,gameRadius)) {
                if (!toChunk.equals(chunk) && fromChunk.equals(chunk)) {
                    PLAYER_IN_WAR_ZONE.remove(player.getUniqueId());
                    player.sendTitle("", ChatColor.GREEN + SwUtil.retMessage(FlagWarExecutor.MESSAGE_YML_FILE, "war.leave_zone"), 20, 30, 20);
                    playerCountDowBar.remove(player.getUniqueId());
                }
            }
        });
    }

    // Check if player place occupy block on flag war game zone.
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        UUID uniqueId = event.getPlayer().getUniqueId();
        if (!PLAYER_IN_WAR_ZONE.containsKey(uniqueId)) {
            return;
        }
        War war = PLAYER_IN_WAR_ZONE.get(uniqueId);
        Player player = event.getPlayer();
        YamlConfiguration configFile = FlagWarExecutor.CONFIG_YML_FILE;
        Block block = event.getBlockPlaced();
        BlockData blockData = block.getBlockData();
        Material material = blockData.getMaterial();
        if (material.isAir()) {
            return;
        }
        String uuid = war.getUUID();
        if (processingWars.contains(uuid)) {
            event.setCancelled(true);
            player.sendTitle("", SwUtil.retMessage(AbstractExecutor.MESSAGE_YML_FILE, "war.processing"), 20, 30, 20);
        } else {
            // When Player on Flag war game zone place an item and starting to count down time.
            ItemStack itemStack = configFile.getItemStack("flag_war.occupy_item", new ItemStack(Material.CRYING_OBSIDIAN));
            if (itemStack.getType().equals(material)) {
                // start to count down and BroadCast Info to other Player
                KingdomPlayer kingdomPlayer = KingdomPlayer.getKingdomPlayer(player);
                if (!kingdomPlayer.hasKingdom()) {
                    player.sendTitle("", SwUtil.retMessage(AbstractExecutor.MESSAGE_YML_FILE, "war.not_join_kingdom"), 20, 70, 20);
                    return;
                }
                String processStr = AbstractExecutor.MESSAGE_YML_FILE.getString("war.process_title");
                int time = AbstractExecutor.CONFIG_YML_FILE.getInt("flag_war.occupy_time", 600);
                String title = String.format(processStr, kingdomPlayer.getKingdom().getName(), time);
                CountDownBar countDownBar = CountDownBar.create(title, time);

                //TODO List player and CountDown bar
                List<Player> playerInChunk = Arrays.stream(war.getChunk().getEntities()).filter(r -> r instanceof Player)
                        .map(r -> (Player) r)
                        .collect(Collectors.toList());
                countDownBar.addPlayers(playerInChunk);
                countDownBar.run();
                playerCountDowBar.put(uuid,countDownBar);
            }
        }

    }

    // Check if Other player break occupy item from first player
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();
        if (!PLAYER_IN_WAR_ZONE.containsKey(uniqueId)) {
            return;
        }
        War war = PLAYER_IN_WAR_ZONE.get(uniqueId);
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Material type = block.getType();
        ItemStack itemStack = FlagWarExecutor.CONFIG_YML_FILE.getItemStack("flag_war.occupy_item", new ItemStack(Material.CRYING_OBSIDIAN));
        if(chunk.equals(war.getChunk()) && type.equals(itemStack.getType())){
            playerCountDowBar.get(war.getUUID()).getBossBar().setVisible(false);
            playerCountDowBar.remove(war.getUUID());
            Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin,()->{
                List<Player> playerInChunk = Arrays.stream(war.getChunk().getEntities()).filter(r -> r instanceof Player)
                        .map(r -> (Player) r)
                        .collect(Collectors.toList());
                playerInChunk.forEach(p -> p.sendTitle("", SwUtil.retMessage(AbstractExecutor.MESSAGE_YML_FILE, "war.remove_process"), 20, 30, 20));
            });
        }
    }

}
