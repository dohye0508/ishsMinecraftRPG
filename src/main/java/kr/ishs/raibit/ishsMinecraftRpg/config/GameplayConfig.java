package kr.ishs.raibit.ishsMinecraftRpg.config;

import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * 게임 규칙과 수치 (스탯 공식, 레벨, 보상, 규칙, 월드 설정). gameplay.yml 에서 읽는다.
 * 기획팀이 코드 없이 수정할 수 있도록, 숫자는 코드가 아니라 이 설정에서만 온다.
 * 설정에 값이 빠져 있으면 임의의 값으로 채우지 않고 콘솔에 경고를 남긴다.
 */
public final class GameplayConfig {

    private GameplayConfig() {}

    /** 코드(PlayerData)에 연결된 5대 스탯 ID. 이름, 아이콘 등 표시 정보는 gameplay.yml 에서 정한다. */
    public static final List<String> STAT_IDS =
            Collections.unmodifiableList(Arrays.asList("str", "dex", "intel", "hp", "wis"));

    /** 스탯 하나의 표시 정보와 시작 값 */
    public static final class StatDef {
        public final String id;
        public final String label;       // 짧은 이름 (예: 근력)
        public final String guiName;     // 스탯 GUI 아이템 이름
        public final Material material;  // 스탯 GUI 아이콘
        public final int slot;           // 스탯 GUI 칸 번호
        public final int starting;       // 새 캐릭터 시작 값

        StatDef(String id, String label, String guiName, Material material, int slot, int starting) {
            this.id = id;
            this.label = label;
            this.guiName = guiName;
            this.material = material;
            this.slot = slot;
            this.starting = starting;
        }
    }

    /** 채굴 시 특별 보상 한 건 (특정 블록이면 추가 경험치/도전과제) */
    public static final class MiningSpecial {
        public final Set<Material> blocks;     // 이 블록이면 적용 (비어 있을 수 있음)
        public final String nameContains;      // 블록 이름에 이 글자가 있으면 적용 (없으면 null)
        public final double exp;
        public final String achievement;       // 없으면 null

        MiningSpecial(Set<Material> blocks, String nameContains, double exp, String achievement) {
            this.blocks = blocks;
            this.nameContains = nameContains;
            this.exp = exp;
            this.achievement = achievement;
        }

        public boolean matches(Material material) {
            if (blocks.contains(material)) return true;
            return nameContains != null && material.toString().contains(nameContains);
        }
    }

    private static final Map<String, StatDef> STATS = new LinkedHashMap<>();

    // ---- stats ----
    public static int statGuiSize;
    public static int statInfoSlot;
    public static int clickAmount;
    public static int shiftClickAmount;
    public static int startingPoints;
    public static double hpBase, hpPerPoint;
    public static double mpBase, mpPerPoint;
    public static double walkSpeedBase, walkSpeedPerDex, walkSpeedMaxBonus;
    public static double manaRegenBase, manaRegenPerIntel;

    // ---- leveling ----
    public static int maxLevel;
    public static double expBase, expExponent;
    public static double pointsBase;
    public static int pointsPerLevels;
    public static double pointsAutoRatio, pointsJobRatio;
    public static int broadcastEveryLevels;
    private static final NavigableMap<Integer, String> LEVEL_ACHIEVEMENTS = new TreeMap<>();

    // ---- rewards ----
    public static double killExp;
    public static double killStatPointChance;
    public static int killStatPoints;
    public static String killAchievement;

    public static double miningExp;
    public static double miningStatPointChance;
    public static int miningStatPoints;
    private static final List<MiningSpecial> MINING_SPECIALS = new ArrayList<>();

    public static String deathAchievement;
    public static String pvpWinAchievement;
    public static int pvpWinStatPoints;

    public static double coopRadius;
    public static double coopExp;
    public static String coopAchievement;

