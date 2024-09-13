package org.nott;


import org.bukkit.plugin.java.JavaPlugin;
import org.nott.executor.FlagWarExecutor;
import org.nott.global.GlobalFactory;
import org.nott.listener.SwFlagWarListener;
import org.nott.manager.FlagWarManager;
import org.nott.utils.SwUtil;
import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

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
        File[] files = file.listFiles();
        if(SwUtil.isNotNull(Arrays.asList(files))) {
            FlagWarManager flagWarManager = new FlagWarManager(this);
            try {
                flagWarManager.doManage();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        this.getServer().getPluginManager().registerEvents(new SwFlagWarListener(),this);
        Objects.requireNonNull(this.getCommand(GlobalFactory.FW_COMMAND)).setExecutor(new FlagWarExecutor(this));
        swLogger.info("SimpleWorld FlagWar 加载成功");

    }
}