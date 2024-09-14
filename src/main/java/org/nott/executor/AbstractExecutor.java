package org.nott.executor;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.nott.global.GlobalFactory;

import java.io.File;
import java.util.Objects;

/**
 * @author Nott
 * @date 2024-9-14
 */
@Data
public class AbstractExecutor {

    private Plugin plugin;

    public static YamlConfiguration MESSAGE_YML_FILE;

    public static YamlConfiguration CONFIG_YML_FILE;

    public static final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

    public AbstractExecutor(Plugin plugin) {
        this.plugin = plugin;
    }

    static {
        loadMessageYamlFile();
        loadConfigYamlFile();
    }

    protected static void loadConfigYamlFile() {
        Plugin worldFlagWar = Bukkit.getPluginManager().getPlugin(GlobalFactory.PLUGIN_NAME);
        Objects.requireNonNull(worldFlagWar);
        CONFIG_YML_FILE = (YamlConfiguration) worldFlagWar.getConfig();
    }

    protected static void loadMessageYamlFile() {
        Plugin worldFlagWar = Bukkit.getPluginManager().getPlugin(GlobalFactory.PLUGIN_NAME);
        Objects.requireNonNull(worldFlagWar);
        File dataFolder = worldFlagWar.getDataFolder();
        File file = new File(dataFolder, GlobalFactory.MESSAGE_YML);
        if (!file.exists()) {
            worldFlagWar.saveResource(GlobalFactory.MESSAGE_YML, false);
        }
        File msgFile = new File(dataFolder, GlobalFactory.MESSAGE_YML);
        MESSAGE_YML_FILE = YamlConfiguration.loadConfiguration(msgFile);
    }


}
