package org.nott.utils;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nott.global.GlobalFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author Nott
 * @date 2024-9-9
 */

public class SwUtil{

    static Plugin plugin = Bukkit.getPluginManager().getPlugin(GlobalFactory.PLUGIN_NAME);

    public static boolean hasPlugin(String plugins){
        Plugin plugin = Bukkit.getPluginManager().getPlugin(plugins);
        return plugin != null;
    }

    public static Plugin getPlugin(String plugins){
        return Bukkit.getPluginManager().getPlugin(plugins);
    }

    public static boolean isNotEmpty(Collection collection){
        return collection != null && !collection.isEmpty();
    }

    public static String retMessage(@NotNull FileConfiguration msgFile, @Nullable String parentPath, @NotNull String path){
        if(StringUtils.isNotEmpty(parentPath)){
            return Objects.requireNonNull(msgFile.getConfigurationSection(parentPath)).getString(path);
        }else {
            return msgFile.getString(path);
        }
    }

    public static File[] getPluginFiles(String path){
        File file = new File(plugin.getDataFolder(),path);
        return file.listFiles();
    }


    public static boolean fileIsExist(String path){
        File file = new File(plugin.getDataFolder(),path);
        return file.exists();
    }

    public static File createPluginFile(String path) throws IOException {
        File dataFolder = plugin.getDataFolder();
        File file = new File(dataFolder, path);
        file.createNewFile();
        return file;
    }


    public static boolean checkLocation(String[] xyz) {
        if(xyz.length == 3){
            try {
                for (String single : xyz) {
                    Double.parseDouble(single);
                }
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static YamlConfiguration loadPlugFile(String path) throws Exception{
        File file = new File(plugin.getDataFolder(),path);
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(file);
        return configuration;
    }

    public static void LogThrow(Throwable e) throws RuntimeException{
        Logger logger = plugin.getLogger();
        logger.info(e.getMessage());
        throw new RuntimeException(e);
    }
}
