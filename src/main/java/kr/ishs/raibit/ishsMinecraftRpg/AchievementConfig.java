package kr.ishs.raibit.ishsMinecraftRpg;

import kr.ishs.raibit.ishsMinecraftRpg.config.Constants;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 도전과제(업적) 정의와 문구. achievements.yml 에서 읽는다. (기획팀이 코드 없이 수정 가능)
 * /도전과제 GUI 와 달성 알림 문구가 모두 여기서 나온다.
 * 어떤 일을 하면 어떤 도전과제를 주는지는 gameplay.yml 에서 도전과제 ID 로 연결한다.
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

    // 문구 템플릿 (achievements.yml 의 texts). 사용하는 곳에서 Messages.apply 로 자리표시자를 채운다.
    public static String guiTitle = "";
    public static String progressTitle = "";
    public static String progressCount = "";
    public static String progressPercent = "";
    public static String unlockedName = "";
    public static String unlockedDescription = "";
    public static String unlockedLabel = "";
    public static String lockedName = "";
    public static String lockedDescription = "";
    public static String lockedLabel = "";
    public static String popupTitle = "";
    public static String popupSubtitle = "";
    public static List<String> popupChat = new ArrayList<>();

    /** achievements.yml 을 읽어 정의와 문구를 갱신합니다. */
    public static void load(FileConfiguration config, Logger logger) {
        DEFS.clear();

        guiTitle = text(config, "gui-title", logger);
        progressTitle = text(config, "progress-title", logger);
        progressCount = text(config, "progress-count", logger);
        progressPercent = text(config, "progress-percent", logger);
        unlockedName = text(config, "unlocked-name", logger);
        unlockedDescription = text(config, "unlocked-description", logger);
        unlockedLabel = text(config, "unlocked-label", logger);
        lockedName = text(config, "locked-name", logger);
        lockedDescription = text(config, "locked-description", logger);
        lockedLabel = text(config, "locked-label", logger);
        popupTitle = text(config, "popup-title", logger);
        popupSubtitle = text(config, "popup-subtitle", logger);
        popupChat = new ArrayList<>(config.getStringList("texts.popup-chat"));

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
            if (material == null || Constants.isAir(material)) {
                logger.warning("[achievements.yml] '" + id + "' 의 material '" + materialName
                        + "' 을(를) 찾을 수 없어 PAPER로 대체합니다.");
                material = Material.PAPER;
            }

            String title = Messages.color(s.getString("title", id));
            String description = Messages.color(s.getString("description", ""));
            String popup = s.contains("popup") ? Messages.color(s.getString("popup", description)) : description;
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

    private static String text(FileConfiguration config, String key, Logger logger) {
        String path = "texts." + key;
        if (!config.contains(path)) {
            logger.warning("[achievements.yml] '" + path + "' 문구가 없습니다.");
            return "";
        }
        return config.getString(path, "");
    }
}
