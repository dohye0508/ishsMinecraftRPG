package kr.ishs.raibit.ishsMinecraftRpg.auth;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import kr.ishs.raibit.ishsMinecraftRpg.MapConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 플레이어 인증 상태 및 데이터를 전역적으로 관리합니다.
 * 인증 사용 여부, 대기소 위치, 실패 허용 횟수, 게임모드, 테스트 계정은 config.yml(MapConfig), 문구는 messages.yml 에서 온다.
 */
public class AuthManager {

    /** 현재 인증 진행 중인 플레이어들의 단계 */
    public enum AuthStep {
        WAITING_ID,       // 학번(아이디) 입력 대기
        WAITING_PASSWORD, // 비밀번호 입력 대기
        AUTHENTICATED,    // 인증 완료
        FAILED            // 인증 실패 (재시도 가능)
    }

    private final IshsMinecraftRpg plugin;
    private final RiroSchoolAuth riroAuth;

    /** UUID → 인증 완료된 데이터 */
    private final Map<UUID, PlayerAuthData> authenticatedPlayers = new ConcurrentHashMap<>();

    /** UUID → 현재 인증 단계 */
    private final Map<UUID, AuthStep> authSteps = new ConcurrentHashMap<>();

    /** UUID → 입력한 아이디 (비밀번호 입력 대기 단계에서 임시 보관) */
    private final Map<UUID, String> pendingUserId = new ConcurrentHashMap<>();

    /** UUID → 인증 실패 횟수 */
    private final Map<UUID, Integer> failCount = new ConcurrentHashMap<>();

    /** UUID → 인증 전 기존 위치 (로그아웃 위치 보존용) */
    private final Map<UUID, org.bukkit.Location> preAuthLocations = new ConcurrentHashMap<>();

    public AuthManager(IshsMinecraftRpg plugin) {
        this.plugin = plugin;
        this.riroAuth = new RiroSchoolAuth(plugin.getLogger());
    }

    /** § 색상 코드가 들어 있는 문구를 채팅/퇴장 화면용 Component 로 바꿉니다. */
    public static Component legacy(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(text);
    }

    /** 플레이어가 서버에 접속했을 때 초기화 */
    public void startAuth(Player player) {
        UUID uuid = player.getUniqueId();

        // 인증 비활성화 시 대기소 이동 없이 바로 인증 완료 처리
        if (!MapConfig.authEnabled) {
            authSteps.put(uuid, AuthStep.AUTHENTICATED);
            return;
        }

        authSteps.put(uuid, AuthStep.WAITING_ID);
        pendingUserId.remove(uuid);
        failCount.put(uuid, 0);

        // 현재 위치 저장 (인증 대기소에 있는 경우 무시)
        org.bukkit.Location currentLoc = player.getLocation();
        boolean isAuthSpawn = Math.abs(currentLoc.getX() - MapConfig.authX) < MapConfig.authLockHalf
                && Math.abs(currentLoc.getZ() - MapConfig.authZ) < MapConfig.authLockHalf;
        if (!isAuthSpawn) {
            preAuthLocations.put(uuid, currentLoc);
        }

        // 로그인 전 대기소로 이동 및 게임모드 변경
        player.teleport(MapConfig.authLocation(player.getWorld()));
        player.setGameMode(MapConfig.authGameModeWaiting);

        Messages.send(player, "auth.prompt");
    }

    /** 플레이어 채팅 입력 처리 */
    public boolean onChat(Player player, String message) {
        UUID uuid = player.getUniqueId();
        AuthStep step = authSteps.get(uuid);

        if (step == null || step == AuthStep.AUTHENTICATED) return false; // 인증 완료된 경우 일반 채팅 허용

        // 인증 실패 횟수 초과
        if (failCount.getOrDefault(uuid, 0) >= MapConfig.authMaxFail) {
            Messages.send(player, "auth.too-many-fails");
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> player.kick(legacy(Messages.get("auth.kick-too-many"))));
            return true;
        }

