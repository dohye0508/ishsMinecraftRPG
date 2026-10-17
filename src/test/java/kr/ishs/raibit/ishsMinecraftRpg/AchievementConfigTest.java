package kr.ishs.raibit.ishsMinecraftRpg;

import org.bukkit.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AchievementConfigTest {

    private static final List<String> WARNINGS = new ArrayList<>();

    @BeforeAll
    static void load() {
        Logger log = Logger.getLogger("test-achievements");
        log.setUseParentHandlers(false);
        log.addHandler(new Handler() {
            @Override public void publish(LogRecord record) { WARNINGS.add(record.getMessage()); }
            @Override public void flush() {}
            @Override public void close() {}
        });
        AchievementConfig.load(TestSupport.plugin("achievements.yml"), log);
    }

    @Test
    void loadsWithoutWarningsAndHasAllTexts() {
        assertTrue(WARNINGS.isEmpty(), "achievements.yml 을 읽는 중 경고: " + WARNINGS);
        assertFalse(AchievementConfig.guiTitle.isBlank());
        assertFalse(AchievementConfig.progressTitle.isBlank());
        assertFalse(AchievementConfig.unlockedName.isBlank());
        assertFalse(AchievementConfig.lockedName.isBlank());
        assertFalse(AchievementConfig.popupTitle.isBlank());
        assertFalse(AchievementConfig.popupChat.isEmpty());
    }

    @Test
    @Tag("parity")
    void thirteenAchievementsNineVisibleInGui() {
        List<AchievementConfig.Def> all = AchievementConfig.all();
        assertEquals(13, all.size());
        List<AchievementConfig.Def> visible = all.stream().filter(d -> d.showInGui).collect(Collectors.toList());
        assertEquals(9, visible.size(), "GUI 에 보이는 도전과제는 이전과 같은 9개");
        // 레벨 도전과제 4개는 알림 전용
        assertEquals(4, all.stream().filter(d -> d.id.startsWith("level_") && !d.showInGui).count());
    }

    @Test
    @Tag("parity")
    void popupFallsBackToDescriptionExceptWhereOverridden() {
        AchievementConfig.Def death = AchievementConfig.get("first_death");
        assertNotNull(death);
        assertEquals("너무 무리하지 마세요!", death.popup);
        assertEquals("가끔은 쓰러져도 괜찮습니다.", death.description);

        AchievementConfig.Def kill = AchievementConfig.get("first_kill");
        assertEquals(kill.description, kill.popup);
    }

    @Test
    @Tag("parity")
    void npcCountEntryUsesPrefixAndCountPlaceholder() {
        AchievementConfig.Def npc = AchievementConfig.get("first_npc");
        assertEquals("first_npc_", npc.countPrefix);
        assertTrue(npc.description.contains("{count}"));
        assertEquals(Material.EMERALD, npc.material);
    }
}
