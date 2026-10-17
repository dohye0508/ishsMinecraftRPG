package kr.ishs.raibit.ishsMinecraftRpg;

import kr.ishs.raibit.ishsMinecraftRpg.npc.NpcConfig;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * configs/ 의 설정 파일들끼리, 그리고 설정 파일과 코드가 서로 어긋나지 않는지 검사한다.
 * 기획팀이 파일을 고친 뒤에도 이 테스트(./gradlew test)로 연결이 깨지지 않았는지 확인할 수 있다.
 */
class ConfigIntegrityTest {

    /** messages.yml 문구 안의 {자리표시자} 중, 코드가 값을 채워 주는 것들 (키 → 허용 목록). 여기 없는 키는 자리표시자가 없어야 한다. */
    private static final Map<String, Set<String>> PLACEHOLDERS = new HashMap<>();

    static {
        put("level-up.title", "level");
        put("level-up.broadcast", "player", "level");
        put("entry.status", "state");
        put("reward.mining-stat-point", "points");
        put("reward.kill-stat-point", "points");
        put("reward.pvp-win", "points");
        put("club.bonus-lore", "bonuses");
        put("club.bonus-entry", "stat", "amount");
        put("club.current", "club");
        put("club.left", "club");
        put("club.joined", "club");
        put("stat.gui-player", "player");
        put("stat.gui-stat", "label", "value");
        put("stat.gui-hint", "click", "shift");
        put("stat.gui-info", "level", "exp", "exp-required", "points");
        put("stat.actionbar", "hp", "max-hp", "mp", "max-mp");
        put("quest.entry", "name", "left");
        put("item.id-list", "ids");
        put("item.player-not-found", "player");
        put("item.unknown-item", "id");
        put("item.received", "item");
        put("item.given", "player", "item");
        put("reload.auth", "state");
        put("reload.border", "state");
        put("reload.border-range", "min-x", "max-x", "min-z", "max-z");
        put("reload.spawn", "x", "y", "z");
        put("reload.content", "items", "achievements", "clubs");
        put("reload.npc", "count");
        put("npc.list-header", "count");
        put("npc.list-line", "key", "name", "x", "y", "z", "conversation");
        put("npc.bad-key", "key");
        put("npc.already-exists", "key");
        put("npc.unknown-key", "key");
        put("npc.moved", "key");
        put("npc.removed", "key");
        put("npc.write-failed", "error");
        put("npc.added", "key", "name");
        put("scoreboard.lines", "level", "player", "exp", "exp-required", "str", "dex", "int", "hp", "wis",
                "club", "cooldown", "mp", "max-mp", "club-ranking");
        put("scoreboard.cooldown", "seconds");
        put("scoreboard.ranking-line", "rank", "club", "score");
        put("tab.header", "online");
        put("tab.footer", "online");
        put("skill.heal", "amount");
        put("auth.list-ok", "player", "info");
        put("auth.list-fail", "player");
        put("auth.list-footer", "count", "total");
        put("auth.list-info-student", "name", "id");
        put("auth.list-info-teacher", "name");
        put("auth.info-student", "player", "name", "id", "generation");
        put("auth.info-teacher", "player", "name");
        put("auth.success-student", "name", "id", "generation");
        put("auth.success-teacher", "name");
        put("auth.join-student", "player", "name", "id");
        put("auth.join-teacher", "player", "name");
        put("auth.quit-student", "player", "name", "id");
        put("auth.quit-teacher", "player", "name");
        put("auth.failed-retry", "remaining");
    }

    private static void put(String key, String... placeholders) {
        PLACEHOLDERS.put(key, new HashSet<>(Arrays.asList(placeholders)));
    }

    // ===== 공통 =====

    @Test
    void everyYamlFileUnderConfigsParses() throws IOException {
        List<Path> yamls;
        try (Stream<Path> walk = Files.walk(Path.of("configs"))) {
            yamls = walk.filter(p -> p.toString().endsWith(".yml")).collect(Collectors.toList());
        }
        assertTrue(yamls.size() >= 10, "configs 에서 yml 을 충분히 찾지 못했습니다: " + yamls.size());
        for (Path yaml : yamls) {
            assertNotNull(TestSupport.yaml(yaml), yaml.toString());
        }
    }

