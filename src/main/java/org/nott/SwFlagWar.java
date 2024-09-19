package org.nott;


import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.QuickShopProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.nott.executor.FlagWarExecutor;
import org.nott.executor.OfferExecutor;
//import org.nott.executor.SwMoneyExecutor;
import org.nott.global.GlobalFactory;
import org.nott.global.KeyWord;
import org.nott.listener.SwClickBankListener;
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

    public static QuickShopAPI quickShopApi;



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
            message.load(this.getTextResource(GlobalFactory.MESSAGE_YML));
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        // Reg flag war model
        if (config.getBoolean(KeyWord.CONFIG.FLAG_ENABLE)) {
            File[] files = file.listFiles();
            if(SwUtil.isNotNull(files)) {
                pluginManager.registerEvents(new SwFlagWarListener(),this);
                FlagWarManager flagWarManager = new FlagWarManager(this);
                try {
                    flagWarManager.doManage();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            Objects.requireNonNull(this.getCommand(GlobalFactory.FW_COMMAND)).setExecutor(new FlagWarExecutor(this));
            swLogger.info(message.getString(KeyWord.CONFIG.REG_FLAG));
        }

        // Reg Death drop model
        if(config.getBoolean(KeyWord.CONFIG.DROP_ENABLE)){
            pluginManager.registerEvents(new SwDeathListener(), this);
            swLogger.info(message.getString(KeyWord.CONFIG.REG_DEATH));
        }

        // Reg money model
//        if(config.getBoolean(KeyWord.CONFIG.MONEY_ENABLE)){
//            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
//            if (rsp == null) {
//                return;
//            }
//            Objects.requireNonNull(this.getCommand(GlobalFactory.SW_COMMAND)).setExecutor(new SwMoneyExecutor(this));
//            swLogger.info(message.getString(KeyWord.CONFIG.REG_MONEY));
//        }

        // Reg Offer model
        if(config.getBoolean(KeyWord.CONFIG.OFFER_ENABLE)){
            Objects.requireNonNull(this.getCommand(GlobalFactory.OFFER_COMMAND)).setExecutor(new OfferExecutor(this));
            swLogger.info(message.getString(KeyWord.CONFIG.REG_OFFER));
        }

        // Reg Bank model
        if(config.getBoolean(KeyWord.CONFIG.BANK_ENABLE)){
            RegisteredServiceProvider<QuickShopProvider> provider = Bukkit.getServicesManager().getRegistration(QuickShopProvider.class);
            if (provider == null) {
                throw new IllegalStateException("QuickShop hadn't loaded at this moment.");
            }
            quickShopApi = provider.getProvider().getApiInstance();
            pluginManager.registerEvents(new SwClickBankListener(),this);
            swLogger.info(message.getString(KeyWord.CONFIG.REG_BANK));
        }

        swLogger.info("SimpleWorld FlagWar 加载成功");

    }
}