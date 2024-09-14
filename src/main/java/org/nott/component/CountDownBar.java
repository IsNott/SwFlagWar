package org.nott.component;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.nott.global.GlobalFactory;
import org.nott.utils.SwUtil;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nott
 * @date 2024-9-14
 */
@Data
public class CountDownBar {

    private Integer totalInterval;

    private Integer timeLeft;

    private String title;

    private ConcurrentHashMap<UUID, Player> players = new ConcurrentHashMap<>();

    private String id;

    private BossBar bossBar;

    private BukkitTask bukkitTask;

    private String currentKingdomName;

    public static CountDownBar create(String title, Integer totalInterval) {
        CountDownBar countDownBar = new CountDownBar();
        countDownBar.setTotalInterval(totalInterval);
        countDownBar.setTimeLeft(totalInterval);
        countDownBar.setTitle(String.format(title, countDownBar.getTimeLeft()));
        BossBar bossBar = Bukkit.createBossBar(title, SwUtil.randomBukkitBossBarColor(), BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
        bossBar.setVisible(true);
        countDownBar.setBossBar(bossBar);
        return countDownBar;
    }

    public void addPlayers(List<Player> players) {
        players.forEach(this::addPlayer);
    }

    public void addPlayer(Player player) {
        this.players.put(player.getUniqueId(), player);
        bossBar.addPlayer(player);
    }

    public void removePlayer(Player player) {
        this.players.remove(player.getUniqueId());
        bossBar.removePlayer(player);
    }

    public BukkitTask run() {
        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                Integer interval = getTotalInterval();
                Integer left = getTimeLeft();
                getBossBar().setProgress((double) left / interval);
                setTimeLeft(left - 1);
            }
        }.runTaskTimer(SwUtil.getPlugin(GlobalFactory.PLUGIN_NAME), 0, 20);
        return bukkitTask;
    }

    public void cancel() {
        this.bossBar.setVisible(false);
        this.bukkitTask.cancel();
    }
}
