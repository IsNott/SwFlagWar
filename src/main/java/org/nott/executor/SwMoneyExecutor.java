//package org.nott.executor;
//
//import lombok.Data;
//import net.md_5.bungee.api.chat.BaseComponent;
//import net.md_5.bungee.api.chat.TextComponent;
//import net.milkbowl.vault.economy.Economy;
//import net.milkbowl.vault.economy.EconomyResponse;
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import org.bukkit.permissions.PermissionAttachmentInfo;
//import org.bukkit.plugin.Plugin;
//import org.jetbrains.annotations.NotNull;
//import org.nott.global.KeyWord;
//import org.nott.utils.SwUtil;
//
//import java.util.Set;
//import java.util.logging.Logger;
//
//@Data
//public class SwMoneyExecutor extends AbstractExecutor implements CommandExecutor {
//
//    public SwMoneyExecutor(Plugin plugin) {
//        super(plugin);
//    }
//
//    static Economy econ = Bukkit.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
//
//    @Override
//    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//        Plugin plugin = getPlugin();
//        Logger logger = plugin.getLogger();
//        Player player = (Player) sender;
//
//        boolean payResult = executePayCommand(sender, args, plugin, player);
//
//        boolean balResult = executeBalCommand(plugin,sender,player, args);
//
//        boolean setResult = executeSetCommand(sender,args,plugin);
//
//        return payResult || balResult || setResult;
//    }
//
//    private boolean executeSetCommand(CommandSender sender, String[] args, Plugin plugin) {
//        if(args.length == 3 && ("set".equals(args[0]) || "add".equals(args[0]))) {
//            Set<PermissionAttachmentInfo> effectivePermissions = sender.getEffectivePermissions();
//            PermissionAttachmentInfo permissionAttachmentInfo = effectivePermissions
//                    .stream()
//                    .filter(r -> KeyWord.PERMISSION.MONEY_OP_PERM.equals(r.getPermission()))
//                    .findAny()
//                    .orElse(null);
//            boolean isOp = permissionAttachmentInfo != null;
//            if (!isOp) {
//                SwUtil.sendMessage2Sender(sender, SwUtil.retMessage(MESSAGE_YML_FILE, "common.not_per"), ChatColor.DARK_RED);
//                return true;
//            }
//            String playName = args[1];
//            Player player = plugin.getServer().getPlayer(playName);
//            if (SwUtil.isNull(player)) {
//                SwUtil.sendMessage2Sender(sender, String.format(SwUtil.retMessage(MESSAGE_YML_FILE, "money.player_not_found"), playName), ChatColor.RED);
//                return true;
//            }
//            double fee = 0.00d;
//            try {
//                fee = Double.parseDouble(args[2]);
//            } catch (NumberFormatException e) {
//                SwUtil.sendMessage2Sender(sender, SwUtil.retMessage(MESSAGE_YML_FILE, "money.number_format_error"), ChatColor.RED);
//                return true;
//            }
//            EconomyResponse response = null;
//            switch (args[0]){
//                default: break;
//                case "add" : {
//                    response = econ.depositPlayer(player,fee);
//                    break;
//                }
//                case "set" : {
//                    double balance = econ.getBalance(player);
//                    econ.withdrawPlayer(player,balance);
//                    response = econ.depositPlayer(player,fee);
//                    break;
//                }
//            }
//            if(response.transactionSuccess()){
//                SwUtil.sendMessage2Sender(sender, String.format(SwUtil.retMessage(MESSAGE_YML_FILE, "money.operation_success"),player.getName(),response.balance), ChatColor.RED);
//            }else {
//                SwUtil.sendMessage2Sender(sender, SwUtil.retMessage(MESSAGE_YML_FILE, "common.exception"), ChatColor.RED);
//                SwUtil.log(response.errorMessage);
//            }
//            return true;
//        }
//        return false;
//    }
//
//    private static boolean executeBalCommand(Plugin plugin, @NotNull CommandSender sender, Player player, @NotNull String[] args) {
//        if (("balance".equals(args[0]) || "bal".equals(args[0]))) {
//            String bankName = AbstractExecutor.MESSAGE_YML_FILE.getString("money.bank_name");
//            String bal = AbstractExecutor.MESSAGE_YML_FILE.getString("money.bal");
//            boolean isQuerySelf = args.length == 1;
//            double balance = 0.00d;
//            BaseComponent per = TextComponent.fromLegacy(ChatColor.GOLD + bankName);
//            Player query = null;
//            if(isQuerySelf){
//                query = (Player) sender;
//            }else {
//                String name = args[1];
//                query = plugin.getServer().getPlayer(name);
//                if (SwUtil.isNull(query)) {
//                    SwUtil.sendMessage2Sender(sender, String.format(SwUtil.retMessage(MESSAGE_YML_FILE, "money.player_not_found"), name), ChatColor.RED);
//                    return true;
//                }
//            }
//            balance = econ.getBalance(query);
//            BaseComponent suf = TextComponent.fromLegacy(ChatColor.GREEN + String.format(bal, sender.getName(), balance));
//            player.spigot().sendMessage(per, suf);
//            return true;
//        }
//        return false;
//    }
//
//    private static boolean executePayCommand(@NotNull CommandSender sender, @NotNull String @NotNull [] args, Plugin plugin, Player player) {
//        if (args.length == 3 && "pay".equals(args[0])) {
//            String playerName = args[1];
//            String fee = args[2];
//            Player toPay = plugin.getServer().getPlayer(playerName);
//            double amount = 0;
//            try {
//                amount = Double.parseDouble(fee);
//            } catch (NumberFormatException e) {
//                SwUtil.sendMessage2Sender(sender, SwUtil.retMessage(MESSAGE_YML_FILE, "money.number_format_error"), ChatColor.RED);
//                return true;
//            }
//            if (SwUtil.isNull(toPay)) {
//                SwUtil.sendMessage2Sender(sender, String.format(SwUtil.retMessage(MESSAGE_YML_FILE, "money.player_not_found"), playerName), ChatColor.RED);
//                return true;
//            }
//            if (econ.has(player, amount)) {
//                EconomyResponse r = econ.withdrawPlayer(player, amount);
//                EconomyResponse economyResponse = econ.depositPlayer(toPay, amount);
//                if (r.transactionSuccess() && economyResponse.transactionSuccess()) {
//                    SwUtil.sendMessage2Sender(sender, String.format(SwUtil.retMessage(MESSAGE_YML_FILE, "money.payed_msg"), econ.format(r.amount), econ.format(r.balance)), ChatColor.DARK_GREEN);
//                    SwUtil.spigotTextMessage(toPay.spigot(),
//                            String.format(SwUtil.retMessage(MESSAGE_YML_FILE, "money.received_msg"), player, economyResponse.amount),
//                            ChatColor.DARK_GREEN);
//                } else {
//                    sender.sendMessage(SwUtil.retMessage(MESSAGE_YML_FILE, "common.exception"));
//                    SwUtil.log(r.errorMessage);
//                    SwUtil.log(economyResponse.errorMessage);
//                }
//            } else {
//                SwUtil.sendMessage2Sender(sender, SwUtil.retMessage(MESSAGE_YML_FILE, "money.not_enough"), ChatColor.RED);
//            }
//            return true;
//        }
//        return false;
//    }
//}
