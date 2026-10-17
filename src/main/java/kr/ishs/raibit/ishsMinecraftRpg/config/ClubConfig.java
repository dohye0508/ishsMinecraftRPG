package kr.ishs.raibit.ishsMinecraftRpg.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 동아리(직업) 정의. clubs.yml 에서 읽는다. (기획팀이 코드 없이 추가/수정 가능)
 * 선택 GUI, 가입 시 스탯 보너스, 랭킹 표시가 모두 이 정의 하나를 사용한다.
 */
public final class ClubConfig {

    private ClubConfig() {}

    /** 동아리 하나 */
    public static final class Club {
        public final String id;          // 저장되는 이름 (대문자). 플레이어 데이터와 BetonQuest 에서 쓰인다
        public final String name;        // GUI 에 보이는 이름
        public final String color;       // 색상 코드 (예: &b)
        public final String icon;        // 랭킹 등에 쓰는 아이콘
        public final Material material;  // GUI 아이콘
        public final int slot;           // GUI 칸 번호
        /** 스탯ID → 가입 시 올라가는 양 (clubs.yml 에 적은 순서 유지) */
        public final Map<String, Integer> bonus;

        Club(String id, String name, String color, String icon, Material material, int slot, Map<String, Integer> bonus) {
            this.id = id;
            this.name = name;
            this.color = color;
            this.icon = icon;
            this.material = material;
            this.slot = slot;
            this.bonus = bonus;
        }

        /** GUI 아이템 이름 (색상 적용) */
        public String displayName() {
            return Messages.color(color + name);
        }

        /** 랭킹 등에 쓰는 접두 (색상 + 아이콘 + 동아리 ID) */
        public String prefix() {
            return Messages.color(color + icon + " " + id);
        }
    }

    private static final Map<String, Club> CLUBS = new LinkedHashMap<>();
    public static int guiSize = 27;

    /** clubs.yml 을 읽어 동아리 목록을 갱신합니다. 파일의 순서가 GUI/랭킹 순서입니다. */
    public static void load(FileConfiguration c, Logger log) {
        CLUBS.clear();
        guiSize = c.getInt("gui-size", 27);

        ConfigurationSection section = c.getConfigurationSection("clubs");
        if (section == null) {
            log.warning("[clubs.yml] clubs 항목이 없습니다. 동아리가 없습니다.");
            return;
        }

        for (String rawId : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(rawId);
            if (s == null) continue;
            String id = rawId.trim().toUpperCase(Locale.ROOT);

            String materialName = s.getString("material", "");
            Material material = Material.matchMaterial(materialName);
            if (material == null || Constants.isAir(material)) {
                log.warning("[clubs.yml] " + id + " 의 material '" + materialName + "' 을(를) 찾을 수 없어 건너뜁니다.");
                continue;
            }

            int slot = s.getInt("slot", -1);
            if (slot < 0 || slot >= guiSize) {
                log.warning("[clubs.yml] " + id + " 의 slot " + slot + " 이(가) 0~" + (guiSize - 1) + " 범위를 벗어나 건너뜁니다.");
                continue;
            }
            Club sameSlot = bySlot(slot);
            if (sameSlot != null) {
                log.warning("[clubs.yml] " + id + " 의 slot " + slot + " 이(가) " + sameSlot.id + " 와(과) 겹쳐 건너뜁니다.");
                continue;
            }

            Map<String, Integer> bonus = new LinkedHashMap<>();
            ConfigurationSection b = s.getConfigurationSection("bonus");
            if (b != null) {
                for (String statId : b.getKeys(false)) {
                    if (!GameplayConfig.STAT_IDS.contains(statId)) {
                        log.warning("[clubs.yml] " + id + " 의 bonus '" + statId + "' 은(는) 스탯 ID가 아니어서 무시합니다. ("
                                + String.join(", ", GameplayConfig.STAT_IDS) + ")");
                        continue;
                    }
                    bonus.put(statId, b.getInt(statId));
                }
            }

            CLUBS.put(id, new Club(id, s.getString("name", id), s.getString("color", "&f"),
                    s.getString("icon", ""), material, slot, bonus));
        }
    }

    /** 정의된 순서대로의 전체 동아리 */
    public static List<Club> all() {
        return new ArrayList<>(CLUBS.values());
    }

    /** 동아리 ID로 찾기 (대소문자 무시, 없으면 null) */
    public static Club get(String id) {
        return id == null ? null : CLUBS.get(id.trim().toUpperCase(Locale.ROOT));
    }

    /** GUI 칸 번호로 찾기 (없으면 null) */
    public static Club bySlot(int slot) {
        for (Club club : CLUBS.values()) if (club.slot == slot) return club;
        return null;
    }
}
