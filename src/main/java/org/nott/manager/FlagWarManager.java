package org.nott.manager;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Color;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.nott.exception.SwException;
import org.nott.global.GlobalFactory;
import org.nott.global.KeyWord;
import org.nott.model.Location;
import org.nott.model.Reward;
import org.nott.model.War;
import org.nott.utils.SwUtil;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author Nott
 * @date 2024-9-9
 */
@Data
public class FlagWarManager implements Manager {

    private Plugin plugin;

    private Logger logger;

    public static Map<String, War> FLAG_WAR_MAP = new ConcurrentHashMap<>(16);

    public static Map<String, War> SCHEDULE_WAR_MAP = new ConcurrentHashMap<>(16);

    public FlagWarManager(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void doManage() throws Exception{
        Plugin spigot = getPlugin();
        setLogger(spigot.getLogger());

        List<War> wars = convertWarList();
        logger.info("开始调度占领战争");
        if (SwUtil.isEmpty(wars)) {
            logger.info("没有可用的占领战争配置，调度结束");
            return;
        }
        // 异步加载战争
        BukkitScheduler scheduler = spigot.getServer().getScheduler();

        scheduler.runTaskAsynchronously(spigot, () -> {
            wars.forEach(r -> {
                try {
                    remindWarGame(r);
                } catch (Exception e) {
                    SwUtil.LogThrow(e);
                }
            });
        });

    }


    public void remindWarGame(War war) {
        remindWarGame(false,war);
    }

    public void remindWarGame(boolean instantly, War war) {
        Plugin spigot = getPlugin();
        Server server = spigot.getServer();

        BukkitScheduler bukkitScheduler = spigot.getServer().getScheduler();
        bukkitScheduler.runTaskAsynchronously(spigot, () -> {
            try {

                List<Location> locations = war.getLocations();
                if (SwUtil.isEmpty(locations)) {
                    SwUtil.log("对应的FlagWar没有位置信息");
                    return;
                }
                YamlConfiguration msgFile = SwUtil.loadPlugFile(GlobalFactory.MESSAGE_YML);
                String start = war.getStart();
                String end = war.getEnd();
                String warOpenTip = msgFile.getString("war_open_tip");
                String warStartingTip = msgFile.getString("war_starting_tip");

                //  Load/Schedule War Map
                if (instantly) {
                    for (Location location : locations) {
                        String formatMsg = String.format(warOpenTip, start, location.toString(), end);
                        server.broadcastMessage(Color.GREEN + formatMsg);
                        loadWarGame2Map(war);
                        Thread.sleep(Duration.ofSeconds(1));
                    }
                } else {
                    for (Location location : locations) {
                        String formatMsg = String.format(warStartingTip, location.toString(), end);
                        server.broadcastMessage(Color.GREEN + formatMsg);
                        // TODO Time
                        bukkitScheduler.runTaskLater(spigot,()->loadWarGame2Map(war),Duration.between(null,null));
                        Thread.sleep(Duration.ofSeconds(1));
                    }
                }
            } catch (Exception e) {
                SwUtil.LogThrow(e);
            }
        });
    }

    public void loadWarGame2Map(War war){
        FLAG_WAR_MAP.put(war.getUUID(),war);
    }

    public void loadWarGame2Map(YamlConfiguration warConfig){
        War war = parseWarPoJo(warConfig);
        FLAG_WAR_MAP.put(war.getUUID(),war);
    }

    private static War parseWarPoJo(YamlConfiguration warConfig) {
        War war = new War();
        war.setUUID(warConfig.getString(KeyWord.WAR.UUID));
        war.setName(warConfig.getString(KeyWord.WAR.NAME));
        war.setStart(warConfig.getString(KeyWord.WAR.START));
        war.setEnd(warConfig.getString(KeyWord.WAR.END));
        war.setDays(warConfig.getIntegerList(KeyWord.WAR.DAYS));
        war.setWorld(warConfig.getString(KeyWord.WAR.WORLD));
        war.setTypes(warConfig.getIntegerList(KeyWord.WAR.TYPES));
        Reward reward = new Reward();
        reward.setType(warConfig.getIntegerList(KeyWord.WAR.REWARDS_TYPE));
        reward.setEffect(warConfig.getString(KeyWord.WAR.REWARDS_EFFECT));
        reward.setAmount(warConfig.getInt(KeyWord.WAR.REWARDS_AMOUNT));
        reward.setPeriod(warConfig.getString(KeyWord.WAR.REWARDS_PERIOD));
        reward.setPeriodVal(warConfig.getInt(KeyWord.WAR.REWARDS_PERIOD_VAL));
        reward.setLevelUp(warConfig.getInt(KeyWord.WAR.REWARDS_LEVEL_UP));
        reward.setCommand(warConfig.getString(KeyWord.WAR.REWARDS_COMMAND));
        reward.setMaterial(warConfig.getString(KeyWord.WAR.REWARDS_MATERIAL));
        reward.setMustStandOn(warConfig.getBoolean(KeyWord.WAR.REWARDS_MUST_STAND_ON));
        war.setRewards(reward);
        return war;
    }


    private static List<War> convertWarList() throws IOException, InvalidConfigurationException {
        ArrayList<War> wars = new ArrayList<>();
        File[] files = SwUtil.getPluginFiles(GlobalFactory.WAR_BASE_DIR);
        for (File ymlFile : files) {
            YamlConfiguration warConfig = new YamlConfiguration();
            warConfig.load(ymlFile);
            String uuid = warConfig.getString(KeyWord.WAR.UUID);
            if (StringUtils.isBlank(uuid)) {
                continue;
            }
            String name = warConfig.getString(KeyWord.WAR.NAME);
            String world = warConfig.getString(KeyWord.WAR.WORLD);
            List<String> locations =  warConfig.getStringList(KeyWord.WAR.LOCATIONS);
            List<Location> locationList = new ArrayList<>();
            if (SwUtil.isEmpty(locations)) {
               continue;
            }
            for (String location : locations) {
                String[] splitStr = location.split(" ");
                if (splitStr.length != 3) {
                    continue;
                }
                Location childWarLoc = new Location(splitStr[0], splitStr[1], splitStr[2]);
                locationList.add(childWarLoc);
            }
            String start = warConfig.getString(KeyWord.WAR.START);
            String end =  warConfig.getString(KeyWord.WAR.END);
            if (locationList.isEmpty() || StringUtils.isBlank(start) || StringUtils.isBlank(end)) {
                continue;
            }
            List<Integer> days = warConfig.getIntegerList(KeyWord.WAR.DAYS);
            List<Integer> types =  warConfig.getIntegerList(KeyWord.WAR.TYPES);
            Reward reward = new Reward();
            List<Integer> type = warConfig.getIntegerList(KeyWord.WAR.REWARDS_TYPE);
            String effect = warConfig.getString(KeyWord.WAR.REWARDS_EFFECT);
            String command = warConfig.getString(KeyWord.WAR.REWARDS_COMMAND);
            String material = warConfig.getString(KeyWord.WAR.REWARDS_MATERIAL);
            String period = warConfig.getString(KeyWord.WAR.REWARDS_PERIOD);
            Integer periodVal = warConfig.getInt(KeyWord.WAR.REWARDS_PERIOD_VAL);
            Integer amount = warConfig.getInt(KeyWord.WAR.REWARDS_AMOUNT);
            int levelUp = warConfig.getInt(KeyWord.WAR.REWARDS_LEVEL_UP);
            boolean muststandOn = warConfig.getBoolean(KeyWord.WAR.REWARDS_MUST_STAND_ON);
            if (SwUtil.isEmpty(type)) {
                continue;
            }
            if (StringUtils.isBlank(command) && StringUtils.isBlank(effect) && StringUtils.isBlank(material) && levelUp == 0 && amount == 0) {
                continue;
            }
            reward.setType(warConfig.getIntegerList(KeyWord.WAR.REWARDS_TYPE));
            reward.setEffect(warConfig.getString(KeyWord.WAR.REWARDS_EFFECT));
            reward.setAmount(warConfig.getInt(KeyWord.WAR.REWARDS_AMOUNT));
            reward.setPeriod(warConfig.getString(KeyWord.WAR.REWARDS_PERIOD));
            reward.setPeriodVal(warConfig.getInt(KeyWord.WAR.REWARDS_PERIOD_VAL));
            reward.setLevelUp(warConfig.getInt(KeyWord.WAR.REWARDS_LEVEL_UP));
            reward.setCommand(warConfig.getString(KeyWord.WAR.REWARDS_COMMAND));
            reward.setMaterial(warConfig.getString(KeyWord.WAR.REWARDS_MATERIAL));
            reward.setMustStandOn(warConfig.getBoolean(KeyWord.WAR.REWARDS_MUST_STAND_ON));
            reward = new Reward(type, command, material, levelUp, amount, effect, period, periodVal, muststandOn);
            War configWar = new War(uuid, name, world, locationList, start, end, days, types, reward);
            wars.add(configWar);
        }
        return wars;
    }


}
