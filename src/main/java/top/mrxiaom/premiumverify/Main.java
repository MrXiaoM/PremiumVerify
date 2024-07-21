package top.mrxiaom.premiumverify;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.premiumverify.utils.PAPI;
import top.mrxiaom.premiumverify.utils.Util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static top.mrxiaom.premiumverify.utils.ColorHelper.t;

public class Main extends JavaPlugin implements Listener {
    private static Main instance;

    public static Main getInstance() {
        return instance;
    }
    protected Map<String, Request> players = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    protected PlayersData data;
    boolean hasPAPI;
    protected List<String> msgHelp;
    protected List<String> msgHelpOp;
    protected String msgCmdReload;
    protected String msgCmdFail;
    protected String msgErrOnlyPlayer;
    protected String msgErrNoPlayer;
    protected String msgErrNoInteger;
    protected List<String> msgErrFailLimit;
    protected List<String> msgErrAlreadyInVerify;
    protected List<String> msgErrAlreadyVerified;
    protected List<String> msgVerifyStart;
    protected List<String> msgVerify;
    protected String msgLinkText;
    protected List<String> msgLinkHover;
    protected List<String> msgResultExpired;
    protected List<String> msgResultNotMatch;
    protected List<String> msgResultLimit;
    protected int verifyTimesLimit;
    protected int failTimesLimit;
    protected int timeout;
    protected String alreadyVerifiedPermission;
    protected List<String> rewards;
    @Override
    public void onEnable() {
        instance = this;
        hasPAPI = PAPI.init();
        if (hasPAPI) {
            Placeholders papi = new Placeholders(this);
            LocalExpansionManager manager = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager();
            Collection<PlaceholderExpansion> expansions = manager.getExpansions();
            for (PlaceholderExpansion expansion : new ArrayList<>(expansions)) {
                if (expansion.getIdentifier().equals(papi.getIdentifier())) {
                    expansion.unregister();
                }
            }
            papi.register();
        }
        data = new PlayersData(this);
        reloadConfig();

        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("PremiumVerify 插件已启用");
    }

    @Override
    public void onDisable() {
        for (Request request : players.values()) {
            request.cancel();
        }
        players.clear();
    }

    @Override
    public void reloadConfig() {
        super.saveDefaultConfig();
        super.reloadConfig();

        FileConfiguration config = getConfig();
        verifyTimesLimit = config.getInt("verify-times-limit", 1);
        failTimesLimit = config.getInt("fail-times-limit", 3);
        timeout = config.getInt("timeout", 300);
        alreadyVerifiedPermission = config.getString("already-verified-permission");
        rewards = config.getStringList("rewards");

        msgHelp = config.getStringList("messages.help");
        msgHelpOp = config.getStringList("messages.help-op");
        msgCmdReload = config.getString("messages.commands.reload");
        msgCmdFail = config.getString("messages.commands.fail");
        msgErrOnlyPlayer = config.getString("messages.error.only-player");
        msgErrNoPlayer = config.getString("messages.error.no-player");
        msgErrNoInteger = config.getString("messages.error.no-integer");
        msgErrFailLimit = config.getStringList("messages.error.fail-limit");
        msgErrAlreadyInVerify = config.getStringList("messages.error.already-in-verify");
        msgErrAlreadyVerified = config.getStringList("messages.error.already-verified");
        msgVerifyStart = config.getStringList("messages.verify-start");
        msgVerify = config.getStringList("messages.verify");
        msgLinkText = config.getString("messages.link-text");
        msgLinkHover = config.getStringList("messages.link-hover");
        msgResultExpired = config.getStringList("messages.result-expired");
        msgResultNotMatch = config.getStringList("messages.result-not-match");
        msgResultLimit = config.getStringList("messages.result-limit");

        data.reload();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("premiumverify.reload")) {
                reloadConfig();
                return t(sender, msgCmdReload);
            }
            if (args[0].equalsIgnoreCase("request") && sender.hasPermission("premiumverify.request")) {
                if (!(sender instanceof Player)) {
                    return t(sender, msgErrOnlyPlayer);
                }
                Player player = (Player) sender;
                if (!sender.hasPermission("premiumverify.request.bypass-fail-limit")) {
                    if (data.getPlayerFailTimes(player.getName()) >= failTimesLimit) {
                        return t(player, msgErrFailLimit);
                    }
                }
                if (players.containsKey(player.getName())) {
                    return t(player, msgErrAlreadyInVerify);
                }
                if (data.isPlayerVerified(player.getName()) || (!alreadyVerifiedPermission.isEmpty() && player.hasPermission(alreadyVerifiedPermission))) {
                    return t(player, msgErrAlreadyVerified);
                }
                players.put(player.getName(), Request.create(this, player));
                return true;
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("fail") && sender.hasPermission("premiumverify.fail")) {
                if (!data.hasPlayer(args[1])) {
                    return t(sender, msgErrNoPlayer);
                }
                int times = Util.parseInt(args[2]).orElse(-1);
                if (times < 0) {
                    return t(sender, msgErrNoInteger);
                }
                data.markPlayerFail(args[1], times);
                return t(sender, msgCmdFail.replace("%player%", args[1]).replace("%times%", String.valueOf(times)));
            }
        }
        if (sender.isOp()) {
            return t(sender, msgHelpOp);
        } else {
            return t(sender, msgHelp);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Request request = players.remove(e.getPlayer().getName());
        if (request != null) {
            request.cancel();
        }
    }

    public void warn(Throwable t) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
        }
        getLogger().warning(sw.toString());
    }
}
