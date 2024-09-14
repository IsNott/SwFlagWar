package org.nott.executor;

import lombok.Data;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * 悬赏命令-执行器
 * @author Nott
 * @date 2024-9-11
 */
@Data
public class OfferExecutor extends AbstractExecutor implements CommandExecutor {

    public OfferExecutor(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return false;
    }
}
