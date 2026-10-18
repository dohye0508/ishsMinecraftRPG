package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.TestSupport;
import kr.ishs.raibit.ishsMinecraftRpg.config.GameplayConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerAdjustTest {

    @BeforeAll
    static void load() {
        GameplayConfig.load(TestSupport.plugin("gameplay.yml"), Logger.getLogger("test-adjust"));
    }

    private static PlayerData newPlayer() {
        return new PlayerData(UUID.randomUUID());
    }

    @Test
    void resolvesKoreanAndEnglishNames() {
        assertEquals(PlayerAdjust.Kind.LEVEL, PlayerAdjust.resolve("레벨").kind());
        assertEquals(PlayerAdjust.Kind.LEVEL, PlayerAdjust.resolve("LEVEL").kind());
        assertEquals(PlayerAdjust.Kind.EXP, PlayerAdjust.resolve("경험치").kind());
        assertEquals(PlayerAdjust.Kind.POINTS, PlayerAdjust.resolve("스탯포인트").kind());
        assertEquals("str", PlayerAdjust.resolve("str").statId());
        assertEquals("intel", PlayerAdjust.resolve("intel").statId());
        assertNull(PlayerAdjust.resolve("모르는항목"));
        for (String id : GameplayConfig.STAT_IDS) {
            // gameplay.yml 에 적힌 스탯 이름(label)으로도 찾을 수 있어야 한다
            assertNotNull(PlayerAdjust.resolve(GameplayConfig.stat(id).label), id);
        }
    }

    @Test
    void levelSetAddAndRange() {
        PlayerData d = newPlayer();
        assertTrue(PlayerAdjust.apply(d, PlayerAdjust.resolve("레벨"), false, 10).ok());
        assertEquals(10, d.getLevel());
        assertTrue(PlayerAdjust.apply(d, PlayerAdjust.resolve("레벨"), true, 5).ok());
        assertEquals(15, d.getLevel());

        PlayerAdjust.Outcome tooHigh = PlayerAdjust.apply(d, PlayerAdjust.resolve("레벨"), false, GameplayConfig.maxLevel + 1);
        assertFalse(tooHigh.ok());
        assertEquals(15, d.getLevel(), "범위를 벗어나면 바꾸지 않는다");
        assertFalse(PlayerAdjust.apply(d, PlayerAdjust.resolve("레벨"), false, 0).ok());
    }

    @Test
    void expPointsAndStatsCannotGoNegative() {
        PlayerData d = newPlayer();
        assertTrue(PlayerAdjust.apply(d, PlayerAdjust.resolve("경험치"), false, 12.5).ok());
        assertEquals(12.5, d.getExperience());
        assertFalse(PlayerAdjust.apply(d, PlayerAdjust.resolve("경험치"), true, -100).ok());
        assertEquals(12.5, d.getExperience());

        assertTrue(PlayerAdjust.apply(d, PlayerAdjust.resolve("스탯포인트"), false, 7).ok());
        assertEquals(7, d.getStatPoints());
        assertFalse(PlayerAdjust.apply(d, PlayerAdjust.resolve("스탯포인트"), true, -8).ok());

        int before = d.getStr();
        assertTrue(PlayerAdjust.apply(d, PlayerAdjust.resolve("str"), true, 5).ok());
        assertEquals(before + 5, d.getStr());
        assertTrue(PlayerAdjust.apply(d, PlayerAdjust.resolve("str"), false, 3).ok());
        assertEquals(3, d.getStr());
        assertFalse(PlayerAdjust.apply(d, PlayerAdjust.resolve("str"), false, -1).ok());
        assertEquals(3, d.getStr());
    }

    @Test
    void outcomeReportsOldAndNewValues() {
        PlayerData d = newPlayer();
        PlayerAdjust.Outcome r = PlayerAdjust.apply(d, PlayerAdjust.resolve("레벨"), false, 4);
        assertEquals("1", r.oldValue());
        assertEquals("4", r.newValue());
    }

    @Test
    void resetReturnsToANewCharacter() {
        PlayerData d = newPlayer();
        PlayerAdjust.apply(d, PlayerAdjust.resolve("레벨"), false, 30);
        PlayerAdjust.apply(d, PlayerAdjust.resolve("str"), false, 99);
        PlayerAdjust.apply(d, PlayerAdjust.resolve("경험치"), false, 500);
        d.setClub("LOGIC");

        PlayerAdjust.reset(d);

        PlayerData fresh = newPlayer();
        assertEquals(fresh.getLevel(), d.getLevel());
        assertEquals(fresh.getExperience(), d.getExperience());
        assertEquals(fresh.getStatPoints(), d.getStatPoints());
        for (String id : GameplayConfig.STAT_IDS) assertEquals(fresh.getStat(id), d.getStat(id), id);
        assertFalse(d.hasClub());
    }
}
