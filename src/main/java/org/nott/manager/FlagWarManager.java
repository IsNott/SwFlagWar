package org.nott.manager;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.nott.model.Location;
import org.nott.model.Reward;
import org.nott.model.War;
import org.nott.utils.SwUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Nott
 * @date 2024-9-9
 */
@Data
public class FlagWarManager implements Manager {

    private Plugin plugin;

    private Map<String, War> warMap;

    public FlagWarManager(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void doManage() throws Exception{
        Plugin spigot = getPlugin();

        List<War> wars = convertWarList(spigot);
        Logger logger = spigot.getLogger();
        logger.info("开始调度占领战争");
        if (wars.isEmpty()) {
            logger.info("没有可用的占领战争配置，调度结束");
            return;
        }
        //TODO 异步开启战争的方法
        BukkitScheduler scheduler = spigot.getServer().getScheduler();

    }



    private static List<War> convertWarList(Plugin plugin) throws IOException, InvalidConfigurationException {
        File dataFolder = plugin.getDataFolder();
        String warsFolder = dataFolder + File.separator + "wars";
        File file = new File(warsFolder);
        File[] files = file.listFiles();
        ArrayList<War> wars = new ArrayList<>();
        for (File ymlFile : files) {
            YamlConfiguration child = new YamlConfiguration();
            child.load(ymlFile);
            String uuid = child.getString("UUID", null);
            if (StringUtils.isBlank(uuid)) {
                continue;
            }
            String name = child.getString("name");
            String world = child.getString("world");
            List<String> locations =  child.getStringList("locations");
            List<Location> locationList = new ArrayList<>();
            if (SwUtil.isNotEmpty(locations)) {
                for (String location : locations) {
                    String[] splitStr = location.split(" ");
                    if (splitStr.length != 3) {
                        continue;
                    }
                    Location childWarLoc = new Location(splitStr[0], splitStr[1], splitStr[2]);
                    locationList.add(childWarLoc);
                }
            }
            String start = (String) child.get("start");
            String end = (String) child.get("end");
            if (locationList.isEmpty() || StringUtils.isBlank(start) || StringUtils.isBlank(end)) {
                continue;
            }
            List<Integer> days = child.getIntegerList("days");
            List<Integer> types =  child.getIntegerList("types");
            List<Map> rewards = (List<Map>) child.get("rewards");
            ArrayList<Reward> rewardList = new ArrayList<>();
            if (SwUtil.isNotEmpty(rewards)) {
                for (Map map : rewards) {
                    List<Integer> type = (List<Integer>) map.get("type");
                    String command = (String) map.get("command");
                    String material = (String) map.get("material");
                    Integer levelUp = (Integer) map.get("level-up");
                    Integer amount = (Integer) map.get("amount");
                    if (StringUtils.isBlank(command) && StringUtils.isBlank(material) && levelUp == null && amount == null) {
                        continue;
                    }
                    String effect = (String) map.get("effect");
                    String period = (String) map.get("period");
                    Integer periodVal = (Integer) map.get("period-val");
                    boolean muststandOn = (boolean) map.get("must-stand-on");
                    Reward reward = new Reward(type, command, material, levelUp, amount, effect, period, periodVal, muststandOn);
                    rewardList.add(reward);
                }
            }
            War configWar = new War(uuid, name, world, locationList, start, end, days, types, rewardList);
            wars.add(configWar);

        }
        return wars;
    }


}