    /** 설정 파일을 실제 로더로 읽었을 때 경고(잘못된 재료 이름, 겹치는 칸, 빠진 값 등)가 없는지 */
    @Test
    void settingsFilesLoadWithoutWarnings() {
        List<String> warnings = new ArrayList<>();
        java.util.logging.Logger log = java.util.logging.Logger.getLogger("test-integrity-" + System.nanoTime());
        log.setUseParentHandlers(false);
        log.addHandler(new java.util.logging.Handler() {
            @Override public void publish(java.util.logging.LogRecord record) { warnings.add(record.getMessage()); }
            @Override public void flush() {}
            @Override public void close() {}
        });

        kr.ishs.raibit.ishsMinecraftRpg.config.GameplayConfig.load(TestSupport.plugin("gameplay.yml"), log);
        kr.ishs.raibit.ishsMinecraftRpg.config.ClubConfig.load(TestSupport.plugin("clubs.yml"), log);
        AchievementConfig.load(TestSupport.plugin("achievements.yml"), log);

        assertTrue(warnings.isEmpty(), "설정 파일을 읽는 중 경고: " + warnings);
    }

    // ===== messages.yml =====

    /** messages.yml 의 모든 문구 키(점으로 이어진 경로) */
    private static Set<String> messageKeys(YamlConfiguration messages) {
        Set<String> keys = new TreeSet<>();
        for (String key : messages.getKeys(true)) {
            if (!messages.isConfigurationSection(key)) keys.add(key);
        }
        return keys;
    }

    private static String javaTextExcept(String... excludedFileNames) throws IOException {
        Set<String> excluded = new HashSet<>(Arrays.asList(excludedFileNames));
        StringBuilder all = new StringBuilder();
        for (Path p : TestSupport.javaSources()) {
            if (excluded.contains(p.getFileName().toString())) continue;
            all.append(Files.readString(p, StandardCharsets.UTF_8)).append('\n');
        }
        return all.toString();
    }

    @Test
    void everyMessageKeyUsedInCodeExistsInMessagesYml() throws IOException {
        YamlConfiguration messages = TestSupport.plugin("messages.yml");
        Set<String> keys = messageKeys(messages);
        String prefixes = String.join("|", messages.getKeys(false));

        // MapConfig 는 config.yml 의 auth.* 경로를 읽으므로, 문구 키와 이름이 겹쳐서 검사에서 뺀다
        String code = javaTextExcept("MapConfig.java");
        Matcher m = Pattern.compile("\"((?:" + prefixes + ")\\.[a-z0-9][a-z0-9.\\-]*)\"").matcher(code);

        List<String> missing = new ArrayList<>();
        while (m.find()) {
            if (!keys.contains(m.group(1))) missing.add(m.group(1));
        }
        assertTrue(missing.isEmpty(), "코드가 쓰는데 messages.yml 에 없는 문구 키: " + missing);
    }

    @Test
    void noUnusedMessageKeys() throws IOException {
        YamlConfiguration messages = TestSupport.plugin("messages.yml");
        String code = javaTextExcept("MapConfig.java");

        List<String> unused = new ArrayList<>();
        for (String key : messageKeys(messages)) {
            if (!code.contains("\"" + key + "\"")) unused.add(key);
        }
        assertTrue(unused.isEmpty(), "messages.yml 에는 있지만 코드에서 쓰지 않는 문구(고쳐도 반영되지 않음): " + unused);
    }

    @Test
    void messagePlaceholdersAreOnesTheCodeFills() {
        YamlConfiguration messages = TestSupport.plugin("messages.yml");
        Pattern token = Pattern.compile("\\{([a-z0-9-]+)\\}");

        List<String> problems = new ArrayList<>();
        for (String key : messageKeys(messages)) {
            List<String> lines = messages.isList(key) ? messages.getStringList(key) : List.of(messages.getString(key, ""));
            Set<String> allowed = PLACEHOLDERS.getOrDefault(key, Set.of());
            for (String line : lines) {
                Matcher m = token.matcher(line);
                while (m.find()) {
                    if (!allowed.contains(m.group(1))) {
                        problems.add(key + " 의 {" + m.group(1) + "} (허용: " + allowed + ")");
                    }
                }
            }
        }
        assertTrue(problems.isEmpty(), "코드가 채워 주지 않는 자리표시자: " + problems);
    }

    @Test
    void scoreboardTemplateHasOneRankingLine() {
        List<String> lines = TestSupport.plugin("messages.yml").getStringList("scoreboard.lines");
        long rankingLines = lines.stream().filter("{club-ranking}"::equals).count();
        assertEquals(1, rankingLines, "scoreboard.lines 에는 {club-ranking} 한 줄이 정확히 하나 있어야 합니다");
    }

