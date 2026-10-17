package kr.ishs.raibit.ishsMinecraftRpg.config;

import kr.ishs.raibit.ishsMinecraftRpg.TestSupport;
import org.bukkit.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * gameplay.yml 의 기본값이, 설정 파일로 옮기기 전 코드에 박혀 있던 계산식/수치와 똑같은 결과를 내는지 확인한다.
 * (아래 reference* 메서드는 옮기기 전 코드의 계산식을 그대로 복사한 것)
 */
@Tag("parity")
class GameplayConfigTest {

    private static final Logger LOG = Logger.getLogger("test-gameplay");
    private static final StringBuilder WARNINGS = new StringBuilder();

    @BeforeAll
    static void load() {
        LOG.setUseParentHandlers(false);
        LOG.addHandler(new Handler() {
            @Override public void publish(LogRecord record) { WARNINGS.append(record.getMessage()).append('\n'); }
            @Override public void flush() {}
            @Override public void close() {}
        });
        GameplayConfig.load(TestSupport.plugin("gameplay.yml"), LOG);
    }

    // ===== 옮기기 전 코드의 계산식 (참조) =====

    private static double referenceMaxHp(int hpStat) { return 100 + (hpStat * 15); }
    private static double referenceMaxMp(int intel) { return 50 + (intel * 10); }

    private static float referenceWalkSpeed(int dex) {
        float baseSpeed = 0.2f;
        float speedBonus = (float) (dex * 0.0003);
        return baseSpeed + (baseSpeed * Math.min(speedBonus, 0.5f));
    }

    private static double referenceRequiredExp(int level) {
        if (level >= 100) return Double.MAX_VALUE;
        return Math.floor(100 * Math.pow(level, 2.5));
    }

    private static int referenceFreePoints(int level) {
        int newPoints = (int) (5 + Math.floor(level / 2.0));
        int autoAssignedPoints = (int) Math.ceil(newPoints * 0.2) * 5;
        int jobPoints = (int) Math.floor(newPoints * 0.3);
        return newPoints - autoAssignedPoints - jobPoints;
    }

    // ===== 테스트 =====

    @Test
    void loadingGameplayYmlProducesNoWarnings() {
        assertEquals("", WARNINGS.toString(), "gameplay.yml 을 읽는 중 경고가 났습니다");
    }

    @Test
    void maxHpAndMpMatchPreviousFormula() {
        for (int stat = 0; stat <= 200; stat++) {
            assertEquals(referenceMaxHp(stat), GameplayConfig.maxHp(stat), 1e-9, "maxHp(" + stat + ")");
            assertEquals(referenceMaxMp(stat), GameplayConfig.maxMp(stat), 1e-9, "maxMp(" + stat + ")");
        }
    }

    @Test
    void walkSpeedMatchesPreviousFormulaIncludingTheCap() {
        for (int dex = 0; dex <= 3000; dex += 7) {
            assertEquals(referenceWalkSpeed(dex), GameplayConfig.walkSpeed(dex), 1e-6f, "walkSpeed(" + dex + ")");
        }
        // 상한: 민첩이 아주 높아도 기본 속도의 1.5배를 넘지 않는다
        assertEquals(0.2f * 1.5f, GameplayConfig.walkSpeed(1_000_000), 1e-6f);
    }

    @Test
    void manaRegenMatchesPreviousFormula() {
        for (int intel = 0; intel <= 300; intel++) {
            assertEquals(2 + intel / 5.0, GameplayConfig.manaRegenPerSecond(intel), 1e-9, "manaRegen(" + intel + ")");
        }
    }

    @Test
    void requiredExpMatchesPreviousFormulaAndMaxLevel() {
        for (int level = 1; level <= 110; level++) {
            assertEquals(referenceRequiredExp(level), GameplayConfig.requiredExp(level), 0.0, "requiredExp(" + level + ")");
        }
        assertEquals(Double.MAX_VALUE, GameplayConfig.requiredExp(100));
    }

    @Test
    void levelUpPointsMatchPreviousFormula() {
        for (int level = 2; level <= 100; level++) {
            assertEquals(referenceFreePoints(level), GameplayConfig.levelUpFreePoints(level), "levelUpFreePoints(" + level + ")");
        }
    }

    /** 기존 계산식이 레벨업 때 포인트를 줄이는 문제를 계속 알 수 있도록 남겨 둔 확인 (규칙을 정하면 이 테스트를 고친다) */
    @Test
    void levelUpPointsCurrentlyNeverIncrease() {
        for (int level = 2; level <= 100; level++) {
            assertTrue(GameplayConfig.levelUpFreePoints(level) <= 0,
                    "레벨 " + level + " 에서 포인트가 늘어납니다. 지급 규칙이 바뀌었다면 이 테스트와 문서를 함께 고치세요.");
        }
    }

