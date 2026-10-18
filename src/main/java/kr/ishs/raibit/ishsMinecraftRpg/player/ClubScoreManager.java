package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.config.ClubConfig;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 동아리 점수. 어떤 동아리가 있는지는 clubs.yml(ClubConfig)이 정하고, 여기서는 점수만 보관한다.
 * 점수는 서버를 껐다 켜도 남도록 플러그인 폴더의 club-scores.yml 에 저장한다.
 * (점수를 올리는 건 관리자 명령 /동아리점수 뿐이고, 자동으로 쌓이는 규칙은 아직 없다)
 */
public class ClubScoreManager {
    private static final ClubScoreManager instance = new ClubScoreManager();
    private final Map<String, Integer> clubScores = new ConcurrentHashMap<>();
    private File file;

    private ClubScoreManager() {}

    public static ClubScoreManager getInstance() {
        return instance;
    }

    public int getScore(String club) {
        return club == null ? 0 : clubScores.getOrDefault(club.toUpperCase(Locale.ROOT), 0);
    }

    /** 점수를 amount 만큼 더한다. (음수면 뺀다) 없는 동아리면 false */
    public boolean addScore(String club, int amount) {
        ClubConfig.Club def = ClubConfig.get(club);
        if (def == null) return false;
        clubScores.merge(def.id, amount, Integer::sum);
        save();
        return true;
    }

    /** 점수를 정확히 value 로 맞춘다. 없는 동아리면 false */
    public boolean setScore(String club, int value) {
        ClubConfig.Club def = ClubConfig.get(club);
        if (def == null) return false;
        clubScores.put(def.id, value);
        save();
        return true;
    }

    /** 한 동아리 점수를 0점으로. 없는 동아리면 false */
    public boolean reset(String club) {
        return setScore(club, 0);
    }

    /** 모든 동아리 점수를 0점으로 */
    public void resetAll() {
        clubScores.clear();
        save();
    }

    /** 저장 파일을 지정하고 읽어 온다. 서버 시작 때 한 번 호출한다. */
    public void load(File dataFile) {
        this.file = dataFile;
        clubScores.clear();
        if (dataFile == null || !dataFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : yaml.getKeys(false)) {
            clubScores.put(key.toUpperCase(Locale.ROOT), yaml.getInt(key));
        }
    }

    private void save() {
        if (file == null) return; // 저장 위치가 정해지지 않았으면(테스트) 메모리에만 둔다
        YamlConfiguration yaml = new YamlConfiguration();
        clubScores.forEach(yaml::set);
        try {
            yaml.save(file);
        } catch (IOException e) {
            java.util.logging.Logger.getLogger("ishsMinecraftRpg").warning("club-scores.yml 을 저장하지 못했습니다: " + e.getMessage());
        }
    }
}
