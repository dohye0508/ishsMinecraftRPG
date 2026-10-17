package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.config.GameplayConfig;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private int level;
    private double experience;
    private int statPoints;

    // 5대 주스탯
    private int str; // 힘
    private int intel; // 지능
    private int hpStat; // 체력
    private int dex; // 민첩
    private int wis; // 정신력

    // 부스탯 (계산된 값)
    private double currentHp;
    private double maxHp;
    private double currentMp;
    private double maxMp;

    private String club;
    private final Set<String> achievements = new HashSet<>();
    private final Set<String> completedNpcEvents = new HashSet<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.level = 1;
        this.experience = 0.0;
        this.statPoints = GameplayConfig.startingPoints;

        this.str = GameplayConfig.stat("str").starting;
        this.intel = GameplayConfig.stat("intel").starting;
        this.hpStat = GameplayConfig.stat("hp").starting;
        this.dex = GameplayConfig.stat("dex").starting;
        this.wis = GameplayConfig.stat("wis").starting;

        // StatManager.applyStats()와 같은 공식(GameplayConfig)으로 최초 HP/MP를 채워서 시작한다.
        // (새 캐릭터의 실제 체력이 최대체력 대비 극히 일부만 채워진 채로 시작해 한두 대에 죽는 것을 막는다)
        this.maxHp = GameplayConfig.maxHp(this.hpStat);
        this.currentHp = this.maxHp;
        this.maxMp = GameplayConfig.maxMp(this.intel);
        this.currentMp = this.maxMp;

        this.club = null;
    }

    // Getters
    public UUID getUuid() { return uuid; }
    public int getLevel() { return level; }
    public double getExperience() { return experience; }
    public int getStatPoints() { return statPoints; }
    public int getStr() { return str; }
    public int getIntel() { return intel; }
    public int getHpStat() { return hpStat; }
    public int getDex() { return dex; }
    public int getWis() { return wis; }
    public double getCurrentHp() { return currentHp; }
    public double getMaxHp() { return maxHp; }
    public double getCurrentMp() { return currentMp; }
    public double getMaxMp() { return maxMp; }
    public String getClub() { return club; }
    public boolean hasClub() { return this.club != null; }
    public Set<String> getAchievements() { return achievements; }

    // Setters
    public void setLevel(int level) { this.level = level; }
    public void addLevel(int amount) { this.level += amount; }
    public void setExperience(double experience) { this.experience = experience; }
    public void addExperience(double amount) { this.experience += amount; }
    public void setStatPoints(int statPoints) { this.statPoints = statPoints; }
    public void addStatPoints(int amount) { this.statPoints += amount; }
    public void setStr(int str) { this.str = str; }
    public void setIntel(int intel) { this.intel = intel; }
    public void setHpStat(int hpStat) { this.hpStat = hpStat; }
    public void setDex(int dex) { this.dex = dex; }
    public void setWis(int wis) { this.wis = wis; }
    public void setCurrentHp(double currentHp) { this.currentHp = Math.max(0, Math.min(currentHp, this.maxHp)); }
    public void setMaxHp(double maxHp) { this.maxHp = maxHp; }
    public void setCurrentMp(double currentMp) { this.currentMp = Math.max(0, Math.min(currentMp, this.maxMp)); }
    public void setMaxMp(double maxMp) { this.maxMp = maxMp; }
    public void setClub(String club) { this.club = club; }

    /** 스탯 ID(str, dex, intel, hp, wis)로 현재 스탯 값을 읽습니다. */
    public int getStat(String statId) {
        switch (statId) {
            case "str": return str;
            case "dex": return dex;
            case "intel": return intel;
            case "hp": return hpStat;
            case "wis": return wis;
            default: return 0;
        }
    }

    /** 스탯 ID(str, dex, intel, hp, wis)의 스탯을 amount 만큼 올립니다. */
    public void addStat(String statId, int amount) {
        switch (statId) {
            case "str": str += amount; break;
            case "dex": dex += amount; break;
            case "intel": intel += amount; break;
            case "hp": hpStat += amount; break;
            case "wis": wis += amount; break;
            default: break;
        }
    }

    /**
     * 레벨업 처리. 레벨을 올리고 스탯 포인트를 지급합니다.
     * 지급량 계산식은 gameplay.yml 의 leveling.level-up-points 를 따릅니다.
     */
    public void levelUp() {
        this.level++;
        this.addStatPoints(GameplayConfig.levelUpFreePoints(this.level));
    }

    /** 다음 레벨에 필요한 경험치. 공식은 gameplay.yml 의 leveling.required-exp 를 따릅니다. */
    public double getRequiredExp() {
        return GameplayConfig.requiredExp(level);
    }

    public boolean hasAchievement(String trackId) {
        return achievements.contains(trackId);
    }

    public void addAchievement(String trackId) {
        achievements.add(trackId);
    }

    public boolean hasCompletedNpcEvent(UUID npcUuid, String eventId) {
        return completedNpcEvents.contains(npcUuid.toString() + "::" + eventId);
    }

    public void completeNpcEvent(UUID npcUuid, String eventId) {
        completedNpcEvents.add(npcUuid.toString() + "::" + eventId);
    }

    public void saveToYaml(YamlConfiguration config) {
        config.set("level", level);
        config.set("experience", experience);
        config.set("statPoints", statPoints);
        config.set("stats.str", str);
        config.set("stats.intel", intel);
        config.set("stats.hpStat", hpStat);
        config.set("stats.dex", dex);
        config.set("stats.wis", wis);
        config.set("hp.current", currentHp);
        config.set("hp.max", maxHp);
        config.set("mp.current", currentMp);
        config.set("mp.max", maxMp);
        config.set("club", club);
        config.set("achievements", new java.util.ArrayList<>(achievements));
        config.set("completedNpcEvents", new java.util.ArrayList<>(completedNpcEvents));
    }

    public void loadFromYaml(YamlConfiguration config) {
        this.level = config.getInt("level", 1);
        this.experience = config.getDouble("experience", 0.0);
        this.statPoints = config.getInt("statPoints", 0);
        this.str = config.getInt("stats.str", GameplayConfig.stat("str").starting);
        this.intel = config.getInt("stats.intel", GameplayConfig.stat("intel").starting);
        this.hpStat = config.getInt("stats.hpStat", GameplayConfig.stat("hp").starting);
        this.dex = config.getInt("stats.dex", GameplayConfig.stat("dex").starting);
        this.wis = config.getInt("stats.wis", GameplayConfig.stat("wis").starting);
        // HP/MP는 StatManager에서 스탯 기반으로 재계산하므로 저장된 값은 참고용으로만 로드
        this.maxHp = config.getDouble("hp.max", 0);
        this.currentHp = config.getDouble("hp.current", 0);
        this.maxMp = config.getDouble("mp.max", 0);
        this.currentMp = config.getDouble("mp.current", 0);
        this.club = config.getString("club", null);
        List<String> achList = config.getStringList("achievements");
        if (achList != null) {
            achievements.clear();
            achievements.addAll(achList);
        }
        List<String> npcEvents = config.getStringList("completedNpcEvents");
        if (npcEvents != null) {
            completedNpcEvents.clear();
            completedNpcEvents.addAll(npcEvents);
        }
    }
}
