package top.mrxiaom.premiumverify;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import top.mrxiaom.premiumverify.utils.PAPI;

public class PremiumVerify extends JavaPlugin {
    private static PremiumVerify instance;

    public static PremiumVerify getInstance() {
        return instance;
    }

    boolean hasPAPI;
    @Override
    public void onEnable() {
        instance = this;
        hasPAPI = PAPI.init();
        reloadConfig();
    }

    @Override
    public void reloadConfig() {
        super.saveDefaultConfig();
        super.reloadConfig();
        FileConfiguration config = getConfig();
    }
}
