package top.mrxiaom.premiumverify;

import net.lenni0451.commons.httpclient.HttpClient;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.java.StepMCProfile;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode;
import org.bukkit.entity.Player;
import top.mrxiaom.premiumverify.utils.ColorHelper;
import top.mrxiaom.premiumverify.utils.Util;

import java.util.regex.Pattern;

import static top.mrxiaom.premiumverify.utils.ColorHelper.t;

public class VerifyRequest {
    final PremiumVerify plugin;
    final Player player;
    final HttpClient httpClient = MinecraftAuth.createHttpClient();
    final Thread thread;
    public VerifyRequest(PremiumVerify plugin, Player player) {
        this.thread = new Thread(this::run);
        this.plugin = plugin;
        this.player = player;
        thread.start();
    }

    private void run() {
        t(player, plugin.msgVerify);
        try {
            StepFullJavaSession.FullJavaSession session = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.getFromInput(httpClient, new StepMsaDeviceCode.MsaDeviceCodeCallback(msaDeviceCode -> {
                String link = msaDeviceCode.getDirectVerificationUri();
                String code = msaDeviceCode.getUserCode();
                Pattern pattern = Pattern.compile("(%link%)");
                for (String s : plugin.msgVerify) {
                    if (!s.contains("%link%")) {
                        t(player, s);
                        continue;
                    }
                    TextComponent component = new TextComponent();
                    Util.split(pattern, s, it -> {
                        if (!it.isMatched) {
                            component.addExtra(ColorHelper.bungee(it.text));
                            return null;
                        }
                        TextComponent text = ColorHelper.bungee(plugin.msgLinkText);
                        text.setHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new Text(ColorHelper.bungee(
                                        String.join("\n", plugin.msgLinkHover).replace("%code%", code)
                                ))
                        ));
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
                        component.addExtra(text);
                        return null;
                    });
                    player.spigot().sendMessage(component);
                }
            }));
            StepMCProfile.MCProfile profile = session.getMcProfile();
            plugin.players.remove(player.getName());
            // TODO: 验证玩家
        } catch (Exception e) {
            plugin.players.remove(player.getName());
            t(player, "验证时出现错误，请联系服务器管理员查看日志");
            plugin.warn(e);
        }
    }

    public void cancel() {
        thread.interrupt();
    }
}