    // ---- rules ----
    public static boolean survivalBlockBreak;
    public static boolean spawnAnimals;
    public static boolean allowTimeSkip;
    public static boolean hungerLoss;
    public static boolean explosionBlockDamage;
    public static boolean glowingPlayers;
    public static boolean pvpRequireClub;
    public static boolean pvpBlockSameClub;

    // ---- world ----
    private static final Map<String, Object> GAME_RULES = new LinkedHashMap<>();
    private static final Map<String, Object> OVERWORLD_GAME_RULES = new LinkedHashMap<>();
    public static boolean clearWeather;

    /** gameplay.yml 을 읽어 값을 갱신합니다. */
    public static void load(FileConfiguration c, Logger log) {
        // ---- 스탯 표시 정보 ----
        STATS.clear();
        ConfigurationSection attrs = c.getConfigurationSection("stats.attributes");
        for (String id : STAT_IDS) {
            ConfigurationSection s = attrs == null ? null : attrs.getConfigurationSection(id);
            if (s == null) {
                log.warning("[gameplay.yml] stats.attributes." + id + " 항목이 없습니다.");
                s = new MemoryConfiguration();
            }
            STATS.put(id, new StatDef(id,
                    s.getString("label", id),
                    s.getString("gui-name", id),
                    material(s.getString("material"), Material.STONE, "stats.attributes." + id, log),
                    s.getInt("slot", 0),
                    s.getInt("starting", 0)));
        }

        statGuiSize = (int) num(c, "stats.gui.size", log);
        statInfoSlot = (int) num(c, "stats.gui.info-slot", log);
        clickAmount = (int) num(c, "stats.click-amount", log);
        shiftClickAmount = (int) num(c, "stats.shift-click-amount", log);
        startingPoints = (int) num(c, "stats.starting-points", log);
        hpBase = num(c, "stats.max-hp.base", log);
        hpPerPoint = num(c, "stats.max-hp.per-point", log);
        mpBase = num(c, "stats.max-mp.base", log);
        mpPerPoint = num(c, "stats.max-mp.per-point", log);
        walkSpeedBase = num(c, "stats.walk-speed.base", log);
        walkSpeedPerDex = num(c, "stats.walk-speed.bonus-per-dex-point", log);
        walkSpeedMaxBonus = num(c, "stats.walk-speed.max-bonus-ratio", log);
        manaRegenBase = num(c, "stats.mana-regen.base-per-second", log);
        manaRegenPerIntel = num(c, "stats.mana-regen.per-intel-point", log);

        // ---- 레벨 ----
        maxLevel = (int) num(c, "leveling.max-level", log);
        expBase = num(c, "leveling.required-exp.base", log);
        expExponent = num(c, "leveling.required-exp.exponent", log);
        pointsBase = num(c, "leveling.level-up-points.base", log);
        pointsPerLevels = Math.max(1, (int) num(c, "leveling.level-up-points.per-levels", log));
        pointsAutoRatio = num(c, "leveling.level-up-points.auto-ratio", log);
        pointsJobRatio = num(c, "leveling.level-up-points.job-ratio", log);
        broadcastEveryLevels = (int) num(c, "leveling.broadcast-every-levels", log);

        LEVEL_ACHIEVEMENTS.clear();
        ConfigurationSection la = c.getConfigurationSection("leveling.level-achievements");
        if (la != null) {
            for (String key : la.getKeys(false)) {
                try {
                    LEVEL_ACHIEVEMENTS.put(Integer.parseInt(key.trim()), la.getString(key));
                } catch (NumberFormatException e) {
                    log.warning("[gameplay.yml] leveling.level-achievements 의 '" + key + "' 은(는) 레벨 숫자가 아닙니다.");
                }
            }
        }

        // ---- 보상 ----
        killExp = num(c, "rewards.kill.exp", log);
        killStatPointChance = num(c, "rewards.kill.stat-point-chance", log);
        killStatPoints = (int) num(c, "rewards.kill.stat-points", log);
        killAchievement = text(c, "rewards.kill.achievement");

        miningExp = num(c, "rewards.mining.exp", log);
        miningStatPointChance = num(c, "rewards.mining.stat-point-chance", log);
        miningStatPoints = (int) num(c, "rewards.mining.stat-points", log);
        MINING_SPECIALS.clear();
        for (Map<?, ?> raw : c.getMapList("rewards.mining.special")) {
            ConfigurationSection s = new MemoryConfiguration().createSection("special", raw);
            Set<Material> blocks = EnumSet.noneOf(Material.class);
            for (String name : s.getStringList("blocks")) {
                Material m = material(name, null, "rewards.mining.special.blocks", log);
                if (m != null) blocks.add(m);
            }
            String contains = text(s, "name-contains");
            if (blocks.isEmpty() && contains == null) {
                log.warning("[gameplay.yml] rewards.mining.special 항목에 blocks 또는 name-contains 가 없어 건너뜁니다.");
                continue;
            }
            MINING_SPECIALS.add(new MiningSpecial(blocks, contains, s.getDouble("exp", 0.0), text(s, "achievement")));
        }

        deathAchievement = text(c, "rewards.death.achievement");
        pvpWinAchievement = text(c, "rewards.pvp-win.achievement");
        pvpWinStatPoints = (int) num(c, "rewards.pvp-win.stat-points", log);

        coopRadius = num(c, "rewards.club-coop.radius", log);
        coopExp = num(c, "rewards.club-coop.exp", log);
        coopAchievement = text(c, "rewards.club-coop.achievement");

        // ---- 규칙 ----
        survivalBlockBreak = c.getBoolean("rules.survival-block-break");
        spawnAnimals = c.getBoolean("rules.spawn-animals");
        allowTimeSkip = c.getBoolean("rules.allow-time-skip");
        hungerLoss = c.getBoolean("rules.hunger-loss");
        explosionBlockDamage = c.getBoolean("rules.explosion-block-damage");
        glowingPlayers = c.getBoolean("rules.glowing-players");
        pvpRequireClub = c.getBoolean("rules.pvp.require-club");
        pvpBlockSameClub = c.getBoolean("rules.pvp.block-same-club");

        // ---- 월드 ----
        loadRules(c, "world.game-rules", GAME_RULES);
        loadRules(c, "world.overworld-game-rules", OVERWORLD_GAME_RULES);
        clearWeather = c.getBoolean("world.clear-weather");
    }

