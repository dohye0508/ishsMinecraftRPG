package kr.ishs.raibit.ishsMinecraftRpg.npc;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * npcs.yml 에서 읽은 NPC 목록. 서버(Citizens) 없이도 읽고 검증할 수 있다.
 * 실제 NPC 생성은 {@link NpcManager} 가 한다.
 */
public final class NpcConfig {

    private NpcConfig() {}

    /** NPC 키: 영어 소문자/숫자/밑줄. BetonQuest 의 NPC 이름으로도 쓰이므로 이 형식만 허용한다. */
    public static final Pattern KEY_PATTERN = Pattern.compile("[a-z][a-z0-9_]*");

    public static final class Npc {
        public final String key;
        public final String name;
        public final EntityType type;      // NPC 형태 (기본 PLAYER, 예: CAT)
        public final String skin;          // 비어 있으면 스킨 지정 없음 (PLAYER 형태에만 적용)
        public final String variant;       // 형태별 세부 종류 (CAT 의 무늬 등). 비어 있으면 지정 없음
        public final boolean sitting;      // 앉아 있는 자세 (CAT 등)
        public final String world;         // 비어 있으면 기본 월드
        public final double x, y, z;
        public final float yaw, pitch;
        public final boolean lookAtPlayer;
        public final String conversation;  // BetonQuest 대화 이름 (conversations/<이름>.yml)

        Npc(String key, String name, EntityType type, String skin, String variant, boolean sitting, String world,
            double x, double y, double z, float yaw, float pitch, boolean lookAtPlayer, String conversation) {
            this.key = key;
            this.name = name;
            this.type = type;
            this.skin = skin;
            this.variant = variant;
            this.sitting = sitting;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.lookAtPlayer = lookAtPlayer;
            this.conversation = conversation;
        }
    }

    private static Map<String, Npc> npcs = Collections.emptyMap();

    public static void load(FileConfiguration cfg, Logger log) {
        npcs = parse(cfg, log);
    }

    public static Map<String, Npc> all() {
        return npcs;
    }

    /** 테스트와 로드에서 함께 쓴다. 잘못된 항목은 경고하고 건너뛴다. */
    public static Map<String, Npc> parse(FileConfiguration cfg, Logger log) {
        Map<String, Npc> out = new LinkedHashMap<>();
        ConfigurationSection root = cfg.getConfigurationSection("npcs");
        if (root == null) return out;

        double ox = cfg.getDouble("offset.x", 0), oy = cfg.getDouble("offset.y", 0), oz = cfg.getDouble("offset.z", 0);

        for (String key : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(key);
            if (s == null) continue;
            if (!KEY_PATTERN.matcher(key).matches()) {
                warn(log, key, "NPC 키는 영어 소문자/숫자/밑줄만 쓸 수 있습니다. (예: vice_principal)");
                continue;
            }
            if (!s.isConfigurationSection("location")
                    || !s.isSet("location.x") || !s.isSet("location.y") || !s.isSet("location.z")) {
                warn(log, key, "location 의 x, y, z 가 필요합니다.");
                continue;
            }
            out.put(key, new Npc(
                    key,
                    s.getString("name", key),
                    parseType(s.getString("type", "PLAYER"), key, log),
                    s.getString("skin", ""),
                    s.getString("variant", ""),
                    s.getBoolean("sitting", false),
                    s.getString("world", ""),
                    s.getDouble("location.x") + ox,
                    s.getDouble("location.y") + oy,
                    s.getDouble("location.z") + oz,
                    (float) s.getDouble("location.yaw", 0),
                    (float) s.getDouble("location.pitch", 0),
                    s.getBoolean("look-at-player", true),
                    s.getString("conversation", key)));
        }
        return out;
    }

    /**
     * BetonQuest 에 알려 줄 NPC 연결 파일 내용. NPC 키별로 실제 Citizens ID 를 연결한다.
     * (BetonQuest 는 패키지 폴더의 모든 yml 을 합쳐 읽으므로 main.yml 을 건드리지 않아도 된다)
     */
    public static String betonQuestBindings(Map<String, Integer> citizensIds, Map<String, Npc> defs) {
        List<String> keys = new ArrayList<>(citizensIds.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        sb.append("# 자동 생성 파일입니다. 직접 수정하지 마세요. (npcs.yml 을 고치면 서버가 다시 만듭니다)\n");
        sb.append("npcs:\n");
        for (String k : keys) sb.append("  ").append(k).append(": \"citizens ").append(citizensIds.get(k)).append("\"\n");
        sb.append("npc_conversations:\n");
        for (String k : keys) sb.append("  ").append(k).append(": ").append(defs.get(k).conversation).append("\n");
        return sb.toString();
    }

    /** 형태 이름(EntityType). 알 수 없거나 NPC 로 쓸 수 없으면 경고하고 PLAYER. */
    private static EntityType parseType(String raw, String key, Logger log) {
        try {
            EntityType type = EntityType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            if (type.isAlive() || type == EntityType.PLAYER) return type;
        } catch (IllegalArgumentException ignored) {
            // 아래에서 경고
        }
        if (log != null) log.warning("[npcs.yml] '" + key + "' 의 type '" + raw + "' 를 쓸 수 없어 PLAYER 로 만듭니다. (예: PLAYER, CAT)");
        return EntityType.PLAYER;
    }

    private static void warn(Logger log, String key, String why) {
        if (log != null) log.warning("[npcs.yml] '" + key + "' 를 건너뜁니다. " + why);
    }
}
