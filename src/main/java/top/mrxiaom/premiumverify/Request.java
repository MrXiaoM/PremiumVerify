package top.mrxiaom.premiumverify;

import net.lenni0451.commons.httpclient.HttpClient;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.java.StepMCProfile;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode;
import net.raphimc.minecraftauth.util.MicrosoftConstants;
import org.bukkit.entity.Player;
import top.mrxiaom.premiumverify.utils.ColorHelper;
import top.mrxiaom.premiumverify.utils.Util;

import java.util.regex.Pattern;

import static top.mrxiaom.premiumverify.utils.ColorHelper.t;

public class Request {
    final Main plugin;
    final Player player;
    final HttpClient httpClient = MinecraftAuth.createHttpClient();
    final Thread thread;
    private Request(Main plugin, Player player) {
        this.thread = new Thread(this::run);
        this.plugin = plugin;
        this.player = player;
        thread.start();
    }

    public static Request create(Main plugin, Player player) {
        return new Request(plugin, player);
    }

    private void run() {
        t(player, plugin.msgVerifyStart);
        try {
            StepFullJavaSession.FullJavaSession session = MinecraftAuth.builder()
                    .withTimeout(plugin.timeout)
                    .withClientId(MicrosoftConstants.JAVA_TITLE_ID).withScope(MicrosoftConstants.SCOPE_TITLE_AUTH)
                    .deviceCode()
                    .withDeviceToken("Win32")
                    .sisuTitleAuthentication(MicrosoftConstants.JAVA_XSTS_RELYING_PARTY)
                    .buildMinecraftJavaProfileStep(true)
                    .getFromInput(httpClient, new StepMsaDeviceCode.MsaDeviceCodeCallback(msaDeviceCode -> {
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
                                new Text(new BaseComponent[] {
                                        ColorHelper.bungee(String.join("\n", plugin.msgLinkHover).replace("%code%", code))
                                })
                        ));
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
                        component.addExtra(text);
                        return null;
                    });
                    player.spigot().sendMessage(component);
                }
            }));
            plugin.players.remove(player.getName());
            if (session.isExpiredOrOutdated()) {
                t(player, plugin.msgResultExpired);
                plugin.players.remove(player.getName());
                return;
            }
            StepMCProfile.MCProfile profile = session.getMcProfile();
            String uuid = profile.getId().toString();
            String name = profile.getName();
            if (!player.getName().equals(name)) {
                plugin.data.markPlayerFail(player.getName());
                for (String s : plugin.msgResultNotMatch) {
                    t(player, s.replace("%name%", name).replace("%player%", player.getName()));
                }
                plugin.players.remove(player.getName());
                return;
            }
            int verifyTimes = plugin.data.getPlayerVerifyTimes(uuid);
            if (verifyTimes > plugin.verifyTimesLimit) {
                plugin.data.markPlayerFail(player.getName());
                plugin.players.remove(player.getName());
                return;
            }
            plugin.players.remove(player.getName());
            plugin.data.markPlayerVerified(name, uuid);
            Util.runCommands(player, plugin.rewards);
        } catch (Throwable e) {
            plugin.players.remove(player.getName());
            t(player, plugin.msgResultCallOP);
            plugin.getLogger().warning("玩家 " + player.getName() + " 进行正版验证时出现一个异常");
            plugin.warn(e);
        }
    }

    public void cancel() {
        plugin.players.remove(player.getName());
        thread.interrupt();
    }
}
