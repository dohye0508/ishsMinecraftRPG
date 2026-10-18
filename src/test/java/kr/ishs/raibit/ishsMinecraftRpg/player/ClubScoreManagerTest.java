package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.TestSupport;
import kr.ishs.raibit.ishsMinecraftRpg.config.ClubConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubScoreManagerTest {

    private final ClubScoreManager scores = ClubScoreManager.getInstance();
    private static String club;

    @BeforeAll
    static void loadClubs() {
        ClubConfig.load(TestSupport.plugin("clubs.yml"), null);
        club = ClubConfig.all().get(0).id;
    }

    @AfterEach
    void cleanUp() {
        scores.load(null); // 저장 위치를 풀고 점수를 비운다
    }

    @Test
    void setAddResetWork() {
        assertTrue(scores.setScore(club, 10));
        assertTrue(scores.addScore(club, 5));
        assertEquals(15, scores.getScore(club));
        assertTrue(scores.addScore(club, -20));
        assertEquals(-5, scores.getScore(club), "음수 점수도 허용");
        assertTrue(scores.reset(club));
        assertEquals(0, scores.getScore(club));
    }

    @Test
    void clubNameIsCaseInsensitiveAndUnknownIsRejected() {
        assertTrue(scores.setScore(club.toLowerCase(), 7));
        assertEquals(7, scores.getScore(club));
        assertFalse(scores.setScore("NO_SUCH_CLUB", 1));
        assertFalse(scores.addScore("NO_SUCH_CLUB", 1));
        assertEquals(0, scores.getScore("NO_SUCH_CLUB"));
    }

    @Test
    void resetAllClearsEverything() {
        scores.setScore(club, 3);
        scores.resetAll();
        assertEquals(0, scores.getScore(club));
    }

    @Test
    void scoresSurviveAReload(@TempDir File dir) {
        File file = new File(dir, "club-scores.yml");
        scores.load(file);
        scores.setScore(club, 42);

        scores.load(file); // 서버를 껐다 켠 것처럼 다시 읽는다
        assertEquals(42, scores.getScore(club));
    }
}
