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
                return t(sender, Lang.commands_reload);
            }
            if (args[0].equalsIgnoreCase("request") && sender.hasPermission("premiumverify.request")) {
                if (!(sender instanceof Player)) {
                    return t(sender, Lang.error__only_player);
                }
                Player player = (Player) sender;
                if (!sender.hasPermission("premiumverify.request.bypass-fail-limit")) {
                    if (plugin.data.getPlayerFailTimes(player.getName()) >= plugin.failTimesLimit) {
                        return t(player, Lang.error__fail_limit);
                    }
                }
                if (plugin.players.containsKey(player.getName())) {
                    return t(player, Lang.error__already_in_verify);
                }
                if (plugin.data.isPlayerVerified(player.getName()) || (!plugin.alreadyVerifiedPermission.isEmpty() && player.hasPermission(plugin.alreadyVerifiedPermission))) {
                    return t(player, Lang.error__already_verified);
                }
                plugin.players.put(player.getName(), Request.create(plugin, player));
                return true;
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("fail") && sender.hasPermission("premiumverify.fail")) {
                if (!plugin.data.hasPlayer(args[1])) {
                    return t(sender, Lang.error__no_player);
                }
                int times = Util.parseInt(args[2]).orElse(-1);
                if (times < 0) {
                    return t(sender, Lang.error__no_integer);
                }
                plugin.data.markPlayerFail(args[1], times);
                return t(sender, Lang.commands_fail.str().replace("%player%", args[1]).replace("%times%", String.valueOf(times)));
            }
        }
        return t(sender, sender.isOp() ? Lang.help_op : Lang.help);
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
