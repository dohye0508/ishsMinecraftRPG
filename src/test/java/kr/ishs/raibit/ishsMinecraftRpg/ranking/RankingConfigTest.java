package kr.ishs.raibit.ishsMinecraftRpg.ranking;

import kr.ishs.raibit.ishsMinecraftRpg.TestSupport;
import kr.ishs.raibit.ishsMinecraftRpg.config.ClubConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankingConfigTest {

    @BeforeAll
    static void loadClubs() {
        ClubConfig.load(TestSupport.plugin("clubs.yml"), null);
    }

    private static YamlConfiguration yaml(String text) {
        try {
            YamlConfiguration c = new YamlConfiguration();
            c.loadFromString(text);
            return c;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void defaultsAndLocationDetection() {
        RankingConfig.Settings s = RankingConfig.parse(yaml("title: x"), null);
        assertTrue(s.enabled);
        assertEquals(100, s.updateTicks, "기본 5초");
        assertFalse(s.hasLocation, "location 이 없으면 랭킹판을 띄우지 않는다");

        RankingConfig.Settings placed = RankingConfig.parse(yaml("location: {x: 1.5, y: 2, z: 3, yaw: 90}"), null);
        assertTrue(placed.hasLocation);
        assertEquals(90f, placed.yaw);
    }

    @Test
    void invalidValuesFallBack() {
        RankingConfig.Settings s = RankingConfig.parse(yaml("update-seconds: 0\nscale: -2"), null);
        assertEquals(100, s.updateTicks);
        assertEquals(2.0, s.scale);
    }

    @Test
    void brightnessDefaultsToMaxAndRejectsOutOfRange() {
        assertEquals(15, RankingConfig.parse(yaml("title: x"), null).brightness);
        assertEquals(-1, RankingConfig.parse(yaml("brightness: -1"), null).brightness);
        assertEquals(15, RankingConfig.parse(yaml("brightness: 99"), null).brightness);
    }

    @Test
    void ranksByScoreAndKeepsClubsYmlOrderOnTies() {
        List<ClubConfig.Club> clubs = ClubConfig.all();
        String first = clubs.get(0).id, second = clubs.get(1).id, third = clubs.get(2).id;
        Map<String, Integer> scores = Map.of(second, 50, third, 50); // first 는 0점

        List<String> lines = RankingConfig.rankedLines(clubs, c -> scores.getOrDefault(c.id, 0), "{rank}|{club}|{score}");
        assertEquals(clubs.size(), lines.size());
        assertTrue(lines.get(0).startsWith("1|") && lines.get(0).contains(second) && lines.get(0).endsWith("|50"));
        assertTrue(lines.get(1).startsWith("2|") && lines.get(1).contains(third), "동점은 clubs.yml 순서");
        assertTrue(lines.get(2).startsWith("3|") && lines.get(2).contains(first));
    }

    @Test
    void withLocationReplacesOnlyThoseLinesAndKeepsComments() {
        String text = "# 주석\nenabled: true\nworld: \"old\"\nlocation: {x: 0, y: 0, z: 0, yaw: 0}\nline: \"a\"\n";
        String out = RankingConfig.withLocation(text, "world", "{x: 1, y: 2, z: 3, yaw: 90}");
        assertTrue(out.contains("# 주석") && out.contains("enabled: true") && out.contains("line: \"a\""));
        assertTrue(out.contains("world: \"world\"") && out.contains("location: {x: 1, y: 2, z: 3, yaw: 90}"));
        assertFalse(out.contains("old"));
    }

    @Test
    void withLocationAppendsWhenMissingAndRemoveDeletes() {
        String text = "enabled: true\n";
        String out = RankingConfig.withLocation(text, "world", "{x: 1, y: 2, z: 3, yaw: 0}");
        assertTrue(RankingConfig.parse(yaml(out), null).hasLocation);
        assertFalse(RankingConfig.parse(yaml(RankingConfig.withoutLocation(out)), null).hasLocation);
    }

    @Test
    void shippedRankingYmlIsValid() {
        YamlConfiguration cfg = TestSupport.plugin("ranking.yml");
        RankingConfig.Settings s = RankingConfig.parse(cfg, null);
        assertTrue(s.updateTicks >= 20 && s.scale > 0);

        Matcher m = Pattern.compile("\\{([a-z-]+)\\}").matcher(s.line);
        while (m.find()) {
            assertTrue(List.of("rank", "club", "score").contains(m.group(1)), "line 의 {" + m.group(1) + "} 는 쓸 수 없습니다");
        }
        // 위치(world, location)는 서버에서 /랭킹판 설치로 정해 저장소에 가져온 값이라 있어도 없어도 된다
    }
}