    // ===== 계산 =====

    public static StatDef stat(String id) {
        return STATS.get(id);
    }

    public static List<StatDef> stats() {
        return new ArrayList<>(STATS.values());
    }

    /** 칸 번호로 스탯 찾기 (스탯 GUI 클릭 처리용). 없으면 null */
    public static StatDef statBySlot(int slot) {
        for (StatDef s : STATS.values()) if (s.slot == slot) return s;
        return null;
    }

    /** 체력 스탯 포인트로 계산한 최대 HP */
    public static double maxHp(int hpPoints) {
        return hpBase + (hpPoints * hpPerPoint);
    }

    /** 지능 스탯 포인트로 계산한 최대 MP */
    public static double maxMp(int intelPoints) {
        return mpBase + (intelPoints * mpPerPoint);
    }

    /** 민첩 스탯 포인트로 계산한 걷기 속도 (상한 적용) */
    public static float walkSpeed(int dexPoints) {
        float baseSpeed = (float) walkSpeedBase;
        float speedBonus = (float) (dexPoints * walkSpeedPerDex);
        return baseSpeed + (baseSpeed * Math.min(speedBonus, (float) walkSpeedMaxBonus));
    }

    /** 초당 마나 재생량 */
    public static double manaRegenPerSecond(int intelPoints) {
        return manaRegenBase + (manaRegenPerIntel * intelPoints);
    }

    /** 해당 레벨에서 다음 레벨로 오르는 데 필요한 경험치 (최대 레벨이면 사실상 무한) */
    public static double requiredExp(int level) {
        if (level >= maxLevel) return Double.MAX_VALUE;
        return Math.floor(expBase * Math.pow(level, expExponent));
    }

