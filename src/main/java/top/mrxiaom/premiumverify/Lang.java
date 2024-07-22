package top.mrxiaom.premiumverify;

import com.google.common.collect.Lists;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked"})
public enum Lang {
    ;
    private static final Map<Lang, Object> current = new HashMap<>();
    static void reload(ConfigurationSection config) {
        current.clear();
        if (config == null) return;
        for (Lang lang : values()) {
            String key = lang.key;
            if (!config.contains(key)) continue;
            if (lang.isList) {
                if (config.isList(key)) {
                    current.put(lang, config.getStringList(key));
                }
            } else {
                current.put(lang, config.getString(key));
            }
        }
    }
    public final String key = name().toLowerCase().replace("__", ".").replace("_", "-");
    public final boolean isList;
    private final Object defaultValue;
    Lang(String s) {
        isList = false;
        defaultValue = s;
    }
    Lang(String... list) {
        isList = true;
        defaultValue = Lists.newArrayList(list);
    }

    public String str() {
        if (isList) {
            List<String> list = (List<String>) current.getOrDefault(this, defaultValue);
            return String.join("\n", list);
        } else {
            return (String) current.getOrDefault(this, defaultValue);
        }
    }

    public List<String> list() {
        if (isList) {
            List<String> list = (List<String>) current.getOrDefault(this, defaultValue);
            return Lists.newArrayList(list);
        } else {
            return Lists.newArrayList((String) current.getOrDefault(this, defaultValue));
        }
    }
}
