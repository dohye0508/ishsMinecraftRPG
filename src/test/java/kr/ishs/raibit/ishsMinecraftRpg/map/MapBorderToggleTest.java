package kr.ishs.raibit.ishsMinecraftRpg.map;

import kr.ishs.raibit.ishsMinecraftRpg.MapConfig;
import kr.ishs.raibit.ishsMinecraftRpg.TestSupport;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapBorderToggleTest {

    private static final String CONFIG = """
            # 위쪽 주석
            auth:
              enabled: true
            border:
              # 설명 주석
              enabled: true
              corner1:
                x: 20
            spawn:
              enabled: yes
            """;

    private static boolean borderEnabled(String text) throws Exception {
        YamlConfiguration c = new YamlConfiguration();
        c.loadFromString(text);
        return c.getBoolean("border.enabled");
    }

    @Test
    void turnsOnlyTheBorderEnabledLineOffAndOn() throws Exception {
        String off = MapConfig.withBorderEnabled(CONFIG, false);
        assertNotNull(off);
        assertFalse(borderEnabled(off));
        assertTrue(off.contains("auth:\n  enabled: true"), "auth.enabled 는 그대로");
        assertTrue(off.contains("# 위쪽 주석") && off.contains("# 설명 주석"), "주석은 그대로");
        assertTrue(off.contains("spawn:\n  enabled: yes"), "다른 enabled 줄은 건드리지 않는다");

        assertTrue(borderEnabled(MapConfig.withBorderEnabled(off, true)));
    }

    @Test
    void returnsNullWhenThereIsNoBorderEnabledLine() {
        assertNull(MapConfig.withBorderEnabled("auth:\n  enabled: true\n", false));
    }

    @Test
    void keepsWindowsLineEndings() {
        String crlf = CONFIG.replace("\n", "\r\n");
        String off = MapConfig.withBorderEnabled(crlf, false);
        assertTrue(off.contains("  enabled: false\r\n"));
        assertEquals(crlf.split("\r\n").length, off.split("\r\n").length);
    }

    @Test
    void worksOnTheShippedConfig() throws Exception {
        String shipped = Files.readString(Path.of("configs", "ishsMinecraftRpg", "config.yml"), StandardCharsets.UTF_8);
        String off = MapConfig.withBorderEnabled(shipped, false);
        assertNotNull(off, "배포 config.yml 에서 border.enabled 줄을 찾을 수 있어야 합니다");
        assertFalse(borderEnabled(off));
        assertEquals(TestSupport.plugin("config.yml").getInt("auth.max-fail"),
                YamlConfigurationOf(off).getInt("auth.max-fail"), "다른 값은 그대로");
    }

    private static YamlConfiguration YamlConfigurationOf(String text) throws Exception {
        YamlConfiguration c = new YamlConfiguration();
        c.loadFromString(text);
        return c;
    }
}
