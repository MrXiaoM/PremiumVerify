package top.mrxiaom.premiumverify.utils;


import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PAPI {
    private static boolean isEnable = false;
    public static boolean init() {
        return isEnable = Util.isPresent("me.clip.placeholderapi.PlaceholderAPI");
    }

    public static String parse(Player player, String s) {
        if (!isEnable) return s.replace("%player_name%", player.getName());
        return PlaceholderAPI.setPlaceholders(player, s);
    }
}
