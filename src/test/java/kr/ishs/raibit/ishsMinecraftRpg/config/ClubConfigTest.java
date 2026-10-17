package kr.ishs.raibit.ishsMinecraftRpg.config;

import kr.ishs.raibit.ishsMinecraftRpg.TestSupport;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubConfigTest {

    /** 설정 파일로 옮기기 전 ClubListener 의 switch 와 ClubGUI 에 있던 값 */
    private static final String[][] PREVIOUS = {
            // id, material, slot, bonus(stat=amount,...)
            {"LOGIC", "BOOK", "10", "intel=15,hp=5"},
            {"WIN", "ANVIL", "11", "str=10,dex=10"},
            {"TNT", "TNT", "12", "str=10,intel=10"},
            {"LOTTOL", "WHEAT", "13", "hp=15,dex=5"},
            {"QUASAR", "ENDER_PEARL", "14", "intel=10,dex=10"},
            {"RAIBIT", "REDSTONE", "15", "dex=15,intel=5"},
            {"PLUTONIUM", "IRON_BLOCK", "16", "str=10,hp=10"},
    };

    private static final Logger LOG = Logger.getLogger("test-clubs");

    private static List<String> warnings() {
        List<String> out = new ArrayList<>();
        LOG.setUseParentHandlers(false);
        for (Handler h : LOG.getHandlers()) LOG.removeHandler(h);
        LOG.addHandler(new Handler() {
            @Override public void publish(LogRecord record) { out.add(record.getMessage()); }
            @Override public void flush() {}
            @Override public void close() {}
        });
        return out;
    }

    @BeforeAll
    static void loadDefaults() {
        ClubConfig.load(TestSupport.plugin("clubs.yml"), LOG);
    }

    @Test
    @Tag("parity")
    void defaultClubsMatchPreviousHardcodedValues() {
        List<String> warnings = warnings();
        ClubConfig.load(TestSupport.plugin("clubs.yml"), LOG);
        assertTrue(warnings.isEmpty(), "clubs.yml 을 읽는 중 경고: " + warnings);

        List<ClubConfig.Club> clubs = ClubConfig.all();
        assertEquals(PREVIOUS.length, clubs.size());
        assertEquals(27, ClubConfig.guiSize);

        for (int i = 0; i < PREVIOUS.length; i++) {
            ClubConfig.Club club = clubs.get(i);
            String[] expected = PREVIOUS[i];
            assertEquals(expected[0], club.id, "순서");
            assertEquals(Material.matchMaterial(expected[1]), club.material, club.id + " 아이콘");
            assertEquals(Integer.parseInt(expected[2]), club.slot, club.id + " 칸");

            Map<String, Integer> bonus = new LinkedHashMap<>();
            for (String pair : expected[3].split(",")) {
                String[] kv = pair.split("=");
                bonus.put(kv[0], Integer.parseInt(kv[1]));
            }
            assertEquals(bonus, club.bonus, club.id + " 보너스");
            // 적은 순서까지 같아야 "지능 +15, 체력 +5" 같은 설명 순서가 유지된다
            assertEquals(new ArrayList<>(bonus.keySet()), new ArrayList<>(club.bonus.keySet()), club.id + " 보너스 순서");
        }
    }

    @Test
    @Tag("parity")
    void displayNameAndPrefixKeepPreviousLook() {
        ClubConfig.Club logic = ClubConfig.get("LOGIC");
        assertEquals("§bLOGIC (수학)", logic.displayName());
        assertEquals("§b📘 LOGIC", logic.prefix());
        assertEquals("§8⚙ PLUTONIUM", ClubConfig.get("PLUTONIUM").prefix());
    }

    @Test
    @Tag("parity")
    void lookupsByIdAndSlot() {
        assertNotNull(ClubConfig.get("logic"), "대소문자 무시");
        assertEquals("WIN", ClubConfig.bySlot(11).id);
        assertNull(ClubConfig.bySlot(0));
        assertNull(ClubConfig.get("NOPE"));
        assertNull(ClubConfig.get(null));
    }

    @Test
    void invalidEntriesAreSkippedWithWarnings() {
        YamlConfiguration bad = new YamlConfiguration();
        try {
            bad.loadFromString(String.join("\n",
                    "gui-size: 27",
                    "clubs:",
                    "  GOOD:",
                    "    name: Good",
                    "    material: BOOK",
                    "    slot: 1",
                    "    bonus: {str: 3, notastat: 9}",
                    "  BADMATERIAL:",
                    "    material: NOT_A_MATERIAL",
                    "    slot: 2",
                    "  OUTOFRANGE:",
                    "    material: BOOK",
                    "    slot: 99",
                    "  SAMESLOT:",
                    "    material: BOOK",
                    "    slot: 1"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        List<String> warnings = warnings();
        ClubConfig.load(bad, LOG);

        assertEquals(1, ClubConfig.all().size(), "정상 항목 하나만 남아야 합니다");
        ClubConfig.Club good = ClubConfig.get("GOOD");
        assertNotNull(good);
        assertEquals(Map.of("str", 3), good.bonus, "스탯 ID가 아닌 보너스는 무시");
        assertEquals(4, warnings.size(), "잘못된 항목마다 경고가 하나씩: " + warnings);

        // 다른 테스트가 기본 설정을 쓰도록 되돌린다
        ClubConfig.load(TestSupport.plugin("clubs.yml"), LOG);
    }
}