    /**
     * 레벨업 시 지급되는 자율 분배 스탯 포인트.
     * 총 포인트 = base + floor(레벨 / per-levels). 이 중 auto-ratio 몫은 5대 스탯 전부에 +1씩(스탯 개수만큼),
     * job-ratio 몫은 직업 주력 스탯에 쓰이는 것으로 계산해 빼고 남은 만큼이 자율 포인트가 된다.
     * (기존 코드의 계산식을 그대로 옮긴 것이며, 설정값에 따라 음수가 나올 수 있다)
     */
    public static int levelUpFreePoints(int newLevel) {
        int total = (int) (pointsBase + Math.floor(newLevel / (double) pointsPerLevels));
        int autoAssigned = (int) Math.ceil(total * pointsAutoRatio) * STAT_IDS.size();
        int jobPoints = (int) Math.floor(total * pointsJobRatio);
        return total - autoAssigned - jobPoints;
    }

    /** 이 레벨에 도달했을 때 지급할 도전과제 ID 목록 (낮은 레벨부터) */
    public static List<String> achievementsUpTo(int level) {
        return new ArrayList<>(LEVEL_ACHIEVEMENTS.headMap(level, true).values());
    }

    public static List<MiningSpecial> miningSpecials() {
        return Collections.unmodifiableList(MINING_SPECIALS);
    }

    // ===== 월드 규칙 적용 =====

    /** 월드에 게임 규칙(world.game-rules, 오버월드는 overworld-game-rules 추가)을 적용합니다. */
    public static void applyWorldRules(World world, boolean overworld, Logger log) {
        for (Map.Entry<String, Object> e : GAME_RULES.entrySet()) applyRule(world, e.getKey(), e.getValue(), log);
        if (overworld) {
            for (Map.Entry<String, Object> e : OVERWORLD_GAME_RULES.entrySet()) applyRule(world, e.getKey(), e.getValue(), log);
            if (clearWeather) {
                world.setStorm(false);
                world.setThundering(false);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void applyRule(World world, String name, Object value, Logger log) {
        try {
            GameRule rule = (GameRule) GameRule.class.getField(name.trim().toUpperCase(Locale.ROOT)).get(null);
            Object converted = value;
            if (rule.getType() == Boolean.class) {
                converted = value instanceof Boolean ? value : Boolean.parseBoolean(String.valueOf(value));
            } else if (rule.getType() == Integer.class) {
                converted = value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(String.valueOf(value));
            }
            world.setGameRule(rule, converted);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            log.warning("[gameplay.yml] 게임 규칙 '" + name + "' 을(를) 적용할 수 없습니다. (이름 또는 값 확인)");
        }
    }

    // ===== 내부 도우미 =====

    private static void loadRules(FileConfiguration c, String path, Map<String, Object> target) {
        target.clear();
        ConfigurationSection s = c.getConfigurationSection(path);
        if (s == null) return;
        for (String key : s.getKeys(false)) target.put(key, s.get(key));
    }

    /** 숫자 설정 읽기. 값이 없으면 0 으로 두되 콘솔에 경고한다. */
    private static double num(ConfigurationSection c, String path, Logger log) {
        if (!c.contains(path)) {
            log.warning("[gameplay.yml] '" + path + "' 값이 없어 0 으로 처리합니다.");
            return 0.0;
        }
        return c.getDouble(path);
    }

    /** 비어 있지 않은 문자열 설정이면 그 값, 아니면 null */
    private static String text(ConfigurationSection c, String path) {
        String v = c.getString(path);
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static Material material(String name, Material fallback, String where, Logger log) {
        if (name == null || name.isBlank()) return fallback;
        Material m = Material.matchMaterial(name.trim());
        if (m == null) {
            log.warning("[gameplay.yml] " + where + " 의 재료 '" + name + "' 을(를) 찾을 수 없습니다.");
            return fallback;
        }
        return m;
    }
}