    // ===== 도전과제 연결 =====

    @Test
    void achievementsReferencedByGameplayExist() {
        YamlConfiguration gameplay = TestSupport.plugin("gameplay.yml");
        Set<String> defined = TestSupport.plugin("achievements.yml").getConfigurationSection("achievements").getKeys(false);

        List<String> referenced = new ArrayList<>();
        for (String path : List.of("rewards.kill.achievement", "rewards.death.achievement",
                "rewards.pvp-win.achievement", "rewards.club-coop.achievement")) {
            referenced.add(gameplay.getString(path, ""));
        }
        for (Map<?, ?> special : gameplay.getMapList("rewards.mining.special")) {
            Object achievement = special.get("achievement");
            if (achievement != null) referenced.add(String.valueOf(achievement));
        }
        ConfigurationSection levels = gameplay.getConfigurationSection("leveling.level-achievements");
        for (String level : levels.getKeys(false)) referenced.add(levels.getString(level));

        for (String id : referenced) {
            if (id.isBlank()) continue;
            assertTrue(defined.contains(id), "gameplay.yml 이 연결한 도전과제 '" + id + "' 가 achievements.yml 에 없습니다");
        }
    }

    // ===== 배치(칸 번호) =====

    @Test
    void guiSlotsFitAndDoNotCollide() {
        YamlConfiguration gameplay = TestSupport.plugin("gameplay.yml");
        int size = gameplay.getInt("stats.gui.size");
        Set<Integer> used = new HashSet<>();
        used.add(gameplay.getInt("stats.gui.info-slot"));
        assertTrue(gameplay.getInt("stats.gui.info-slot") < size, "info-slot 이 GUI 크기를 벗어납니다");

        ConfigurationSection attrs = gameplay.getConfigurationSection("stats.attributes");
        for (String id : attrs.getKeys(false)) {
            int slot = attrs.getInt(id + ".slot");
            assertTrue(slot >= 0 && slot < size, id + " slot " + slot + " 이(가) GUI 크기를 벗어납니다");
            assertTrue(used.add(slot), id + " slot " + slot + " 이(가) 다른 칸과 겹칩니다");
            assertNotNull(Material.matchMaterial(attrs.getString(id + ".material", "")), id + " material");
        }
    }

    // ===== 아이템 효과 =====

    @Test
    void itemEffectsAreStructurallyValid() {
        ConfigurationSection items = TestSupport.plugin("items.yml").getConfigurationSection("items");
        Set<String> triggers = Set.of("RIGHT_CLICK", "HIT");
        Set<String> actionTypes = Set.of("HEAL", "POTION", "KNOCKBACK", "LAUNCH");

        for (String id : items.getKeys(false)) {
            ConfigurationSection item = items.getConfigurationSection(id);
            assertNotNull(Material.matchMaterial(item.getString("material", "")), id + " material");
            for (Map<?, ?> effect : item.getMapList("effects")) {
                String trigger = String.valueOf(effect.get("trigger"));
                assertTrue(triggers.contains(trigger), id + " trigger " + trigger);
                if (Boolean.TRUE.equals(effect.get("consume"))) {
                    assertEquals("RIGHT_CLICK", trigger, id + ": consume 은 RIGHT_CLICK 전용");
                }
                Object actions = effect.get("actions");
                assertTrue(actions instanceof List && !((List<?>) actions).isEmpty(), id + " 에 actions 가 없습니다");
                for (Object o : (List<?>) actions) {
                    Map<?, ?> action = (Map<?, ?>) o;
                    String type = String.valueOf(action.get("type"));
                    assertTrue(actionTypes.contains(type), id + " action type " + type);
                    if (type.equals("KNOCKBACK") || type.equals("LAUNCH")) {
                        assertEquals("HIT", trigger, id + ": " + type + " 은 HIT 전용");
                    }
                    if (type.equals("POTION")) {
                        assertNotNull(action.get("potion"), id + " POTION 에 potion 이 없습니다");
                        assertTrue(((Number) action.get("seconds")).doubleValue() > 0, id + " POTION seconds");
                    }
                }
            }
        }
    }

