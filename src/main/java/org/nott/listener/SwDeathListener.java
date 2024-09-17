package org.nott.listener;

import org.apache.commons.lang3.RandomUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.nott.executor.FlagWarExecutor;
import org.nott.global.GlobalFactory;
import org.nott.global.KeyWord;
import org.nott.utils.SwUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Nott
 * @date 2024-9-14
 */
public class SwDeathListener implements Listener {

    // Drop player head probability while death.
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeathEvent(PlayerDeathEvent event){
        Player dead = event.getEntity();
        EntityDamageEvent lastDamageCause = dead.getLastDamageCause();
        if(lastDamageCause instanceof EntityDamageByEntityEvent){
            EntityDamageByEntityEvent damageCause = (EntityDamageByEntityEvent) lastDamageCause;
            Entity damageCauseEntity = damageCause.getEntity();
            if(!(damageCauseEntity instanceof Player)){
                return;
            }
            handlePlayerHeadDrop(event, dead);
            handlePlayerInvDrop(event,dead);
        }
    }

    private void handlePlayerInvDrop(PlayerDeathEvent event, Player dead) {
        YamlConfiguration config = FlagWarExecutor.CONFIG_YML_FILE;
        YamlConfiguration messageFile = FlagWarExecutor.MESSAGE_YML_FILE;
        double probability = config.getDouble(KeyWord.CONFIG.DROP_STEAL_PROB, 0.5);
        if (probability < RandomUtils.nextDouble(0.0, 1.0)) {
            return;
        }
        int dropInvCount = RandomUtils.nextInt(1, config.getInt(KeyWord.CONFIG.DROP_STEAL_MAX, 3));
        PlayerInventory inventory = dead.getInventory();
        ItemStack[] contents = inventory.getContents();
        // Filter Player's armor and weapon(sword + bow + .....).
        List<ItemStack> contentContainItem = Arrays.stream(contents)
                .filter(Objects::nonNull)
                .filter(r -> !GlobalFactory.NOT_DROPS_EQUIP.contains(r.getType()))
                .collect(Collectors.toList());
        int contentLength = contentContainItem.size();
        if(SwUtil.isEmpty(contentContainItem) || contentLength == 0){
            return;
        }
        // Drop death player's item
        if (contentLength <= dropInvCount) {
            event.getDrops().addAll(contentContainItem);
            if(SwUtil.isEmpty(contentContainItem))return;
            dropInvCount = contentLength;
            inventory.removeItem(contentContainItem.toArray(new ItemStack[0]));
        }else {
            List<ItemStack> drops = new LinkedList<>();
            for (int i = 0; i < dropInvCount; i++) {
                int removeIndex = RandomUtils.nextInt(0, contentLength);
                ItemStack itemStack = contentContainItem.get(removeIndex);
                drops.add(itemStack);
                inventory.removeItem(itemStack);
            }
            if(SwUtil.isEmpty(drops))return;
            inventory.removeItem(drops.toArray(new ItemStack[0]));
            event.getDrops().addAll(drops);
        }
        SwUtil.spigotTextMessage(dead.spigot()
                , String.format(messageFile.getString(KeyWord.CONFIG.DROP_INVENTORY), dropInvCount)
                , ChatColor.RED);

    }

    private static void handlePlayerHeadDrop(PlayerDeathEvent event, Player entity) {
        YamlConfiguration config = FlagWarExecutor.CONFIG_YML_FILE;
        YamlConfiguration messageFile = FlagWarExecutor.MESSAGE_YML_FILE;
        double probability = config.getDouble(KeyWord.CONFIG.DROP_HEAD_PROB, 0.5);
        if (probability < RandomUtils.nextDouble(0.0, 1.0)) {
            return;
        }
        // Drop player's head
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta sm = (SkullMeta) item.getItemMeta();
        assert sm != null;
        sm.setOwningPlayer(entity);
        item.setItemMeta(sm);
        event.getDrops().add(item);
        SwUtil.spigotTextMessage(entity.spigot()
                , messageFile.getString(KeyWord.CONFIG.DROP_HEAD)
                , ChatColor.RED);
    }

}
