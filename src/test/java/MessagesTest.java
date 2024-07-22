import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import top.mrxiaom.premiumverify.Lang;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MessagesTest {
    @Test
    public void generate() throws IOException {
        File original = new File("src/main/resources/config.yml");
        if (!original.exists()) throw new FileNotFoundException("config.yml");
        File file = new File("src/main/generated/resources/config.yml");
        if (file.getParentFile().mkdirs()) System.gc();
        YamlConfiguration config = new YamlConfiguration();
        for (Lang lang : Lang.values()) {
            String key = "messages." + lang.key;
            boolean isList = lang.isList;
            if (isList) {
                config.set(key, lang.list());
            } else {
                config.set(key, lang.str());
            }
        }
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fileInput = new FileInputStream(original);
             InputStreamReader input = new InputStreamReader(fileInput, StandardCharsets.UTF_8)
        ) {
            int length;
            char[] buffer = new char[1024];
            while ((length = input.read(buffer)) != -1) {
                sb.append(buffer, 0, length);
            }
        }
        sb.append("\n# 插件消息\n").append(config.saveToString());
        try (FileOutputStream fileOutput = new FileOutputStream(file);
            OutputStreamWriter output = new OutputStreamWriter(fileOutput, StandardCharsets.UTF_8);
            BufferedWriter writer = new BufferedWriter(output)
        ) {
            writer.write(sb.toString());
        }
    }
}
