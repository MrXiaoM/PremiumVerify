package top.mrxiaom.premiumverify;

import com.google.common.collect.Lists;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked"})
public enum Lang {
    help(
            "&7[&b正版验证&7] &e正版验证 帮助",
            "&7[&b正版验证&7] &f/pv request &e请求正版验证，最多可尝试3次"
    ),
    help_op(
            "&7[&b正版验证&7] &e正版验证 帮助",
            "&7[&b正版验证&7] &f/pv request &e请求正版验证，最多可尝试3次",
            "&7[&b正版验证&7] &f/pv fail <玩家> <次数> &e设置玩家的失败次数",
            "&7[&b正版验证&7] &f/pv reload &e重载配置文件"
    ),
    commands_reload("&a配置文件已重载."),
    commands_fail("&a已设置 &e%player% &a的验证失败次数为 &e%times%."),
    error__only_player("&7[&b正版验证&7] &e该命令仅能由玩家执行."),
    error__no_player("&7[&b正版验证&7] &e无法找到该玩家."),
    error__no_integer("&7[&b正版验证&7] &e你应该输入一个大于或等于0的整数."),
    error__fail_limit("&7[&b正版验证&7] &e你的账户已达到允许失败次数上限.", true),
    error__already_in_verify("&7[&b正版验证&7] &e你已经在验证中了.", true),
    error__already_verified("&7[&b正版验证&7] &a你已经进行过正版验证了.", true),
    verify_start("&7[&b正版验证&7] &e正在发起验证请求，请稍等…", true),
    verify(
            "&f",
            "&f",
            "&f  请打开聊天栏，点击 %link% &f打开网页进行验证。",
            "&f  请尽量使用&e人脸、指纹、PIN 或安全密钥&f、&e账号密码&f等方式登录，",
            "&f  避免使用 Github 账号等第三方账号登录。",
            "&f",
            "&f  请在&e5分钟&f内完成验证。",
            "&f  验证期间&b不要离开服务器&f，完成后你将获得奖励。",
            "&f"
    ),
    link_text("&7[&b&n&l验证网址&7]"),
    link_hover(
            "&e点击打开验证网址",
            "&7如果需要输入代码，请使用 &f%code%"
    ),
    result_expired("&7[&b正版验证&7] &e你申请的验证已超时或过期.", true),
    result_not_match("&7[&b正版验证&7] &e用户名不正确，该正版账户的用户名是&b %name%&e，而你是&b %player%&e.", true),
    result_limit("&7[&b正版验证&7] &e该正版账户已到达可验证次数上限.", true),
    result_call_op("&7[&b正版验证&7] &c验证时出现错误，请联系服务器管理员查看日志", true),

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
        this.isList = false;
        defaultValue = s;
    }
    Lang(String s, boolean isList) {
        this.isList = isList;
        if (isList) {
            defaultValue = Lists.newArrayList(s);
        } else {
            defaultValue = s;
        }
    }
    Lang(String... list) {
        this.isList = true;
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
