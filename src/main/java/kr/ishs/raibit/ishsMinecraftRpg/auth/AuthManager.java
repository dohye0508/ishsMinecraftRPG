package kr.ishs.raibit.ishsMinecraftRpg.auth;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import kr.ishs.raibit.ishsMinecraftRpg.MapConfig;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 플레이어 인증 상태 및 데이터를 전역적으로 관리합니다.
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

    private static final int MAX_FAIL = 5;

    // 인증 사용 여부, 인증 대기소, 최초 스폰 좌표는 config.yml에서 읽는다. (MapConfig 참고)

    public AuthManager(IshsMinecraftRpg plugin) {
        this.plugin = plugin;
        this.riroAuth = new RiroSchoolAuth(plugin.getLogger());
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

        // 로그인 전 대기소로 이동 및 모험 모드 변경
        player.teleport(MapConfig.authLocation(player.getWorld()));
        player.setGameMode(org.bukkit.GameMode.ADVENTURE);

        sendAuthPrompt(player);
    }

    /** 인증 진입 안내 메시지 */
    private void sendAuthPrompt(Player player) {
        player.sendMessage("§8§l--------------------------------------------------");
        player.sendMessage("§b§l인천과학고 마인크래프트 서버에 오신 것을 환영합니다!");
        player.sendMessage("");
        player.sendMessage("§f이 서버는 §e리로스쿨 계정§f으로만 접속할 수 있습니다.");
        player.sendMessage("§f인증을 완료해야 게임을 즐길 수 있습니다.");
        player.sendMessage("");
        player.sendMessage("§a▶ 리로스쿨 §f아이디§a를 채팅으로 입력해 주세요:");
        player.sendMessage("§8§l--------------------------------------------------");
    }

    /** 플레이어 채팅 입력 처리 */
    public boolean onChat(Player player, String message) {
        UUID uuid = player.getUniqueId();
        AuthStep step = authSteps.get(uuid);

        if (step == null || step == AuthStep.AUTHENTICATED) return false; // 인증 완료된 경우 일반 채팅 허용

        // 인증 실패 횟수 초과
        if (failCount.getOrDefault(uuid, 0) >= MAX_FAIL) {
            player.sendMessage("§c인증 시도 횟수를 초과했습니다. 서버에서 퇴장됩니다.");
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> player.kick(net.kyori.adventure.text.Component.text("§c리로스쿨 인증 실패 (시도 횟수 초과)")));
            return true;
        }

        switch (step) {
            case WAITING_ID -> {
                pendingUserId.put(uuid, message.trim());
                authSteps.put(uuid, AuthStep.WAITING_PASSWORD);
                player.sendMessage("§a▶ 비밀번호를 입력해 주세요: §7(채팅창은 다른 플레이어에게 보이지 않습니다)");
            }
            case WAITING_PASSWORD -> {
                String userId   = pendingUserId.get(uuid);
                String password = message.trim();
                pendingUserId.remove(uuid);

                player.sendMessage("§e리로스쿨 서버에 접속 중... 잠시 기다려 주세요.");

                // 비동기로 HTTP 요청 (메인 스레드 블로킹 방지)
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    PlayerAuthData data;
                    if ("1".equals(userId) && "1".equals(password)) {
                        // 계정이 없는 사람(테스트용)을 위한 1/1 하이패스 계정
                        data = new PlayerAuthData(player.getName(), "게스트(테스트)", "0000", "0", true);
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
                player.sendMessage("§a▶ 비밀번호를 입력해 주세요:");
            }
            default -> {} // AUTHENTICATED는 위에서 이미 처리됨
        }

        return true; // 인증 과정 중 채팅은 차단
    }

    private void onAuthSuccess(Player player, PlayerAuthData data) {
        player.sendMessage("§8§l--------------------------------------------------");
        player.sendMessage("§a§l✔ 인증 성공!");
        player.sendMessage("");
        if (data.isStudent()) {
            player.sendMessage("§f이름: §e" + data.getRealName());
            player.sendMessage("§f학번: §e" + data.getStudentId() + "  §f기수: §e" + data.getGeneration() + "기");
        } else {
            player.sendMessage("§f이름: §e" + data.getRealName() + " §7(교사)");
        }
        player.sendMessage("");
        player.sendMessage("§b즐거운 모험 되세요! ⚔");
        player.sendMessage("§8§l--------------------------------------------------");

        // 모든 플레이어에게 입장 메시지
        String joinMsg = "§8[§a+§8] §f" + player.getName() +
                (data.isStudent() ? " §7(" + data.getRealName() + " / " + data.getStudentId() + ")" : " §7(" + data.getRealName() + " 선생님)") +
                " §f님이 접속했습니다.";
        plugin.getServer().broadcast(net.kyori.adventure.text.Component.text(joinMsg));

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

        // 로그인 성공 시 서바이벌 모드로 변경
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);

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
        int remaining = MAX_FAIL - failCount;
        player.sendMessage("§8§l--------------------------------------------------");
        player.sendMessage("§c§l✘ 인증 실패!");
        player.sendMessage("§f아이디 또는 비밀번호가 올바르지 않습니다.");
        if (remaining > 0) {
            player.sendMessage("§e남은 시도 횟수: §f" + remaining + "번");
            player.sendMessage("");
            player.sendMessage("§a▶ 다시 리로스쿨 §f아이디§a를 입력해 주세요:");
        } else {
            player.sendMessage("§c더 이상 시도할 수 없습니다. 서버에서 퇴장됩니다.");
            plugin.getServer().getScheduler().runTaskLater(plugin,
                    () -> player.kick(net.kyori.adventure.text.Component.text("§c리로스쿨 인증 실패")), 60L);
        }
        player.sendMessage("§8§l--------------------------------------------------");
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
