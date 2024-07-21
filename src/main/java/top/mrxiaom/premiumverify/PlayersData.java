package top.mrxiaom.premiumverify;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class PlayersData {
    File file;
    YamlConfiguration config;
    PremiumVerify plugin;
    public PlayersData(PremiumVerify plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    public int getPlayerVerifyTimes(String uuid) {
        return config.getInt("uuid." + uuid + ".verify-times", 0);
    }

    public int getPlayerFailTimes(String name) {
        return config.getInt("offline-players." + name.toLowerCase() + ".fail-times", 0);
    }

    public boolean isPlayerVerified(String name) {
        return config.contains("offline-players." + name.toLowerCase() + ".premium-uuid");
    }

    public void markPlayerVerified(String name, String uuid) {
        int times = getPlayerVerifyTimes(uuid);
        config.set("uuid." + uuid + ".verify-times", times + 1);
        config.set("offline-players." + name.toLowerCase() + ".premium-uuid", uuid);
        save();
    }

    public void markPlayerFail(String name) {
        markPlayerFail(name, -1);
    }
    public void markPlayerFail(String name, int override) {
        int times = override < 0 ? (getPlayerFailTimes(name) + 1) : override;
        config.set("offline-players." + name.toLowerCase() + ".fail-times", times);
        save();
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.warn(e);
        }
    }
}
