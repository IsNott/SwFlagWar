package org.nott.executor;

import lombok.Data;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nott.global.GlobalFactory;
import org.nott.global.KeyWord;
import org.nott.manager.FlagWarManager;
import org.nott.model.Reward;
import org.nott.utils.SwUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Flagwar 命令执行器
 * @author Nott
 * @date 2024-9-9
 */
@Data
public class FlagWarExecutor extends AbstractExecutor implements CommandExecutor{

    public FlagWarExecutor(Plugin plugin) {
        super(plugin);
    }




    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Plugin spigot = getPlugin();

        if (args == null || args.length == 0) {
            return false;
        }

        Set<PermissionAttachmentInfo> effectivePermissions = commandSender.getEffectivePermissions();
        PermissionAttachmentInfo permissionAttachmentInfo = effectivePermissions.stream().filter(r -> KeyWord.PERMISSION.WAR_OP_PERM.equals(r.getPermission())).findAny().orElse(null);
        boolean isOp = permissionAttachmentInfo != null;
        // Handle 'fw help' Command
        boolean helpCommand = executeHelpCommand(isOp, args, commandSender);

        // Handle 'fw list' Command
        boolean warListCommand = executeListCommand(args, commandSender);

        // Handle 'fw reload' Command
        boolean reloadCommand = executeReloadCommand(commandSender, args, spigot);

        // Handle 'fw create' Command
        boolean createCommand = executeCreateCommand(isOp, commandSender, args, spigot);

        // Handle 'fw add location' Command
        boolean addLocationCommand = executeAddLocationCommand(isOp, commandSender, args, spigot);

        // Handle 'fw list location' Command
//        boolean listLocationCommand = executeListLocationCommand(isOp, commandSender, args, spigot);

        // Handle 'fw loc remove/reset' Command
//        boolean setOrRemoveLocationCommand = executeSetOrRemoveLocationCommand(isOp, commandSender, args, spigot);

        boolean setOrRemoveLocationCommand = executeResetLocationCommand(isOp, commandSender, args, spigot);

        boolean result =  helpCommand || warListCommand || reloadCommand || createCommand || addLocationCommand || setOrRemoveLocationCommand;