        switch (step) {
            case WAITING_ID -> {
                pendingUserId.put(uuid, message.trim());
                authSteps.put(uuid, AuthStep.WAITING_PASSWORD);
                Messages.send(player, "auth.ask-password");
            }
            case WAITING_PASSWORD -> {
                String userId   = pendingUserId.get(uuid);
                String password = message.trim();
                pendingUserId.remove(uuid);

                Messages.send(player, "auth.connecting");

                // 비동기로 HTTP 요청 (메인 스레드 블로킹 방지)
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    PlayerAuthData data;
                    if (MapConfig.testAccountEnabled
                            && MapConfig.testAccountId.equals(userId)
                            && MapConfig.testAccountPassword.equals(password)) {
                        // 계정이 없는 사람(테스트용)을 위한 임시 계정. config.yml 의 auth.test-account 로 켜고 끈다.
                        plugin.getLogger().warning("[Auth] 테스트 계정으로 인증되었습니다: " + player.getName());
                        data = new PlayerAuthData(player.getName(), MapConfig.testAccountName,
                                MapConfig.testAccountStudentId, MapConfig.testAccountGeneration, true);
                    } else {
                        data = riroAuth.login(player.getName(), userId, password);
                    }

                    // 결과 처리는 메인 스레드에서
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        if (data != null) {
                            authenticatedPlayers.put(uuid, data);
                            authSteps.put(uuid, AuthStep.AUTHENTICATED);
                            failCount.remove(uuid);
                            onAuthSuccess(player, data);
                        } else {
                            int fails = failCount.merge(uuid, 1, Integer::sum);
                            authSteps.put(uuid, AuthStep.WAITING_ID);
                            onAuthFailed(player, fails);
                        }
                    });
                });
            }
            case FAILED -> {
                // 재시도: 다시 WAITING_ID로
                authSteps.put(uuid, AuthStep.WAITING_ID);
                pendingUserId.put(uuid, message.trim());
                authSteps.put(uuid, AuthStep.WAITING_PASSWORD);
                Messages.send(player, "auth.ask-password-retry");
            }
            default -> {} // AUTHENTICATED는 위에서 이미 처리됨
        }

        return true; // 인증 과정 중 채팅은 차단
    }

    private void onAuthSuccess(Player player, PlayerAuthData data) {
        Messages.send(player, "auth.success-header");
        Messages.send(player, data.isStudent() ? "auth.success-student" : "auth.success-teacher",
                "name", data.getRealName(), "id", data.getStudentId(), "generation", data.getGeneration());
        Messages.send(player, "auth.success-footer");

        // 모든 플레이어에게 입장 메시지
        String joinMsg = Messages.get(data.isStudent() ? "auth.join-student" : "auth.join-teacher",
                "player", player.getName(), "name", data.getRealName(), "id", data.getStudentId());
        plugin.getServer().broadcast(legacy(joinMsg));

        // 위치 복구 또는 최초 인증 스폰 이동
        org.bukkit.Location orig = preAuthLocations.remove(player.getUniqueId());
        boolean firstTime = !player.hasPlayedBefore();

        if (firstTime || orig == null) {
            // 최초 접속이거나, 이전 데이터 없이 대기실에서 로그아웃 한 경우 config.yml의 최초 스폰으로
            player.teleport(MapConfig.firstSpawnLocation(player.getWorld()));
        } else {
            // 인증 2회차 이상 (마지막 플레이 위치 복구)
            player.teleport(orig);
        }

        // 로그인 성공 시 게임모드 변경 (config.yml 의 auth.gamemode.after)
        player.setGameMode(MapConfig.authGameModeAfter);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData rpgData = kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager.getInstance().getPlayerData(player);
                if (rpgData != null && !rpgData.hasClub()) {
                    kr.ishs.raibit.ishsMinecraftRpg.player.ClubGUI.openClubGUI(player);
                }
            }
        }, 20L); // 텔레포트 이후 인벤토리 여는 것이 안정적 (1초)
    }

    private void onAuthFailed(Player player, int failCount) {
        int remaining = MapConfig.authMaxFail - failCount;
        Messages.send(player, "auth.failed-header");
        if (remaining > 0) {
            Messages.send(player, "auth.failed-retry", "remaining", remaining);
        } else {
            Messages.send(player, "auth.failed-final");
            plugin.getServer().getScheduler().runTaskLater(plugin,
                    () -> player.kick(legacy(Messages.get("auth.kick-failed"))), 60L);
        }
        Messages.send(player, "auth.failed-footer");
    }

    /** 플레이어가 서버를 떠날 때 정리 */
    public void onLeave(Player player) {
        UUID uuid = player.getUniqueId();
        authSteps.remove(uuid);
        pendingUserId.remove(uuid);
        failCount.remove(uuid);
        preAuthLocations.remove(uuid);
        // 인증 데이터는 유지 (재접속 시 재인증 필요하므로 제거)
        authenticatedPlayers.remove(uuid);
    }

    /** 플레이어가 인증 완료되었는지 확인 */
    public boolean isAuthenticated(Player player) {
        return authSteps.get(player.getUniqueId()) == AuthStep.AUTHENTICATED;
    }

    /** 인증된 플레이어의 데이터 반환 */
    public PlayerAuthData getAuthData(Player player) {
        return authenticatedPlayers.get(player.getUniqueId());
    }

    /** 현재 인증 단계 반환 */
    public AuthStep getStep(Player player) {
        return authSteps.getOrDefault(player.getUniqueId(), AuthStep.WAITING_ID);
    }
}
