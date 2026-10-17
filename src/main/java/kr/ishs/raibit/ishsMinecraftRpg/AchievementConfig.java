package kr.ishs.raibit.ishsMinecraftRpg;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 도전과제(업적) 정의. achievements.yml에서 읽는다. (기획팀이 코드 없이 수정 가능)
 * /도전과제 GUI와 달성 알림 문구가 모두 여기서 나온다.
 * 달성 조건은 도전과제 ID에 코드가 연결되어 있다.
 */
public final class AchievementConfig {

    private AchievementConfig() {}

    public static final class Def {
        public final String id;
        public final Material material;
        public final String title;
        public final String description;
        /** 달성했을 때 화면에 뜨는 문구 (없으면 description) */
        public final String popup;
        public final boolean showInGui;
        /** 이 글자로 시작하는 달성 기록의 개수를 세는 항목이면 그 접두어, 아니면 null */
        public final String countPrefix;

        Def(String id, Material material, String title, String description, String popup,
            boolean showInGui, String countPrefix) {
            this.id = id;
            this.material = material;
            this.title = title;
            this.description = description;
            this.popup = popup;
            this.showInGui = showInGui;
            this.countPrefix = countPrefix;
        }
    }

    private static final Map<String, Def> DEFS = new LinkedHashMap<>();

    // GUI 공통 문구 (achievements.yml 의 texts 항목, 없으면 아래 기본값)
    public static String lockedDescription = "아직 밝혀지지 않은 업적입니다.";
    public static String unlockedLabel = "✔ 달성 완료";
    public static String lockedLabel = "✘ 미달성";
    public static String progressTitle = "달성 현황";

    /** achievements.yml 을 읽어 정의를 갱신합니다. */
    public static void load(FileConfiguration config, Logger logger) {
        DEFS.clear();

        lockedDescription = color(config.getString("texts.locked-description", lockedDescription));
        unlockedLabel = color(config.getString("texts.unlocked-label", unlockedLabel));
        lockedLabel = color(config.getString("texts.locked-label", lockedLabel));
        progressTitle = color(config.getString("texts.progress-title", progressTitle));

        ConfigurationSection section = config.getConfigurationSection("achievements");
        if (section == null) {
            logger.warning("[achievements.yml] achievements 항목이 없습니다. 도전과제가 없습니다.");
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(id);
            if (s == null) continue;

            String materialName = s.getString("material", "PAPER");
            Material material = Material.matchMaterial(materialName);
            if (material == null || material.isAir()) {
                logger.warning("[achievements.yml] '" + id + "' 의 material '" + materialName
                        + "' 을(를) 찾을 수 없어 PAPER로 대체합니다.");
                material = Material.PAPER;
            }

            String title = color(s.getString("title", id));
            String description = color(s.getString("description", ""));
            String popup = s.contains("popup") ? color(s.getString("popup", description)) : description;
            DEFS.put(id, new Def(id, material, title, description, popup,
                    s.getBoolean("show-in-gui", true), s.getString("count-prefix")));
        }
    }

    /** 정의 순서대로의 전체 도전과제 */
    public static List<Def> all() {
        return new ArrayList<>(DEFS.values());
    }

    /** ID로 정의 찾기 (없으면 null) */
    public static Def get(String id) {
        return DEFS.get(id);
    }

    @SuppressWarnings("deprecation")
    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
