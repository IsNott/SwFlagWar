package org.nott.executor;

import lombok.Data;
import org.bukkit.Bukkit;
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
import org.nott.asyn.callback.SwCallBack;
import org.nott.global.GlobalFactory;
import org.nott.global.KeyWord;
import org.nott.utils.SwUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Nott
 * @date 2024-9-9
 */
@Data
public class FlagWarExecutor implements CommandExecutor {

    private Plugin plugin;

    public static YamlConfiguration messageFile;

    public static final String WAR_BASE_DIR = "wars" + File.separator;

    public static final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

    static {
        loadMessageYamlFile();
    }

    private static void loadMessageYamlFile() {
        Plugin worldFlagWar = Bukkit.getPluginManager().getPlugin(GlobalFactory.PLUGIN_NAME);
        Objects.requireNonNull(worldFlagWar);
        File dataFolder = worldFlagWar.getDataFolder();
        File file = new File(dataFolder, GlobalFactory.MESSAGE_YML);
        if (!file.exists()) {
            worldFlagWar.saveResource(GlobalFactory.MESSAGE_YML, false);
        }
        File msgFile = new File(dataFolder, GlobalFactory.MESSAGE_YML);
        messageFile = YamlConfiguration.loadConfiguration(msgFile);
    }

    public FlagWarExecutor(Plugin plugin) {
        this.plugin = plugin;
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

        // Handle 'fw reload' Command
        boolean reloadCommand = executeReloadCommand(commandSender, args, spigot);

        // Handle 'fw create' Command
        boolean createCommand = executeCreateCommand(isOp, commandSender, args, spigot);

        // Handle 'fw add location' Command
        boolean addLocationCommand = executeAddLocationCommand(isOp, commandSender, args, spigot);

        // Handle 'fw list location' Command
        boolean listLocationCommand = executeListLocationCommand(isOp, commandSender, args, spigot);

        // Handle 'fw list remove/reset' Command
        boolean setOrRemoveLocationCommand = executeSetOrRemoveLocationCommand(isOp, commandSender, args, spigot);


        return helpCommand || reloadCommand || createCommand || addLocationCommand || listLocationCommand || setOrRemoveLocationCommand;
    }

    private boolean executeSetOrRemoveLocationCommand(boolean isOp, CommandSender commandSender, String[] args, Plugin spigot) {
        if (args.length > 2 && (KeyWord.WAR.REMOVE.equals(args[1]) || KeyWord.WAR.RESET.equals(args[1])) && KeyWord.WAR.LOC.equals(args[0])) {
            if(requrieOP(isOp,commandSender))return true;
            try {
                String indexStr = args[3];
                String locationStr = args[4];
                String[] xyz = locationStr.split(",");
                int index = Integer.parseInt(indexStr);
                if (!SwUtil.checkLocation(xyz)) {
                    commandSender.sendMessage(SwUtil.retMessage(messageFile, GlobalFactory.WAR_MESSAGE_SUFFIX, "location_format_error"));
                    return true;
                }
                if (index < 1) {
                    commandSender.sendMessage(SwUtil.retMessage(messageFile, GlobalFactory.COMMON_MSG_SUFFIX, "index_format_error"));
                    return true;
                }
                String name = args[2];
                boolean isReset = KeyWord.WAR.RESET.equals(args[1]);
                String path2War = WAR_BASE_DIR + name + GlobalFactory.YML_PREFIX;
                if (!SwUtil.fileIsExist(path2War)) {
                    commandSender.sendMessage(SwUtil.retMessage(messageFile, GlobalFactory.WAR_MESSAGE_SUFFIX, "war_not_exist"));
                    return true;
                }
                YamlConfiguration configuration = SwUtil.loadPlugFile(path2War);
                List<String> locations = configuration.getStringList("locations");
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
                commandSender.sendMessage(SwUtil.retMessage(messageFile, GlobalFactory.COMMON_MSG_SUFFIX, "exception"));
                SwUtil.LogThrow(e);
            }
        }
        return false;
    }

    private boolean executeListLocationCommand(boolean isOp, CommandSender commandSender, String[] args, Plugin spigot) {
        if (args.length == 3 && KeyWord.WAR.LIST.equals(args[1]) && KeyWord.WAR.LOC.equals(args[0])) {
            try {
                if (requrieOP(isOp, commandSender)) return true;
                String name = args[2];
                String path2War = WAR_BASE_DIR + name + GlobalFactory.YML_PREFIX;
                if (!SwUtil.fileIsExist(path2War)) {
                    commandSender.sendMessage(SwUtil.retMessage(messageFile, GlobalFactory.WAR_MESSAGE_SUFFIX, "war_not_exist"));
                    return true;
                }
                YamlConfiguration configuration = SwUtil.loadPlugFile(path2War);
                List<String> locations = configuration.getStringList("locations");
                commandSender.sendMessage(locations.toArray(new String[]{}));
                return true;
            } catch (Exception e) {
                commandSender.sendMessage(SwUtil.retMessage(messageFile, GlobalFactory.COMMON_MSG_SUFFIX, "exception"));
                SwUtil.LogThrow(e);
            }
        }
        return false;
    }

