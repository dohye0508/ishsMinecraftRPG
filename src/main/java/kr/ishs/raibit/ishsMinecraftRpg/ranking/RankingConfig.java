package kr.ishs.raibit.ishsMinecraftRpg.ranking;

import kr.ishs.raibit.ishsMinecraftRpg.config.ClubConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.logging.Logger;

/**
 * 월드 랭킹판 설정(ranking.yml)과 순위 줄 만들기. 서버 없이도 읽고 검증할 수 있다.
 * 실제로 글씨를 띄우는 일은 {@link RankingBoard} 가 한다.
 */
public final class RankingConfig {

    private RankingConfig() {}

    /** 읽어 온 설정 한 벌 */
    public static final class Settings {
        public final boolean enabled;
        public final int updateTicks;
        public final double scale;
        public final boolean background;
        public final String title;
        public final String line;
        public final String world;      // 비어 있으면 기본 월드
        public final boolean hasLocation;
        public final double x, y, z;
        public final float yaw;

        Settings(boolean enabled, int updateTicks, double scale, boolean background, String title, String line,
                 String world, boolean hasLocation, double x, double y, double z, float yaw) {
            this.enabled = enabled;
            this.updateTicks = updateTicks;
            this.scale = scale;
            this.background = background;
            this.title = title;
            this.line = line;
            this.world = world;
            this.hasLocation = hasLocation;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
        }
    }

    private static Settings current = parse(new YamlConfiguration(), null);

    public static void load(FileConfiguration cfg, Logger log) {
        current = parse(cfg, log);
    }

    public static Settings get() {
        return current;
    }

    /** 잘못된 값은 경고하고 기본값으로 대신한다. */
    public static Settings parse(FileConfiguration c, Logger log) {
        int seconds = c.getInt("update-seconds", 5);
        if (seconds < 1) {
            warn(log, "update-seconds 는 1 이상이어야 합니다. 5초로 대신합니다.");
            seconds = 5;
        }
        double scale = c.getDouble("scale", 3.0);
        if (scale <= 0) {
            warn(log, "scale 은 0보다 커야 합니다. 3 으로 대신합니다.");
            scale = 3.0;
        }
        return new Settings(
                c.getBoolean("enabled", true),
                seconds * 20,
                scale,
                c.getBoolean("background", true),
                c.getString("title", ""),
                c.getString("line", "&f{rank}위 {club} &f{score}점"),
                c.getString("world", ""),
                c.isSet("location.x") && c.isSet("location.y") && c.isSet("location.z"),
                c.getDouble("location.x"), c.getDouble("location.y"), c.getDouble("location.z"),
                (float) c.getDouble("location.yaw", 0));
    }

    /**
     * 순위 줄. 점수가 높은 순이고, 점수가 같으면 clubs.yml 의 순서를 따른다.
     *
     * @param template {rank} 순위, {club} 동아리(색+아이콘+이름), {score} 점수
     */
    public static List<String> rankedLines(List<ClubConfig.Club> clubs, ToIntFunction<ClubConfig.Club> score, String template) {
        List<ClubConfig.Club> sorted = new ArrayList<>(clubs);
        sorted.sort((a, b) -> Integer.compare(score.applyAsInt(b), score.applyAsInt(a))); // 안정 정렬: 동점은 원래 순서 유지

        List<String> out = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            ClubConfig.Club club = sorted.get(i);
            out.add(Messages.apply(template, "rank", i + 1, "club", club.prefix(), "score", score.applyAsInt(club)));
        }
        return out;
    }

    /**
     * ranking.yml 글에서 world, location 줄만 바꿔 돌려준다. (YAML 을 다시 쓰면 주석이 사라지므로 줄 단위로 고친다)
     * 줄이 없으면 맨 아래에 붙인다.
     */
    public static String withLocation(String text, String world, String inlineLocation) {
        List<String> lines = new ArrayList<>(Arrays.asList(text.split("\n", -1)));
        setTopLevel(lines, "world", "\"" + world + "\"");
        setTopLevel(lines, "location", inlineLocation);
        return String.join("\n", lines);
    }

    /** location(과 world) 줄을 지워 랭킹판을 없앤다. */
    public static String withoutLocation(String text) {
        List<String> lines = new ArrayList<>(Arrays.asList(text.split("\n", -1)));
        lines.removeIf(l -> l.startsWith("location:") || l.startsWith("world:"));
        return String.join("\n", lines);
    }

    private static void setTopLevel(List<String> lines, String key, String value) {
        String cr = lines.stream().anyMatch(l -> l.endsWith("\r")) ? "\r" : "";
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(key + ":")) {
                lines.set(i, key + ": " + value + cr);
                return;
            }
        }
        // 맨 아래(끝의 빈 줄 앞)에 붙인다
        int at = lines.size();
        while (at > 0 && lines.get(at - 1).isBlank()) at--;
        lines.add(at, key + ": " + value + cr);
    }

    private static void warn(Logger log, String why) {
        if (log != null) log.warning("[ranking.yml] " + why);
    }
}
