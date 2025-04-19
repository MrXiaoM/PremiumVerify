package top.mrxiaom.premiumverify;

import net.lenni0451.commons.httpclient.HttpClient;
import net.lenni0451.commons.httpclient.proxy.ProxyHandler;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.java.StepMCProfile;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode;
import net.raphimc.minecraftauth.util.MicrosoftConstants;
import net.raphimc.minecraftauth.util.logging.NOPLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.mrxiaom.premiumverify.utils.ColorHelper;
import top.mrxiaom.premiumverify.utils.Util;

import java.util.regex.Pattern;

import static top.mrxiaom.premiumverify.utils.ColorHelper.t;

public class Request {
    final Main plugin;
    final Player player;
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
        t(player, Lang.verify_start);
        try {
            HttpClient httpClient;
            ProxyHandler proxy = plugin.getProxy();
            if (proxy != null) {
                httpClient = MinecraftAuth.createHttpClient(10000);
                httpClient.setProxyHandler(proxy);
                httpClient.setIgnoreInvalidSSL(true);
            } else {
                httpClient = MinecraftAuth.createHttpClient();
            }
            StepFullJavaSession.FullJavaSession session = MinecraftAuth.builder()
                    .withTimeout(plugin.timeout)
                    .withClientId(MicrosoftConstants.JAVA_TITLE_ID).withScope(MicrosoftConstants.SCOPE_TITLE_AUTH)
                    .deviceCode()
                    .withDeviceToken("Win32")
                    .sisuTitleAuthentication(MicrosoftConstants.JAVA_XSTS_RELYING_PARTY)
                    .buildMinecraftJavaProfileStep(true)
                    .getFromInput(NOPLogger.INSTANCE, httpClient, new StepMsaDeviceCode.MsaDeviceCodeCallback(msaDeviceCode -> {
                String link = msaDeviceCode.getDirectVerificationUri();
                String code = msaDeviceCode.getUserCode();
                Pattern pattern = Pattern.compile("(%link%)");
                for (String s : Lang.verify.list()) {
                    if (!s.contains("%link%")) {
                        t(player, s);
                        continue;
                    }
                    TextComponent component = new TextComponent("");
                    Util.split(pattern, s, it -> {
                        if (!it.isMatched) {
                            component.addExtra(ColorHelper.bungee(it.text));
                            return;
                        }
                        TextComponent text = ColorHelper.bungee(Lang.link_text.str());
                        text.setHoverEvent(ColorHelper.hover(Lang.link_hover.str().replace("%code%", code)));
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
                        component.addExtra(text);
                    });
                    player.spigot().sendMessage(component);
                }
            }));
            plugin.players.remove(player.getName());
            StepMCProfile.MCProfile profile = session.getMcProfile();
            String uuid = profile.getId().toString();
            String name = profile.getName();

            if (!player.getName().equals(name)) {
                plugin.data.markPlayerFail(player.getName());
                for (String s : Lang.result_not_match.list()) {
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
            plugin.getScheduler().runNextTick((t) -> Util.runCommands(player, plugin.rewards));
        } catch (Throwable e) {
            boolean withCause = true;
            plugin.players.remove(player.getName());
            if (e instanceof java.util.concurrent.TimeoutException) {
                t(player, Lang.result_expired);
                return; // 不重要异常: 达到 timeout 时间未响应
            }
            if (e instanceof java.lang.InterruptedException) {
                return; // 不重要异常: thread.interrupt();
            }
            if (e instanceof java.io.IOException) { // java 11: java.net.http.HttpConnectTimeoutException
                withCause = false; // 无需详细信息异常: 网络超时等问题
                t(player, Lang.result_io_exception);
            } else {
                t(player, Lang.result_call_op);
            }
            plugin.getLogger().warning("玩家 " + player.getName() + " 进行正版验证时出现一个异常");
            plugin.warn(e, withCause);
        }
    }

    public void cancel() {
        plugin.players.remove(player.getName());
        thread.interrupt();
    }
}
