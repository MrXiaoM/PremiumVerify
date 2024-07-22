package top.mrxiaom.premiumverify;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import top.mrxiaom.premiumverify.utils.PAPI;
import top.mrxiaom.premiumverify.utils.Util;

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
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            if (withCause) t.printStackTrace(pw);
            else Util.printWithoutCause(t, pw);
        }
        getLogger().warning(sw.toString());
    }
}
