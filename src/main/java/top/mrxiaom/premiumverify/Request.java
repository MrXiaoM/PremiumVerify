package top.mrxiaom.premiumverify;

import net.lenni0451.commons.httpclient.HttpClient;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.java.StepMCProfile;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode;
import net.raphimc.minecraftauth.util.MicrosoftConstants;
import net.raphimc.minecraftauth.util.logging.ILogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.mrxiaom.premiumverify.utils.ColorHelper;
import top.mrxiaom.premiumverify.utils.Util;

import java.util.regex.Pattern;

import static top.mrxiaom.premiumverify.utils.ColorHelper.t;

public class Request {
    public static class NOPLogger implements ILogger {
        public static NOPLogger INSTANCE = new NOPLogger();
        private NOPLogger() {}
        @Override
        public void info(String s) {}
        @Override
        public void warn(String s) {}
        @Override
        public void error(String s) {}
    }
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
        t(player, plugin.msgVerifyStart);
        try {
            HttpClient httpClient = MinecraftAuth.createHttpClient();
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
                for (String s : plugin.msgVerify) {
                    if (!s.contains("%link%")) {
                        t(player, s);
                        continue;
                    }
                    TextComponent component = new TextComponent();
                    Util.split(pattern, s, it -> {
                        if (!it.isMatched) {
                            component.addExtra(ColorHelper.bungee(it.text));
                            return;
                        }
                        TextComponent text = ColorHelper.bungee(plugin.msgLinkText);
                        text.setHoverEvent(ColorHelper.hover(String.join("\n", plugin.msgLinkHover).replace("%code%", code)));
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
            Bukkit.getScheduler().runTask(plugin, () -> Util.runCommands(player, plugin.rewards));
        } catch (Throwable e) {
            boolean withCause = true;
            plugin.players.remove(player.getName());
            if (e instanceof java.util.concurrent.TimeoutException) {
                t(player, plugin.msgResultExpired);
                return;
            }
            if (e instanceof java.lang.InterruptedException) {
                return;
            }
            t(player, plugin.msgResultCallOP);
            plugin.getLogger().warning("玩家 " + player.getName() + " 进行正版验证时出现一个异常");
            if (e instanceof java.io.IOException) { // java 11: java.net.http.HttpConnectTimeoutException
                withCause = false;
            }
            plugin.warn(e, withCause);
        }
    }

    public void cancel() {
        plugin.players.remove(player.getName());
        thread.interrupt();
    }
}
