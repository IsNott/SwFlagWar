package org.nott.utils;

import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nott.global.Formatter;
import org.nott.global.GlobalFactory;
import org.nott.model.Location;

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

public class SwUtil {

    static Plugin plugin = Bukkit.getPluginManager().getPlugin(GlobalFactory.PLUGIN_NAME);

    public static boolean hasPlugin(String plugins) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(plugins);
        return plugin != null;
    }

    public static Plugin getPlugin(String plugins) {
        return Bukkit.getPluginManager().getPlugin(plugins);
    }

    public static boolean isNotNull(Collection collection) {
        return collection != null && !collection.isEmpty();
    }

    public static <T> boolean isNotNull(T[] arrays) {
        return arrays != null && arrays.length > 0;
    }

    public static boolean isNotNull(Object obj) {
        return obj != null && !"".equals(obj);
    }

    public static boolean isEmpty(Collection collection) {
        return !isNotNull(collection);
    }

    public static <T> boolean arrayNotEmpty(T[] arrays) {
        return arrays != null && arrays.length > 0;
    }

    public static String retMessage(@NotNull FileConfiguration msgFile, @Nullable String parentPath, @NotNull String path) {
        if (StringUtils.isNotEmpty(parentPath)) {
            return Objects.requireNonNull(msgFile.getConfigurationSection(parentPath)).getString(path);
        } else {
            return msgFile.getString(path);
        }
    }

    public static String retMessage(@NotNull FileConfiguration msgFile, @NotNull String path) {
        return msgFile.getString(path);
    }

    public static File[] getPluginFiles(String path) {
        File file = new File(plugin.getDataFolder(), path);
        return file.listFiles();
    }


    public static boolean fileIsExist(String path) {
        File file = new File(plugin.getDataFolder(), path);
        return file.exists();
    }

    public static File createPluginFile(String path) throws IOException {
        File dataFolder = plugin.getDataFolder();
        File file = new File(dataFolder, path);
        file.createNewFile();
        return file;
    }


    public static boolean checkLocation(String[] xyz) {
        if (xyz.length == 3) {
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

    public static YamlConfiguration loadPlugFile(String path) throws Exception {
        File file = new File(plugin.getDataFolder(), path);
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(file);
        return configuration;
    }

    public static void logThrow(Throwable e) throws RuntimeException {
        Logger logger = plugin.getLogger();
        logger.log(Level.ALL, e.getMessage(), e);
        throw new RuntimeException(e);
    }

    public static void log(String msg) {
        Logger logger = plugin.getLogger();
        logger.log(Level.ALL, msg);
    }

    public static boolean checkHourStr(String timeStr) {
        try {
            Formatter.DATE.HH_MM.parse(timeStr);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    public static boolean hourIsBefore(String time1, String time2) {
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

    public static <T> Map<String, Object> convertMap(T obj) {
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

    public static void sendMessage2Sender(CommandSender sender, String message, @Nullable ChatColor color) {
        sender.spigot().sendMessage(
                TextComponent.fromLegacy(color + message)
        );
    }

    public static <T extends CommandSender.Spigot> void spigotTextMessage(T spigot, String message, ChatColor color) {
        spigot.sendMessage(
                TextComponent.fromLegacy(color + message)
        );
    }

    public static void broadcast(String msg, ChatColor color) {
        Bukkit.spigot().broadcast(
                TextComponent.fromLegacy(msg, color.asBungee())
        );
    }

    public static BarColor randomBukkitBossBarColor() {
        List<BarColor> list = Arrays.asList(BarColor.values());
        int index = RandomUtils.nextInt(0, list.size() - 1);
        return list.get(index);
    }

    public static void makeCircle(Location loc, Integer radius, Material m) {
        int x;
        int y = Integer.parseInt(loc.getX());
        int z;

        for (double i = 0.0; i < 360.0; i += 0.1) {
            double angle = i * Math.PI / 180;
            x = (int) (Double.parseDouble(loc.getX()) + radius * Math.cos(angle));
            z = (int) (Double.parseDouble(loc.getZ()) + radius * Math.sin(angle));

            Objects.requireNonNull(Bukkit.getServer().getWorld("world")).getBlockAt(x, y, z).setType(m);
        }
    }

    public static Chunk makeRegion(Location centerPointLoc, Material m) {
        double x = Double.parseDouble(centerPointLoc.getX());
        double y = Double.parseDouble(centerPointLoc.getY());
        double z = Double.parseDouble(centerPointLoc.getZ());
        // Mark Flag war game center point.
        World defWorld = Bukkit.getServer().getWorld("world");
        Objects.requireNonNull(defWorld);
        Block centerPointBlock = defWorld.getBlockAt((int) x, (int) y, (int) z);
        Objects.requireNonNull(centerPointBlock);
        centerPointBlock.setType(m);
        Chunk chunk = centerPointBlock.getChunk();
        // Get a chuck by block
        return chunk;
    }

    public static void makeSquare(Location centerPointLoc, Integer radius, Material m) {
        double x = Double.parseDouble(centerPointLoc.getX());
        double y = Double.parseDouble(centerPointLoc.getY());
        double z = Double.parseDouble(centerPointLoc.getZ());
        int intX = (int) x;
        int intY = (int) y;
        int intZ = (int) z;
        // Mark Flag war game center point.
        World defWorld = Bukkit.getServer().getWorld("world");
        Block block = null;
        Block centerPointBlock = defWorld.getBlockAt(intX, intY, intZ);
        centerPointBlock.setType(m);
        Objects.requireNonNull(defWorld);
        for (Integer i = 1; i < radius; i++) {
            int xBlock = intX + i;
            int xBlock1 = intX - i;
            int zBlock = intZ + i;
            int zBlock1 = intZ - i;
            defWorld.getBlockAt(xBlock, intY, intZ).setType(m);
            defWorld.getBlockAt(xBlock1, intY, intZ).setType(m);
            defWorld.getBlockAt(intX, intY, zBlock).setType(m);
            defWorld.getBlockAt(intX, intY, zBlock1).setType(m);
            for (Integer j = 1; j < radius; j++) {
                int XBlock = intX + i;
                int XBlock1 = intX - i;
                int ZBlock = intZ + i;
                int ZBlock1 = intZ - i;
                int XBlockj = intX + j;
                int XBlockj1 = intX - j;
                int ZBlockj = intZ + j;
                int ZBlockj1 = intZ - j;
                defWorld.getBlockAt(XBlock, intY, ZBlockj).setType(m);
                defWorld.getBlockAt(XBlock1, intY, ZBlockj1).setType(m);
                defWorld.getBlockAt(XBlockj1, intY, ZBlock).setType(m);
                defWorld.getBlockAt(XBlockj, intY, ZBlock1).setType(m);
            }
        }
    }

    public static boolean isInChunkZone(org.bukkit.Location currLoc, Chunk chunk) {
        double x = currLoc.getX();
        double y = currLoc.getY();
        double z = currLoc.getZ();
        World defWorld = Bukkit.getServer().getWorld("world");
        Objects.requireNonNull(defWorld);
        Block block = defWorld.getBlockAt((int) x, (int) y, (int) z);
        BlockData blockData = block.getBlockData();
        return chunk.contains(blockData);
    }

    public static boolean isInGameSquare(org.bukkit.Location toLoc, Location center, Integer gameRadius) {
        double x = toLoc.getX();
        double y = toLoc.getY();
        double z = toLoc.getZ();
        int pX = (int) Double.parseDouble(center.getX());
        int pY = (int) Double.parseDouble(center.getY());
        int pZ = (int) Double.parseDouble(center.getZ());

        int gotoX = (int) x;
        int gotoY = (int) y;
        int gotoZ = (int) z;

        World defWorld = Bukkit.getServer().getWorld("world");
        Objects.requireNonNull(defWorld);

        if (gotoY < pY) {
            return false;
        }

        return (gotoX >= pX && gotoX <= (pX + gameRadius)) && (gotoZ > pZ && gotoZ <= (pZ + gameRadius))
                ||
                (gotoX <= pX && gotoX >= (pX - gameRadius)) && (gotoZ > pZ && gotoZ <= (pZ - gameRadius))
                        ||
                (gotoX >= pX && gotoX <= (pX + gameRadius)) && (gotoZ <= pZ && gotoZ >= (pZ - gameRadius))
                ||
                (gotoX <= pX && gotoX >= (pX - gameRadius)) && (gotoZ <= pZ && gotoZ >= (pZ - gameRadius));

    }
}

