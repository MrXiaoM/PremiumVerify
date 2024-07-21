package top.mrxiaom.premiumverify;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.premiumverify.utils.PAPI;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static top.mrxiaom.premiumverify.utils.ColorHelper.t;

public class PremiumVerify extends JavaPlugin implements Listener {
    private static PremiumVerify instance;

    public static PremiumVerify getInstance() {
        return instance;
    }
    protected Map<String, VerifyRequest> players = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    boolean hasPAPI;
    protected List<String> msgHelp;
    protected List<String> msgHelpOp;
    protected List<String> msgVerifyStart;
    protected List<String> msgVerify;
    protected String msgLinkText;
    protected List<String> msgLinkHover;
    protected List<String> msgResultExpired;
    protected int verifyTimesLimit;
    protected int failTimesLimit;
    protected int timeout;
    @Override
    public void onEnable() {
        instance = this;
        hasPAPI = PAPI.init();
        reloadConfig();

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        for (VerifyRequest request : players.values()) {
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

        msgHelp = config.getStringList("messages.help");
        msgHelpOp = config.getStringList("messages.help-op");
        msgVerifyStart = config.getStringList("messages.verify-start");
        msgVerify = config.getStringList("messages.verify");
        msgLinkText = config.getString("messages.link-text");
        msgLinkHover = config.getStringList("messages.link-hover");
        msgResultExpired = config.getStringList("messages.result-expired");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload") && sender.isOp()) {
                reloadConfig();
                return t(sender, "&a配置文件已重载");
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
        VerifyRequest request = players.remove(e.getPlayer().getName());
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