    /** 설정 파일로 옮기기 전 CustomItemListener 에 있던 값과 같은지 (개발용, ./gradlew parityTest) */
    @Test
    @Tag("parity")
    void itemEffectsKeepPreviousBehavior() {
        ConfigurationSection items = TestSupport.plugin("items.yml").getConfigurationSection("items");
        assertEffect(items, "demerit_card", "HIT", 6, false, "POTION", "potion", "WEAKNESS", 5, 2);
        assertEffect(items, "jasmine_flower", "RIGHT_CLICK", 0, true, "HEAL", "amount", 6, 0, 0);
        assertEffect(items, "changbong_spear", "HIT", 0, false, "KNOCKBACK", "strength", 1.8, 0.3, 0);
        assertEffect(items, "changbong_rod", "HIT", 0, false, "LAUNCH", "height", 1.1, 0, 0);

        List<Map<?, ?>> schema = items.getConfigurationSection("schema").getMapList("effects");
        assertEquals(1, schema.size());
        List<?> schemaActions = (List<?>) schema.get(0).get("actions");
        assertEquals(2, schemaActions.size(), "스키마는 힘과 신속 두 가지를 부여한다");
        assertEquals(Set.of("STRENGTH", "SPEED"), schemaActions.stream()
                .map(a -> String.valueOf(((Map<?, ?>) a).get("potion"))).collect(Collectors.toSet()));
        for (Object a : schemaActions) {
            assertEquals(90.0, ((Number) ((Map<?, ?>) a).get("seconds")).doubleValue());
            assertEquals(2, ((Number) ((Map<?, ?>) a).get("level")).intValue());
        }
    }

    /** 효과 하나(첫 번째 action)가 기대한 값인지 확인한다. v1, v2 는 종류에 따라 다른 값(시간/단계, 세기/위로 뜨는 정도 등) */
    private static void assertEffect(ConfigurationSection items, String id, String trigger, double cooldown, boolean consume,
                                     String type, String key, Object expected, double v1, double v2) {
        List<Map<?, ?>> effects = items.getConfigurationSection(id).getMapList("effects");
        assertEquals(1, effects.size(), id + " 효과 개수");
        Map<?, ?> effect = effects.get(0);
        assertEquals(trigger, effect.get("trigger"), id);
        assertEquals(cooldown, effect.get("cooldown") == null ? 0.0 : ((Number) effect.get("cooldown")).doubleValue(), id + " cooldown");
        assertEquals(consume, Boolean.TRUE.equals(effect.get("consume")), id + " consume");

        Map<?, ?> action = (Map<?, ?>) ((List<?>) effect.get("actions")).get(0);
        assertEquals(type, action.get("type"), id);
        Object actual = action.get(key);
        if (expected instanceof Number) {
            assertEquals(((Number) expected).doubleValue(), ((Number) actual).doubleValue(), 1e-9, id + " " + key);
        } else {
            assertEquals(expected, actual, id + " " + key);
        }
        if (type.equals("POTION")) {
            assertEquals(v1, ((Number) action.get("seconds")).doubleValue(), id + " seconds");
            assertEquals(v2, ((Number) action.get("level")).doubleValue(), id + " level");
        } else if (type.equals("KNOCKBACK")) {
            assertEquals(v1, ((Number) action.get("lift")).doubleValue(), 1e-9, id + " lift");
        }
    }

    // ===== 스킬 =====

    @Test
    void skillsAreValid() {
        ConfigurationSection skills = TestSupport.plugin("skills.yml").getConfigurationSection("skills");
        Set<String> types = Set.of("FIREBALL", "EARTHQUAKE", "HOLY_BOMB", "HEAL");
        Set<String> triggers = new HashSet<>();
        for (String id : skills.getKeys(false)) {
            ConfigurationSection s = skills.getConfigurationSection(id);
            assertTrue(types.contains(s.getString("type", "")), id + " type");
            Material material = Material.matchMaterial(s.getString("trigger.material", ""));
            assertNotNull(material, id + " trigger.material");
            String name = s.getString("trigger.name", "");
            assertTrue(!name.isBlank(), id + " trigger.name");
            assertTrue(triggers.add(material + "/" + name), id + ": 같은 재료+이름의 스킬이 둘 이상입니다");
        }
    }

    // ===== BetonQuest 패키지 =====