    @Test
    void startingStatsAndGuiLayout() {
        assertEquals(5, GameplayConfig.startingPoints);
        assertEquals(1, GameplayConfig.clickAmount);
        assertEquals(10, GameplayConfig.shiftClickAmount);
        assertEquals(54, GameplayConfig.statGuiSize);
        assertEquals(4, GameplayConfig.statInfoSlot);

        for (String id : GameplayConfig.STAT_IDS) {
            GameplayConfig.StatDef def = GameplayConfig.stat(id);
            assertNotNull(def, id);
            assertEquals(1, def.starting, id + " 시작 값");
            assertFalse(def.label.isBlank(), id + " label");
            assertFalse(def.guiName.isBlank(), id + " gui-name");
        }
        // 이전 /stat GUI 배치와 같은 칸/아이콘
        assertEquals(20, GameplayConfig.stat("str").slot);
        assertEquals(Material.REDSTONE, GameplayConfig.stat("str").material);
        assertEquals(21, GameplayConfig.stat("dex").slot);
        assertEquals(Material.FEATHER, GameplayConfig.stat("dex").material);
        assertEquals(22, GameplayConfig.stat("intel").slot);
        assertEquals(Material.BOOK, GameplayConfig.stat("intel").material);
        assertEquals(23, GameplayConfig.stat("hp").slot);
        assertEquals(Material.APPLE, GameplayConfig.stat("hp").material);
        assertEquals(24, GameplayConfig.stat("wis").slot);
        assertEquals(Material.NETHER_STAR, GameplayConfig.stat("wis").material);
        assertEquals("hp", GameplayConfig.statBySlot(23).id);
        assertEquals(null, GameplayConfig.statBySlot(0));
    }

    @Test
    void rewardsMatchPreviousValues() {
        assertEquals(10.0, GameplayConfig.killExp);
        assertEquals(0.2, GameplayConfig.killStatPointChance);
        assertEquals(1, GameplayConfig.killStatPoints);
        assertEquals("first_kill", GameplayConfig.killAchievement);

        assertEquals(2.0, GameplayConfig.miningExp);
        assertEquals(0.05, GameplayConfig.miningStatPointChance);
        assertEquals(1, GameplayConfig.miningStatPoints);

        assertEquals("first_death", GameplayConfig.deathAchievement);
        assertEquals("pvp_win", GameplayConfig.pvpWinAchievement);
        assertEquals(5, GameplayConfig.pvpWinStatPoints);

        assertEquals(15.0, GameplayConfig.coopRadius);
        assertEquals(5.0, GameplayConfig.coopExp);
        assertEquals("club_coop", GameplayConfig.coopAchievement);
    }

    @Test
    void miningSpecialsCoverDiamondAndLogs() {
        List<GameplayConfig.MiningSpecial> specials = GameplayConfig.miningSpecials();
        assertEquals(2, specials.size());

        GameplayConfig.MiningSpecial diamond = specials.get(0);
        assertTrue(diamond.matches(Material.DIAMOND_ORE));
        assertTrue(diamond.matches(Material.DEEPSLATE_DIAMOND_ORE));
        assertFalse(diamond.matches(Material.IRON_ORE));
        assertEquals(30.0, diamond.exp);
        assertEquals("diamond", diamond.achievement);

        GameplayConfig.MiningSpecial logs = specials.get(1);
        assertTrue(logs.matches(Material.OAK_LOG));
        assertTrue(logs.matches(Material.DARK_OAK_LOG));
        assertFalse(logs.matches(Material.STONE));
        assertEquals("lumberjack", logs.achievement);
    }

    @Test
    void levelAchievementsAreOrderedAndCumulative() {
        assertTrue(GameplayConfig.achievementsUpTo(4).isEmpty());
        assertEquals(List.of("level_5"), GameplayConfig.achievementsUpTo(5));
        assertEquals(List.of("level_5", "level_10"), GameplayConfig.achievementsUpTo(12));
        assertEquals(List.of("level_5", "level_10", "level_20", "level_50"), GameplayConfig.achievementsUpTo(50));
        assertEquals(5, GameplayConfig.broadcastEveryLevels);
        assertEquals(100, GameplayConfig.maxLevel);
    }

    @Test
    void rulesMatchPreviousBehavior() {
        assertFalse(GameplayConfig.survivalBlockBreak);
        assertFalse(GameplayConfig.spawnAnimals);
        assertFalse(GameplayConfig.allowTimeSkip);
        assertFalse(GameplayConfig.hungerLoss);
        assertFalse(GameplayConfig.explosionBlockDamage);
        assertTrue(GameplayConfig.glowingPlayers);
        assertTrue(GameplayConfig.pvpRequireClub);
        assertTrue(GameplayConfig.pvpBlockSameClub);
        assertTrue(GameplayConfig.clearWeather);
    }
}
