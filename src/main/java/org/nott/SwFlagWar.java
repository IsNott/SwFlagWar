package org.nott;


import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.nott.executor.FlagWarExecutor;
import org.nott.executor.OfferExecutor;
import org.nott.global.GlobalFactory;
import org.nott.global.KeyWord;
import org.nott.listener.SwDeathListener;
import org.nott.listener.SwFlagWarListener;
import org.nott.manager.FlagWarManager;
import org.nott.utils.SwUtil;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

@SuppressWarnings({""})
public class SwFlagWar extends JavaPlugin {

    public final Logger swLogger = super.getLogger();

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        File dataFolder = this.getDataFolder();
        saveDefaultConfig();
        String warsFolder = dataFolder + File.separator + GlobalFactory.WAR_BASE_DIR;
        File file = new File(warsFolder);
        if(!file.exists()){
            file.mkdir();
        }
        File exampleFile = new File(dataFolder + File.separator + GlobalFactory.EXAMPLE_FILE);
        if(!exampleFile.exists()){
            this.saveResource(GlobalFactory.EXAMPLE_FILE, false);
        }
        FileConfiguration config = this.getConfig();
        PluginManager pluginManager = this.getServer().getPluginManager();
        YamlConfiguration message = new YamlConfiguration();
        try {
            message.load(new File(GlobalFactory.MESSAGE_YML));
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        if (config.getBoolean(KeyWord.CONFIG.FLAG_ENABLE)) {
            File[] files = file.listFiles();
            if(SwUtil.isNotNull(files)) {
                FlagWarManager flagWarManager = new FlagWarManager(this);
                try {
                    flagWarManager.doManage();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            pluginManager.registerEvents(new SwFlagWarListener(),this);
            Objects.requireNonNull(this.getCommand(GlobalFactory.FW_COMMAND)).setExecutor(new FlagWarExecutor(this));
            swLogger.info(message.getString(KeyWord.CONFIG.REG_FLAG));
        }
        if(config.getBoolean(KeyWord.CONFIG.DROP_ENABLE)){
            pluginManager.registerEvents(new SwDeathListener(), this);
            swLogger.info(message.getString(KeyWord.CONFIG.REG_DEATH));
        }
        if(config.getBoolean(KeyWord.CONFIG.OFFER_ENABLE)){
            Objects.requireNonNull(this.getCommand(GlobalFactory.OFFER_COMMAND)).setExecutor(new OfferExecutor(this));
            swLogger.info(message.getString(KeyWord.CONFIG.REG_OFFER));
        }
        swLogger.info("SimpleWorld FlagWar 加载成功");

    }
}