    private boolean executeAddLocationCommand(boolean isOp, CommandSender commandSender, String[] args, Plugin spigot) {
        if (args.length == 4 && KeyWord.WAR.ADD.equals(args[0]) && KeyWord.WAR.LOC.equals(args[1])) {
            try {
                if (requrieOP(isOp, commandSender)) return true;
                String location = args[2];
                String name = args[3];

                String[] xyz = location.split(",");
                if (!SwUtil.checkLocation(xyz)) {
                    commandSender.sendMessage(SwUtil.retMessage(messageFile, GlobalFactory.WAR_MESSAGE_SUFFIX, "location_format_error"));
                    return true;
                }
                String path2War = WAR_BASE_DIR + name + GlobalFactory.YML_PREFIX;
                if (!SwUtil.fileIsExist(path2War)) {
                    commandSender.sendMessage(SwUtil.retMessage(messageFile, GlobalFactory.WAR_MESSAGE_SUFFIX, "war_not_exist"));
                    return true;
                }
                scheduler.runTaskAsynchronously(spigot, () -> {
                    try {
                        YamlConfiguration configuration = SwUtil.loadPlugFile(path2War);
                        List<String> locationList = configuration.getStringList(location);
                        locationList.add(location);
                        configuration.set(KeyWord.WAR.LOCATIONS,locationList);
                        configuration.save(new File(path2War));
                    } catch (Exception e) {
                        SwUtil.LogThrow(e);
                    }
                });
            } catch (Exception e) {
                commandSender.sendMessage(SwUtil.retMessage(messageFile, GlobalFactory.COMMON_MSG_SUFFIX, "exception"));
                SwUtil.LogThrow(e);
            }
            return true;
        }
        return false;
    }

    private boolean executeCreateCommand(boolean isOp, CommandSender commandSender, String[] args, Plugin spigot) {
        if (args.length == 2 && KeyWord.WAR.CREATE.equals(args[0])) {
            if (requrieOP(isOp, commandSender)) return true;
            String createName = args[1];
            if (createName.length() < 3 || createName.length() > 16) {
                commandSender.sendMessage(SwUtil.retMessage(messageFile, GlobalFactory.WAR_MESSAGE_SUFFIX, "name_limit"));
                return true;
            }
            String path2file = WAR_BASE_DIR + File.separator + createName + GlobalFactory.YML_PREFIX;
            if (SwUtil.fileIsExist(path2file)) {
                commandSender.sendMessage(SwUtil.retMessage(messageFile, GlobalFactory.WAR_MESSAGE_SUFFIX, "already_exist"));
                return true;
            }

            scheduler.runTaskAsynchronously(spigot, () -> {
                try {
                    File file = SwUtil.createPluginFile(path2file);
                    YamlConfiguration yamlConfiguration = new YamlConfiguration();
                    yamlConfiguration.load(file);
                    String id = UUID.randomUUID().toString();
                    yamlConfiguration.set("UUID", id);
                    yamlConfiguration.set("name", createName);
                    yamlConfiguration.set("world", "world");
                    yamlConfiguration.set("locations", Collections.emptyList());
                    yamlConfiguration.set("start", "");
                    yamlConfiguration.set("end", "");
                    yamlConfiguration.set("days", Collections.emptyList());
                    yamlConfiguration.set("types", Collections.emptyList());
                    yamlConfiguration.set("rewards", "");
                    yamlConfiguration.save(file);
                } catch (Exception e) {
                    plugin.getLogger().info("Create FlagWar failed");
                    SwUtil.LogThrow(e);
                }

                scheduler.runTask(spigot, () -> {
                    new SwCallBack() {
                        @Override
                        public void returnMessage(String str) {
                            Player player = plugin.getServer().getPlayer(commandSender.getName());
                            player.sendMessage(SwUtil.retMessage(messageFile, GlobalFactory.WAR_MESSAGE_SUFFIX, "create_success"));
                        }
                    };
                });
            });
            return true;
        }
        return false;
    }

    private static boolean requrieOP(boolean isOp, CommandSender commandSender) {
        if (!isOp) {
            commandSender.sendMessage(SwUtil.retMessage(messageFile, GlobalFactory.COMMON_MSG_SUFFIX, "not_per"));
            return true;
        }
        return false;
    }

    private boolean executeReloadCommand(@NotNull CommandSender commandSender, @NotNull String @NotNull [] args, Plugin spigot) {
        if (args.length == 1 && KeyWord.COMMON.RELOAD.equals(args[0])) {
            scheduler.runTaskAsynchronously(spigot, () -> {
                spigot.saveConfig();
                loadMessageYamlFile();
                //todo load war file

                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        // call the callback with the result
                        commandSender.sendMessage(SwUtil.retMessage(messageFile, GlobalFactory.COMMON_MSG_SUFFIX, "reloaded"));
                    }
                });
            });

            return true;
        }
        return false;
    }

    private Boolean executeHelpCommand(boolean isOp, @NotNull String[] args, @NotNull CommandSender commandSender) {
        ConfigurationSection help = messageFile.getConfigurationSection(GlobalFactory.WAR_MESSAGE_SUFFIX);
        ConfigurationSection common = messageFile.getConfigurationSection(GlobalFactory.COMMON_MSG_SUFFIX);
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
                commandSender.sendMessage(SwUtil.retMessage(messageFile, GlobalFactory.COMMON_MSG_SUFFIX, "wrong_page"));
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
            bf.append(str).append("\n");
        }
        if (msg.size() > (10 * index) + 1) {
            String next = common.getString(KeyWord.COMMON.NEXT);
            String page = " /fw help " + (index + 1);
            bf.append(String.format(next, page));
        }
        commandSender.sendMessage(bf.toString());
    }
}
