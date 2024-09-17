package org.nott.executor;

import lombok.Data;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

@Data
public class SwMoneyExecutor extends AbstractExecutor implements CommandExecutor {

    public SwMoneyExecutor(Plugin plugin) {
        super(plugin);
    }

    static Economy econ = Bukkit.getServer().getServicesManager().getRegistration(Economy.class).getProvider();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Plugin plugin = getPlugin();
        Logger logger = plugin.getLogger();
        if(!(sender instanceof Player)) {
            logger.info("Only players are supported for this Example Plugin, but you should not do this!!!");
            return true;
        }
        Player player = (Player) sender;
        if (command.getLabel().equals("test-economy")) {
            // Lets give the player 1.05 currency (note that SOME economic plugins require rounding!)
            sender.sendMessage(String.format("You have %s", econ.format(econ.getBalance(player.getName()))));
            EconomyResponse r = econ.depositPlayer(player, 1.05);
            if (r.transactionSuccess()) {
                sender.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
            } else {
                sender.sendMessage(String.format("An error occured: %s", r.errorMessage));
            }
            return true;
        } else if(("balance".equals(args[0]) || "bal".equals(args[0]))){
            String bankName = AbstractExecutor.MESSAGE_YML_FILE.getString("money.bank_name");
            String bal = AbstractExecutor.MESSAGE_YML_FILE.getString("money.bal");
            double balance = econ.getBalance(player);
            BaseComponent per = TextComponent.fromLegacy(bankName, ChatColor.GOLD);
            BaseComponent suf = TextComponent.fromLegacy(String.format(bal, balance), ChatColor.GREEN);
            player.spigot().sendMessage(per,suf);
            return true;
        }
        else {
            return false;
        }
    }
}