        if(!result){
            SwUtil.sendMessage2Sender(commandSender, SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.MESSAGE_YML, KeyWord.COMMON.UNKNOWN_COMMAND), ChatColor.RED);
            return true;
        }

        return true;
    }

    private boolean executeListCommand(String[] args, CommandSender commandSender) {
        if (args.length == 1 && KeyWord.WAR.LIST.equals(args[0])) {
            try {
                File[] pluginFiles = SwUtil.getPluginFiles(GlobalFactory.WAR_BASE_DIR);
                if (SwUtil.arrayNotEmpty(pluginFiles)) {
                    List<String> messages = new ArrayList<>();
                    for (File warFile : pluginFiles) {
                        YamlConfiguration configuration = new YamlConfiguration();
                        configuration.load(warFile);
                        List<String> locStrList = configuration.getStringList(KeyWord.WAR.LOCATIONS);
                        String name = configuration.getString(KeyWord.WAR.NAME);
                        if (SwUtil.isEmpty(locStrList) || StringUtils.isEmpty(name)) {
                            continue;
                        }
                        String loc = locStrList.get(0);
                        String warShowInfo = SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.WAR_MESSAGE_SUFFIX, "war_show_info");
                        messages.add(String.format(warShowInfo, name,loc));
                    }
                    messages.add(ChatColor.GREEN + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.COMMON_MSG_SUFFIX, "no_more_info"));
                    for (String message : messages) {
                        commandSender.spigot().sendMessage(TextComponent.fromLegacy(message));
                    }
                    return true;
                }
            } catch (Exception e) {
                SwUtil.logThrow(e);
            }
        }
        return false;
    }

    private boolean executeResetLocationCommand(boolean isOp, CommandSender commandSender, String[] args, Plugin spigot) {
        if (args.length == 4 && KeyWord.WAR.RESET.equals(args[1]) && KeyWord.WAR.LOC.equals(args[0])) {
            if (requrieOP(isOp, commandSender)) return true;
            try {
                String locationStr = args[3];
                String[] xyz = locationStr.split(KeyWord.COMMON.COMMA);
                if (!SwUtil.checkLocation(xyz)) {
                    commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.WAR_MESSAGE_SUFFIX, "location_format_error")));
                    return true;
                }
                String name = args[2];
                String path2War = GlobalFactory.WAR_BASE_DIR + name + GlobalFactory.YML_PREFIX;
                if (!SwUtil.fileIsExist(path2War)) {
                    commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.WAR_MESSAGE_SUFFIX, "war_not_exist")));
                    return true;
                }
                YamlConfiguration configuration = SwUtil.loadPlugFile(path2War);
                List<String> locations = configuration.getStringList(KeyWord.WAR.LOCATIONS);
                locations.set(0,locationStr);
                scheduler.runTaskAsynchronously(spigot, () -> {
                    try {
                        configuration.save(new File(path2War));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

            } catch (Exception e) {
                commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.COMMON_MSG_SUFFIX, "exception")));
                SwUtil.logThrow(e);
            }
        }
        return false;
    }

    private boolean executeSetOrRemoveLocationCommand(boolean isOp, CommandSender commandSender, String[] args, Plugin spigot) {
        if (args.length > 2 && (KeyWord.WAR.REMOVE.equals(args[1]) || KeyWord.WAR.RESET.equals(args[1])) && KeyWord.WAR.LOC.equals(args[0])) {
            if (requrieOP(isOp, commandSender)) return true;
            try {
                String indexStr = args[3];
                String locationStr = args[4];
                String[] xyz = locationStr.split(KeyWord.COMMON.COMMA);
                int index = Integer.parseInt(indexStr);
                if (!SwUtil.checkLocation(xyz)) {
                    commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.WAR_MESSAGE_SUFFIX, "location_format_error")));
                    return true;
                }
                if (index < 1) {
                    commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.COMMON_MSG_SUFFIX, "index_format_error")));
                    return true;
                }
                String name = args[2];
                boolean isReset = KeyWord.WAR.RESET.equals(args[1]);
                String path2War = GlobalFactory.WAR_BASE_DIR + name + GlobalFactory.YML_PREFIX;
                if (!SwUtil.fileIsExist(path2War)) {
                    commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.WAR_MESSAGE_SUFFIX, "war_not_exist")));
                    return true;
                }
                YamlConfiguration configuration = SwUtil.loadPlugFile(path2War);
                List<String> locations = configuration.getStringList(KeyWord.WAR.LOCATIONS);
                locations.remove(index - 1);
                if (isReset) {
                    locations.add(locationStr);
                }
                scheduler.runTaskAsynchronously(spigot, () -> {
                    try {
                        configuration.save(new File(path2War));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

            } catch (Exception e) {
                commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.COMMON_MSG_SUFFIX, "exception")));
                SwUtil.logThrow(e);
            }
        }
        return false;
    }

    private boolean executeListLocationCommand(boolean isOp, CommandSender commandSender, String[] args, Plugin spigot) {
        if (args.length == 2 && KeyWord.WAR.LOC.equals(args[0])) {
            try {
                if (requrieOP(isOp, commandSender)) return true;
                String name = args[1];
                List<String> locations = getLocationsByName(commandSender, name);
                if (locations == null) return true;
                for (String location : locations) {
                    commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.GREEN + location));
                }

                return true;
            } catch (Exception e) {
                commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.COMMON_MSG_SUFFIX, "exception")));
                SwUtil.logThrow(e);
            }
        }
        return false;
    }

    @Nullable
    private static List<String> getLocationsByName(CommandSender commandSender, String name) throws Exception {
        String path2War = GlobalFactory.WAR_BASE_DIR + name + GlobalFactory.YML_PREFIX;
        if (!SwUtil.fileIsExist(path2War)) {
            commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.WAR_MESSAGE_SUFFIX, "war_not_exist")));
            return null;
        }
        YamlConfiguration configuration = SwUtil.loadPlugFile(path2War);
        List<String> locations = configuration.getStringList(KeyWord.WAR.LOCATIONS);
        return locations;
    }

    private boolean executeAddLocationCommand(boolean isOp, CommandSender commandSender, String[] args, Plugin spigot) {
        if (args.length == 4 && KeyWord.WAR.ADD.equals(args[1]) && KeyWord.WAR.LOC.equals(args[0])) {
            try {
                if (requrieOP(isOp, commandSender)) return true;
                String location = args[2];
                String name = args[3];

                String[] xyz = location.split(KeyWord.COMMON.COMMA);
                if (!SwUtil.checkLocation(xyz)) {
                    commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.WAR_MESSAGE_SUFFIX, "location_format_error")));
                    return true;
                }
                String path2War = GlobalFactory.WAR_BASE_DIR + name + GlobalFactory.YML_PREFIX;
                if (!SwUtil.fileIsExist(path2War)) {
                    commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.WAR_MESSAGE_SUFFIX, "war_not_exist")));
                    return true;
                }
                scheduler.runTaskAsynchronously(spigot, () -> {
                    try {
                        YamlConfiguration configuration = SwUtil.loadPlugFile(path2War);
                        List<String> locationList = configuration.getStringList(KeyWord.WAR.LOCATIONS);
                        locationList.add(location);
                        configuration.set(KeyWord.WAR.LOCATIONS, locationList);
                        configuration.save(new File(getPlugin().getDataFolder(), path2War));
                    } catch (Exception e) {
                        SwUtil.logThrow(e);
                    }
                });
            } catch (Exception e) {
                commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.COMMON_MSG_SUFFIX, "exception")));
                SwUtil.logThrow(e);
            }
            return true;
        }
        return false;
    }

    private boolean executeCreateCommand(boolean isOp, CommandSender commandSender, String[] args, Plugin spigot) {
        if (args.length == 4 && KeyWord.WAR.CREATE.equals(args[0])) {
            if (requrieOP(isOp, commandSender)) return true;
            String createName = args[1];
            String startTime = args[2];
            String endTime = args[3];
            if (createName.length() < 3 || createName.length() > 16) {
                commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.WAR_MESSAGE_SUFFIX, "name_limit")));
                return true;
            }
            if (!SwUtil.checkHourStr(startTime) && !SwUtil.checkHourStr(endTime) && !SwUtil.hourIsBefore(startTime, endTime)) {
                commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.WAR_MESSAGE_SUFFIX, "date_format_error")));
                return true;
            }
            String path2file = GlobalFactory.WAR_BASE_DIR + File.separator + createName + GlobalFactory.YML_PREFIX;
            if (SwUtil.fileIsExist(path2file)) {
                commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.WAR_MESSAGE_SUFFIX, "already_exist")));
                return true;
            }
            String playerName = commandSender.getName();
            Player player = spigot.getServer().getPlayer(playerName);
            String loc = "";
            String world = "";
            if (SwUtil.isNotNull(player)) {
                Location location = player.getLocation();
                double x = location.getX();
                // Use player stand loc.
                double y = location.getY() - 1;
                double z = location.getZ();
                loc = x + KeyWord.COMMON.WHITER_SPACE + y + KeyWord.COMMON.WHITER_SPACE + z;
                world = player.getWorld().getName();
            }
            final String worldName = world;
            final String locInfo = loc;
            scheduler.runTaskAsynchronously(spigot, () -> {
                try {
                    File file = SwUtil.createPluginFile(path2file);
                    YamlConfiguration yamlConfiguration = new YamlConfiguration();
                    yamlConfiguration.load(file);
                    String id = UUID.randomUUID().toString();
                    yamlConfiguration.set("UUID", id);
                    yamlConfiguration.set("name", createName);
                    yamlConfiguration.set("world", StringUtils.isNotEmpty(worldName) ? worldName : "world");
                    yamlConfiguration.set("locations", StringUtils.isNotEmpty(locInfo) ? Collections.singletonList(locInfo) : Collections.emptyList());
                    yamlConfiguration.set("start", startTime);
                    yamlConfiguration.set("end", endTime);
                    yamlConfiguration.set("days", Collections.emptyList());
                    yamlConfiguration.set("types", Collections.emptyList());
                    yamlConfiguration.set("rewards", SwUtil.convertMap(Reward.defVal()));
                    yamlConfiguration.save(file);
                } catch (Exception e) {
                    SwUtil.sendMessage2Sender(commandSender,
                            SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.WAR_MESSAGE_SUFFIX, "create_failed"),
                            ChatColor.RED
                    );
                    SwUtil.logThrow(e);
                }

                commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.GREEN + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.WAR_MESSAGE_SUFFIX, "create_success")));
            });
            return true;
        }
        return false;
    }

    private static boolean requrieOP(boolean isOp, CommandSender commandSender) {
        if (!isOp) {
            commandSender.sendMessage(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.COMMON_MSG_SUFFIX, "not_per"));
            return true;
        }
        return false;
    }

    private boolean executeReloadCommand(@NotNull CommandSender commandSender, @NotNull String @NotNull [] args, Plugin spigot) {
        if (args.length == 1 && KeyWord.COMMON.RELOAD.equals(args[0])) {
//            scheduler.runTaskAsynchronously(spigot, () -> {
//                spigot.saveConfig();
//                loadMessageYamlFile();
//                // load war file
//                try {
//                    new FlagWarManager(spigot).doManage();
//                } catch (Exception e) {
//                    SwUtil.logThrow(e);
//                }
//
//                // call the callback with the result
//                commandSender.spigot().sendMessage(TextComponent.fromLegacy(net.md_5.bungee.api.ChatColor.GREEN + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.COMMON_MSG_SUFFIX, "reloaded")));
//            });

            spigot.saveConfig();
            loadMessageYamlFile();
            // load war file
            try {
                new FlagWarManager(spigot).doManage();
            } catch (Exception e) {
                SwUtil.logThrow(e);
            }

            // call the callback with the result
            commandSender.spigot().sendMessage(TextComponent.fromLegacy(net.md_5.bungee.api.ChatColor.GREEN + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.COMMON_MSG_SUFFIX, "reloaded")));

            return true;
        }
        return false;
    }

    private boolean executeHelpCommand(boolean isOp, @NotNull String[] args, @NotNull CommandSender commandSender) {
        ConfigurationSection help = MESSAGE_YML_FILE.getConfigurationSection(GlobalFactory.WAR_MESSAGE_SUFFIX);
        ConfigurationSection common = MESSAGE_YML_FILE.getConfigurationSection(GlobalFactory.COMMON_MSG_SUFFIX);
        if (args.length == 1 && KeyWord.COMMON.HELP.equals(args[0])) {
            returnHelpMessage(commandSender, common, help, 1, isOp);
            return true;
        }
        if (args.length == 2 && KeyWord.COMMON.HELP.equals(args[0])) {
            String index = args[1];
            int indexInt = 0;
            try {
                indexInt = Integer.parseInt(index);
            } catch (NumberFormatException e) {
                commandSender.spigot().sendMessage(TextComponent.fromLegacy(ChatColor.RED + SwUtil.retMessage(MESSAGE_YML_FILE, GlobalFactory.COMMON_MSG_SUFFIX, "wrong_page")));
                return true;
            }
            returnHelpMessage(commandSender, common, help, indexInt, isOp);
            return true;
        }
        return false;
    }

    private static void returnHelpMessage(@NotNull CommandSender commandSender, ConfigurationSection common, ConfigurationSection help, Integer index, boolean isOp) {
        List<String> msg = help.getStringList(isOp ? KeyWord.COMMON.OP : KeyWord.COMMON.PLAYER);
        Long skip = index <= 1 ? 0 : (index - 1) * 10L;
        List<String> msgPage1 = msg.stream().skip(skip).limit(10).collect(Collectors.toList());
        StringBuffer bf = new StringBuffer();
        for (String str : msgPage1) {
            bf.append(ChatColor.GOLD + str).append("\n");
        }
        if (msg.size() > (10 * index) + 1) {
            String next = common.getString(KeyWord.COMMON.NEXT);
            String page = KeyWord.WAR.FW_HELP + KeyWord.COMMON.WHITER_SPACE + (index + 1);
            bf.append(ChatColor.GOLD + String.format(next, page));
        }
        commandSender.sendMessage(bf.toString());
    }
}
