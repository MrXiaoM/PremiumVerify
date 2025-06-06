package top.mrxiaom.premiumverify;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import net.lenni0451.commons.httpclient.proxy.ProxyHandler;
import net.lenni0451.commons.httpclient.proxy.ProxyType;
import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.premiumverify.utils.PAPI;
import top.mrxiaom.premiumverify.utils.Util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class Main extends JavaPlugin implements Listener, TabCompleter {
    private static Main instance;

    public static Main getInstance() {
        return instance;
    }
    protected Map<String, Request> players = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    protected PlayersData data;
    boolean hasPAPI;
    private String proxyType;
    private String proxyHost;
    private int proxyPort;
    private @Nullable String proxyUser;
    private @Nullable String proxyPass;
    protected int verifyTimesLimit;
    protected int failTimesLimit;
    protected int timeout;
    protected String alreadyVerifiedPermission;
    protected List<String> rewards;
    private boolean hideStacktrace;
    private final FoliaLib foliaLib;
    public Main() {
        foliaLib = new FoliaLib(this);
    }

    public PlatformScheduler getScheduler() {
        return foliaLib.getScheduler();
    }

    @Override
    public void onEnable() {
        instance = this;
        hasPAPI = PAPI.init();
        if (hasPAPI) {
            Placeholders papi = new Placeholders(this);
            try {
                papi.unregister();
            } catch (Throwable ignored) {
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
        foliaLib.getScheduler().cancelAllTasks();
    }

    @Nullable
    public ProxyHandler getProxy() {
        ProxyType type;
        switch (proxyType.toLowerCase()) {
            case "http":
                type = ProxyType.HTTP;
                break;
            case "socks4":
                type = ProxyType.SOCKS4;
                break;
            case "socks5":
                type = ProxyType.SOCKS5;
                break;
            default:
                return null;
        }
        return new ProxyHandler(type, proxyHost, proxyPort, proxyUser, proxyPass);
    }

    @Override
    public void reloadConfig() {
        super.saveDefaultConfig();
        super.reloadConfig();

        FileConfiguration config = getConfig();

        proxyType = config.getString("proxy.type", "none");
        proxyHost = config.getString("proxy.host", "127.0.0.1");
        proxyPort = config.getInt("proxy.port", 8081);
        proxyUser = config.getString("proxy.user", null);
        proxyPass = config.getString("proxy.pass", null);

        verifyTimesLimit = config.getInt("verify-times-limit", 1);
        failTimesLimit = config.getInt("fail-times-limit", 3);
        timeout = config.getInt("timeout", 300);
        alreadyVerifiedPermission = config.getString("already-verified-permission");
        rewards = config.getStringList("rewards");
        hideStacktrace = config.getBoolean("hide-stacktrace", false);

        Lang.reload(config.getConfigurationSection("messages"));

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
        warn(t, true);
    }
    public void warn(Throwable t, boolean withCause) {
        if (hideStacktrace && t instanceof IOException) {
            getLogger().warning(t.toString());
            return;
        }
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            if (withCause) t.printStackTrace(pw);
            else Util.printWithoutCause(t, pw);
        }
        getLogger().warning(sw.toString());
    }
}
