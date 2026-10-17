package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.config.ClubConfig;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 동아리 점수. 어떤 동아리가 있는지는 clubs.yml(ClubConfig)이 정하고, 여기서는 점수만 보관한다.
 * (점수가 쌓이는 조건은 아직 코드에 없어 현재는 모든 동아리가 0점이다)
 */
public class ClubScoreManager {
    private static final ClubScoreManager instance = new ClubScoreManager();
    private final Map<String, Integer> clubScores = new ConcurrentHashMap<>();

    private ClubScoreManager() {}

    public static ClubScoreManager getInstance() {
        return instance;
    }

    public int getScore(String club) {
        return club == null ? 0 : clubScores.getOrDefault(club.toUpperCase(Locale.ROOT), 0);
    }

    public void addScore(String club, int amount) {
        if (ClubConfig.get(club) != null) {
            clubScores.merge(club.toUpperCase(Locale.ROOT), amount, Integer::sum);
        }
    }
}
