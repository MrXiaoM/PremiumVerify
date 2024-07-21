package top.mrxiaom.premiumverify;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.premiumverify.utils.Util;

import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.premiumverify.utils.ColorHelper.t;

public class Commands implements CommandExecutor, TabCompleter {
    Main plugin;
    public Commands(Main plugin) {
        this.plugin = plugin;
        PluginCommand command = plugin.getCommand("premiumverify");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("premiumverify.reload")) {
                plugin.reloadConfig();
                return t(sender, plugin.msgCmdReload);
            }
            if (args[0].equalsIgnoreCase("request") && sender.hasPermission("premiumverify.request")) {
                if (!(sender instanceof Player)) {
                    return t(sender, plugin.msgErrOnlyPlayer);
                }
                Player player = (Player) sender;
                if (!sender.hasPermission("premiumverify.request.bypass-fail-limit")) {
                    if (plugin.data.getPlayerFailTimes(player.getName()) >= plugin.failTimesLimit) {
                        return t(player, plugin.msgErrFailLimit);
                    }
                }
                if (plugin.players.containsKey(player.getName())) {
                    return t(player, plugin.msgErrAlreadyInVerify);
                }
                if (plugin.data.isPlayerVerified(player.getName()) || (!plugin.alreadyVerifiedPermission.isEmpty() && player.hasPermission(plugin.alreadyVerifiedPermission))) {
                    return t(player, plugin.msgErrAlreadyVerified);
                }
                plugin.players.put(player.getName(), Request.create(plugin, player));
                return true;
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("fail") && sender.hasPermission("premiumverify.fail")) {
                if (!plugin.data.hasPlayer(args[1])) {
                    return t(sender, plugin.msgErrNoPlayer);
                }
                int times = Util.parseInt(args[2]).orElse(-1);
                if (times < 0) {
                    return t(sender, plugin.msgErrNoInteger);
                }
                plugin.data.markPlayerFail(args[1], times);
                return t(sender, plugin.msgCmdFail.replace("%player%", args[1]).replace("%times%", String.valueOf(times)));
            }
        }
        if (sender.isOp()) {
            return t(sender, plugin.msgHelpOp);
        } else {
            return t(sender, plugin.msgHelp);
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            String arg0 = args[0].toLowerCase();
            if ("request".startsWith(arg0) && sender.hasPermission("premiumverify.request")) list.add("request");
            if ("fail".startsWith(arg0) && sender.hasPermission("premiumverify.fail")) list.add("fail");
            if ("reload".startsWith(arg0) && sender.hasPermission("premiumverify.reload")) list.add("reload");
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("fail") && sender.hasPermission("premiumverify.fail")) return null;
        }
        return list;
    }
}