    @Test
    void questNamesInConfigMatchBetonQuestObjectives() {
        Set<String> objectives = TestSupport.yaml(TestSupport.betonQuest("main.yml").toPath())
                .getConfigurationSection("objectives").getKeys(false);
        ConfigurationSection quests = TestSupport.plugin("config.yml").getConfigurationSection("quests");

        for (String id : quests.getKeys(false)) {
            assertTrue(objectives.contains(id), "config.yml 의 quests 키 '" + id + "' 가 main.yml 의 objectives 에 없습니다");
        }
    }

    @Test
    void betonQuestPackageIsConsistent() {
        YamlConfiguration main = TestSupport.yaml(TestSupport.betonQuest("main.yml").toPath());
        Set<String> events = main.getConfigurationSection("events").getKeys(false);
        ConfigurationSection objectives = main.getConfigurationSection("objectives");
        ConfigurationSection eventDefs = main.getConfigurationSection("events");
        Set<String> items = main.getConfigurationSection("items").getKeys(false);

        // 목표가 가리키는 완료 이벤트와 craft 대상 아이템
        Pattern eventsRef = Pattern.compile("events:([A-Za-z0-9_,]+)");
        Pattern craft = Pattern.compile("^craft\\s+(\\S+)");
        for (String id : objectives.getKeys(false)) {
            String instruction = objectives.getString(id);
            Matcher m = eventsRef.matcher(instruction);
            while (m.find()) {
                for (String ref : m.group(1).split(",")) {
                    assertTrue(events.contains(ref), "objective " + id + " 의 완료 이벤트 '" + ref + "' 가 events 에 없습니다");
                }
            }
            Matcher c = craft.matcher(instruction);
            if (c.find()) {
                assertTrue(items.contains(c.group(1)), "objective " + id + " 가 쓰는 아이템 '" + c.group(1) + "' 가 items 에 없습니다");
            }
        }

        // folder 이벤트가 묶는 하위 이벤트
        for (String id : events) {
            String instruction = eventDefs.getString(id, "");
            if (!instruction.startsWith("folder ")) continue;
            for (String ref : instruction.substring("folder ".length()).trim().split(",")) {
                assertTrue(events.contains(ref.trim()), "event " + id + " 가 묶는 '" + ref + "' 가 events 에 없습니다");
            }
        }
    }

    @Test
    void npcsYmlIsValidAndLinkedToConversations() {
        YamlConfiguration cfg = TestSupport.plugin("npcs.yml");
        java.util.Map<String, NpcConfig.Npc> npcs = NpcConfig.parse(cfg, null);

        // 형식이 틀려 건너뛰어지는 항목이 없어야 한다
        ConfigurationSection raw = cfg.getConfigurationSection("npcs");
        Set<String> declared = raw == null ? Set.of() : raw.getKeys(false);
        assertEquals(declared, npcs.keySet(), "npcs.yml 에서 형식이 잘못되어 무시되는 NPC 가 있습니다");

        for (NpcConfig.Npc npc : npcs.values()) {
            assertTrue(!npc.name.isBlank(), npc.key + " name");
            java.io.File file = TestSupport.betonQuest("conversations/" + npc.conversation + ".yml");
            assertTrue(file.exists(), npc.key + " 의 대화 파일이 없습니다: " + file);
            YamlConfiguration conv = TestSupport.yaml(file.toPath());
            assertTrue(conv.isConfigurationSection("conversations." + npc.conversation),
                    file.getName() + " 안에 '" + npc.conversation + "' 대화가 정의되어 있지 않습니다");
        }

        // main.yml 에는 NPC 연결이 없어야 한다 (NPC 연결은 npcs.yml 에서 자동 생성됨)
        YamlConfiguration main = TestSupport.yaml(TestSupport.betonQuest("main.yml").toPath());
        assertTrue(!main.isSet("npcs") && !main.isSet("npc_conversations"),
                "main.yml 에 npcs/npc_conversations 가 있습니다. NPC 는 npcs.yml 에서만 관리합니다");
    }

    @Test
    void conversationFilesAreAllUsedByAnNpc() {
        java.util.Set<String> used = new HashSet<>();
        for (NpcConfig.Npc npc : NpcConfig.parse(TestSupport.plugin("npcs.yml"), null).values()) used.add(npc.conversation);
        java.io.File[] files = TestSupport.betonQuest("conversations").listFiles((d, n) -> n.endsWith(".yml"));
        for (java.io.File f : files) {
            String name = f.getName().replace(".yml", "");
            assertTrue(used.contains(name), "대화 파일 " + f.getName() + " 을 쓰는 NPC 가 npcs.yml 에 없습니다");
        }
    }
}
