package top.mrxiaom.premiumverify.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.mrxiaom.premiumverify.PremiumVerify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    public static void runCommands(Player player, List<String> commands) {
        runCommands(player, commands, 0);
    }
    public static void runCommands(Player player, List<String> commands, int startIndex) {
        for (int i = startIndex; i < commands.size(); i++) {
            String s = ColorHelper.parseColor(PAPI.parse(player, commands.get(i)));
            if (s.startsWith("[console]")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), removePrefix(s, "[console]").trim());
                continue;
            }
            if (s.startsWith("[player]")) {
                Bukkit.dispatchCommand(player, removePrefix(s, "[player]").trim());
                continue;
            }
            if (s.startsWith("[message]")) {
                player.sendMessage(removePrefix(s, "[message]").trim());
                continue;
            }
            if (s.startsWith("[broadcast]")) {
                String msg = removePrefix(s, "[broadcast]").trim();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(msg);
                }
                continue;
            }
            if (s.startsWith("[delay]")) {
                int index = i + 1;
                long delay = parseLong(removePrefix(s, "[delay]").trim()).orElse(0L);
                if (delay > 0 && index < commands.size()) {
                    Bukkit.getScheduler().runTaskLater(
                            PremiumVerify.getInstance(),
                            () -> runCommands(player, commands, index),
                            delay
                    );
                    break;
                }
            }
        }
    }

    public static String removePrefix(String s, String prefix) {
        return s.startsWith(prefix) ? s.substring(prefix.length()) : s;
    }

    public static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Optional<Integer> parseInt(String s) {
        if (s == null || s.isEmpty()) return Optional.empty();
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Long> parseLong(String s) {
        if (s == null || s.isEmpty()) return Optional.empty();
        try {
            return Optional.of(Long.parseLong(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static <T> List<T> split(Pattern regex, String s, Function<RegexResult, T> transform) {
        List<T> list = new ArrayList<>();
        int index = 0;
        Matcher m = regex.matcher(s);
        while (m.find()) {
            int first = m.start();
            int last = m.end();
            if (first > index) {
                T value = transform.apply(new RegexResult(false, s.substring(index, first)));
                if (value != null) list.add(value);
            }
            T value = transform.apply(new RegexResult(true, s.substring(first, last)));
            if (value != null) list.add(value);
            index = last;
        }
        if (index < s.length()) {
            T value = transform.apply(new RegexResult(false, s.substring(index)));
            if (value != null) list.add(value);
        }
        return list;
    }
    public static class RegexResult {
        public boolean isMatched;
        public String text;

        public RegexResult(boolean isMatched, String text) {
            this.isMatched = isMatched;
            this.text = text;
        }
    }
}
