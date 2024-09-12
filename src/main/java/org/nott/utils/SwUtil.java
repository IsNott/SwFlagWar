package org.nott.utils;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nott.global.Formatter;
import org.nott.global.GlobalFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
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

    public static boolean isNotNull(Collection collection){
        return collection != null && !collection.isEmpty();
    }

    public static boolean isNotNull(Object obj){
        return obj != null && !"".equals(obj);
    }

    public static boolean isEmpty(Collection collection){
        return !isNotNull(collection);
    }

    public static <T> boolean arrayNotEmpty(T[] arrays){
        return arrays != null && arrays.length > 0;
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

    public static void logThrow(Throwable e) throws RuntimeException{
        Logger logger = plugin.getLogger();
        logger.log(Level.ALL,e.getMessage(),e);
    }

    public static void log(String msg){
        Logger logger = plugin.getLogger();
        logger.log(Level.ALL,msg);
    }

    public static boolean checkHourStr(String timeStr) {
        try {
            Formatter.DATE.HH_MM.parse(timeStr);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    public static boolean hourIsBefore(String time1,String time2){
        Date hour1 = null;
        Date hour2 = null;
        try {
            hour1 = Formatter.DATE.HH_MM.parse(time1);
            hour2 = Formatter.DATE.HH_MM.parse(time2);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return hour1.before(hour2);

    }

    public static <T> Map<String,Object> convertMap(T obj){
        Map<String, Object> map = null;
        try {
            map = new HashMap<String, Object>();
            Class<?> aClass = obj.getClass();
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(obj));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return map;
    }
}
