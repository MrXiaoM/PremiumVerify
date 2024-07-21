package top.mrxiaom.premiumverify;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.premiumverify.utils.PAPI;
import top.mrxiaom.premiumverify.utils.Util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static top.mrxiaom.premiumverify.utils.ColorHelper.t;

public class Main extends JavaPlugin implements Listener, TabCompleter {
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
    protected List<String> msgResultCallOP;
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

        new Commands(this);
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
        msgResultCallOP = config.getStringList("messages.result-call-op");

        data.reload();
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
