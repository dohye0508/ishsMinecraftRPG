package kr.ishs.raibit.ishsMinecraftRpg;

import kr.ishs.raibit.ishsMinecraftRpg.auth.AuthCommand;
import kr.ishs.raibit.ishsMinecraftRpg.auth.AuthListener;
import kr.ishs.raibit.ishsMinecraftRpg.auth.AuthManager;
import kr.ishs.raibit.ishsMinecraftRpg.listener.MapBorderListener;
import kr.ishs.raibit.ishsMinecraftRpg.listener.StatListener;
import kr.ishs.raibit.ishsMinecraftRpg.player.*;
import kr.ishs.raibit.ishsMinecraftRpg.item.CustomItems;
import kr.ishs.raibit.ishsMinecraftRpg.item.CustomItemListener;
import kr.ishs.raibit.ishsMinecraftRpg.item.CustomItemCommand;
import kr.ishs.raibit.ishsMinecraftRpg.stat.GameConfig;
import kr.ishs.raibit.ishsMinecraftRpg.stat.StatCommand;
import kr.ishs.raibit.ishsMinecraftRpg.stat.StatManager;
import kr.ishs.raibit.ishsMinecraftRpg.task.RpgUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class IshsMinecraftRpg extends JavaPlugin {

    private AuthManager authManager;
    private PlayerManager playerManager;
    private StatManager statManager;
    private SkillManager skillManager;
    private RpgListener rpgListener;

    @Override
    public void onEnable() {
        // 데이터 폴더 생성
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // config.yml 로드 (캐릭터 최초 생성 시 기본 스탯 등)
        saveDefaultConfig();
        GameConfig.load(getConfig());
        MapConfig.load(getConfig());
        QuestCommand.loadQuestNames(getConfig());

        // Manager 초기화
        playerManager = PlayerManager.getInstance();
        statManager = new StatManager(this, playerManager);
        authManager = new AuthManager(this);
        skillManager = new SkillManager(this);

        // 커스텀 아이템 태그 초기화
        CustomItems.init(this);

        // items.yml / achievements.yml / help.yml 로드 (없으면 기본 파일 생성)
        reloadContent();

        // 서버에 이미 접속 중인 플레이어가 있다면(리로드 등) 로드
        for (Player p : getServer().getOnlinePlayers()) {
            playerManager.loadPlayer(p);
            // 스탯 적용
            statManager.applyStats(p);
        }

        // 이벤트 리스너 등록
        rpgListener = new RpgListener(this);
        getServer().getPluginManager().registerEvents(rpgListener, this);
        getServer().getPluginManager().registerEvents(new AuthListener(this, authManager), this);
        MapBorderListener mapBorderListener = new MapBorderListener(authManager);
        getServer().getPluginManager().registerEvents(mapBorderListener, this);
        mapBorderListener.startVisualTask(this); // 플레이어별 가상 월드보더로 직사각형 경계를 파란 벽으로 표시
        // listener 패키지의 StatListener만 등록하여 중복을 제거합니다.
        getServer().getPluginManager().registerEvents(new StatListener(playerManager, statManager), this);
        getServer().getPluginManager().registerEvents(new ClubListener(), this);
        getServer().getPluginManager().registerEvents(skillManager, this);
        getServer().getPluginManager().registerEvents(new CustomItemListener(), this);

        // 커맨드 등록
        AuthCommand authCmd = new AuthCommand(this, authManager);
        getCommand("인증").setExecutor(authCmd);
        getCommand("인증정보").setExecutor(authCmd);
        getCommand("인증목록").setExecutor(authCmd);

        getCommand("stat").setExecutor(new StatCommand());
        getCommand("도전과제").setExecutor(new AchievementCommand());
        getCommand("lobby").setExecutor(new LobbyCommand());
        getCommand("도움말").setExecutor(new HelpCommand());
        getCommand("퀘스트").setExecutor(new QuestCommand());
        getCommand("아이템지급").setExecutor(new CustomItemCommand());
        getCommand("입장허용").setExecutor(new EntryGateCommand(rpgListener));
        getCommand("설정리로드").setExecutor(new ReloadCommand(this, mapBorderListener));

        // PlaceholderAPI가 설치되어 있다면 연동
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook(this).register();
        }

        // 반복 태스크
        getServer().getScheduler().runTaskTimer(this, new RpgUpdateTask(this, skillManager), 20L, 20L);
        statManager.startActionBarTask(); // 액션바 업데이트 태스크 시작

        for (World world : Bukkit.getWorlds()) {
            applyWorldSettings(world);
        }

        getLogger().info("ISHSMinecraftRpg 활성화 완료 (스탯 시스템 및 UI 포함)");
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    /** 플러그인 폴더의 YAML 파일을 읽습니다. 파일이 없으면 jar 안의 기본 파일을 먼저 복사합니다. */
    public FileConfiguration loadYaml(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /** items.yml / achievements.yml / help.yml 을 다시 읽어 반영합니다. (/설정리로드 에서도 사용) */
    public void reloadContent() {
        CustomItems.load(loadYaml("items.yml"));
        AchievementConfig.load(loadYaml("achievements.yml"), getLogger());
        HelpCommand.load(loadYaml("help.yml"));
    }

    @Override
    public void onDisable() {
        playerManager.saveAll();
    }

    @SuppressWarnings("removal")
    public void applyWorldSettings(World world) {
        if (world == null) return;

        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false); // 커스텀 회복을 위해 false로 설정
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);

        // 바닐라 월드보더(정사각형)는 쓰지 않는다. 직사각형 경계는 MapBorderListener가 처리한다.
        // 주의: 맵 위치(좌표)가 나중에 바뀔 수 있으니 MapBorderListener, AuthManager의 좌표를 다시 확인할 것
        try {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                world.setSpawnLocation(MapConfig.firstSpawnLocation(world)); // config.yml 의 최초 스폰
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                world.setStorm(false);
                world.setThundering(false);
            }
        } catch (Throwable ignored) {}
    }
}
