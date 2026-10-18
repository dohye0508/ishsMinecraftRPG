package kr.ishs.raibit.ishsMinecraftRpg;

import kr.ishs.raibit.ishsMinecraftRpg.auth.AuthCommand;
import kr.ishs.raibit.ishsMinecraftRpg.auth.AuthListener;
import kr.ishs.raibit.ishsMinecraftRpg.auth.AuthManager;
import kr.ishs.raibit.ishsMinecraftRpg.config.ClubConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.GameplayConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import kr.ishs.raibit.ishsMinecraftRpg.listener.MapBorderListener;
import kr.ishs.raibit.ishsMinecraftRpg.listener.StatListener;
import kr.ishs.raibit.ishsMinecraftRpg.npc.NpcCommand;
import kr.ishs.raibit.ishsMinecraftRpg.npc.NpcConfig;
import kr.ishs.raibit.ishsMinecraftRpg.npc.NpcManager;
import kr.ishs.raibit.ishsMinecraftRpg.npc.NpcStartupListener;
import kr.ishs.raibit.ishsMinecraftRpg.ranking.RankingBoard;
import kr.ishs.raibit.ishsMinecraftRpg.ranking.RankingCommand;
import kr.ishs.raibit.ishsMinecraftRpg.ranking.RankingConfig;
import kr.ishs.raibit.ishsMinecraftRpg.player.*;
import kr.ishs.raibit.ishsMinecraftRpg.item.CustomItems;
import kr.ishs.raibit.ishsMinecraftRpg.item.CustomItemListener;
import kr.ishs.raibit.ishsMinecraftRpg.item.CustomItemCommand;
import kr.ishs.raibit.ishsMinecraftRpg.stat.StatCommand;
import kr.ishs.raibit.ishsMinecraftRpg.stat.StatManager;
import kr.ishs.raibit.ishsMinecraftRpg.task.RpgUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class IshsMinecraftRpg extends JavaPlugin {

    private AuthManager authManager;
    private PlayerManager playerManager;
    private StatManager statManager;
    private SkillManager skillManager;
    private RpgListener rpgListener;
    private NpcManager npcManager;
    private RankingBoard rankingBoard;

    @Override
    public void onEnable() {
        // 데이터 폴더 생성
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // config.yml 이 없으면 기본 파일 생성
        saveDefaultConfig();

        // Manager 초기화
        playerManager = PlayerManager.getInstance();
        statManager = new StatManager(this, playerManager);
        authManager = new AuthManager(this);
        skillManager = new SkillManager(this);
        rankingBoard = new RankingBoard(this);

        // 커스텀 아이템 태그 초기화
        CustomItems.init(this);

        // 모든 설정 파일 로드 (없는 파일은 기본 파일을 만들어 둔다)
        reloadAll();

        // 동아리 점수는 서버를 껐다 켜도 남는다
        ClubScoreManager.getInstance().load(new File(getDataFolder(), "club-scores.yml"));

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
        getCommand("엔피씨").setExecutor(new NpcCommand(this));
        getCommand("랭킹판").setExecutor(new RankingCommand(this));
        getCommand("동아리점수").setExecutor(new ClubScoreCommand());
        getCommand("스탯조정").setExecutor(new PlayerAdjustCommand(this));
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

        // NPC 는 Citizens 가 저장된 NPC 를 모두 불러온 뒤(서버 시작이 끝난 뒤)에 npcs.yml 대로 맞춘다
        npcManager = new NpcManager(this);
        if (getServer().getPluginManager().getPlugin("Citizens") != null) {
            getServer().getPluginManager().registerEvents(new NpcStartupListener(this), this);
        }

        getLogger().info("ISHSMinecraftRpg 활성화 완료 (동아리 " + ClubConfig.all().size()
                + "개, 아이템 " + CustomItems.getIds().size() + "종, 도전과제 " + AchievementConfig.all().size()
                + "개, 스킬 " + skillManager.skillCount() + "개 로드)");
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public StatManager getStatManager() {
        return statManager;
    }

    /**
     * 플러그인 폴더의 YAML 파일을 읽습니다. 파일이 없으면 jar 안의 기본 파일을 먼저 복사합니다.
     * 파일에서 빠진 항목은 jar 안의 기본 파일 값으로 채워지므로, 코드에 기본 숫자를 따로 두지 않아도 됩니다.
     */
    public FileConfiguration loadYaml(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            saveResource(fileName, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        try (InputStream in = getResource(fileName)) {
            if (in != null) {
                config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8)));
            }
        } catch (IOException e) {
            getLogger().warning("[" + fileName + "] 기본값을 읽지 못했습니다: " + e.getMessage());
        }
        return config;
    }

    /** config.yml 과 모든 설정 파일(messages, gameplay, clubs, items, achievements, help, skills)을 다시 읽습니다. */
    public void reloadAll() {
        reloadConfig();
        MapConfig.load(getConfig());
        QuestCommand.loadQuestNames(getConfig());
        reloadContent();
    }

    /** npcs.yml 을 다시 읽어 게임 안 NPC 를 그 내용대로 만들고, 옮기고, 지웁니다. @return 반영된 NPC 수 (Citizens 가 없으면 -1) */
    public int reloadNpcs() {
        NpcConfig.load(loadYaml("npcs.yml"), getLogger());
        return npcManager.sync();
    }

    /** config.yml 을 제외한 설정 파일들을 다시 읽어 반영합니다. */
    public void reloadContent() {
        Messages.load(loadYaml("messages.yml"), getLogger());
        GameplayConfig.load(loadYaml("gameplay.yml"), getLogger());
        ClubConfig.load(loadYaml("clubs.yml"), getLogger());
        CustomItems.load(loadYaml("items.yml"));
        AchievementConfig.load(loadYaml("achievements.yml"), getLogger());
        HelpCommand.load(loadYaml("help.yml"));
        skillManager.load(loadYaml("skills.yml"), getLogger());
        reloadRanking();
    }

    /** ranking.yml 을 다시 읽어 월드 랭킹판을 새 설정으로 다시 띄웁니다. */
    public void reloadRanking() {
        RankingConfig.load(loadYaml("ranking.yml"), getLogger());
        rankingBoard.refresh();
    }

    @Override
    public void onDisable() {
        rankingBoard.remove();
        playerManager.saveAll();
    }

    /** 월드 설정 적용: 게임 규칙과 날씨는 gameplay.yml 의 world, 월드 스폰은 config.yml 의 spawn.first */
    public void applyWorldSettings(World world) {
        if (world == null) return;

        boolean overworld = world.getEnvironment() == World.Environment.NORMAL;
        GameplayConfig.applyWorldRules(world, overworld, getLogger());

        // 바닐라 월드보더(정사각형)는 쓰지 않는다. 직사각형 경계는 MapBorderListener가 처리한다.
        // 주의: 맵 위치(좌표)가 나중에 바뀔 수 있으니 config.yml 의 border, spawn, auth 좌표를 다시 확인할 것
        try {
            if (overworld) {
                world.setSpawnLocation(MapConfig.firstSpawnLocation(world));
            }
        } catch (Throwable ignored) {}
    }
}
