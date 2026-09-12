package kr.ishs.raibit.ishsMinecraftRpg.auth;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 인증 관련 이벤트 처리기
 * <ul>
 *   <li>접속 시 인증 시작</li>
 *   <li>채팅 입력 가로채기 (아이디/비밀번호 입력)</li>
 *   <li>미인증 플레이어 이동 제한</li>
 * </ul>
 */
public class AuthListener implements Listener {

    private final IshsMinecraftRpg plugin;
    private final AuthManager authManager;

    /** 미인증 플레이어가 스폰될 위치 (이동 못 하도록 고정) */
    private static final double LOCK_RADIUS = 3.0;

    public AuthListener(IshsMinecraftRpg plugin, AuthManager authManager) {
        this.plugin = plugin;
        this.authManager = authManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 기존 입장 메시지 제거 (인증 후 AuthManager가 방송)
        event.joinMessage(null);

        // 인증 시작 (약간 딜레이 후 메시지 발송 - 로딩 완료 대기)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            authManager.startAuth(player);
        }, 20L); // 1초 후
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerAuthData data = authManager.getAuthData(player);

        if (data != null) {
            event.quitMessage(
                    net.kyori.adventure.text.Component.text(
                    "§8[§c-§8] §f" + player.getName() +
                    (data.isStudent()
                            ? " §7(" + data.getRealName() + " / " + data.getStudentId() + ")"
                            : " §7(" + data.getRealName() + " 선생님)") +
                    " §f님이 퇴장했습니다.")
            );
        } else {
            event.quitMessage(null); // 미인증 퇴장은 조용히
        }

        authManager.onLeave(player);
    }

    /**
     * 채팅 이벤트 - 인증 중인 플레이어의 입력을 가로챕니다.
     * Paper의 AsyncChatEvent 사용 (deprecated AsyncPlayerChatEvent 대체)
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (authManager.isAuthenticated(player)) return; // 인증된 경우 일반 채팅

        // 인증 중인 경우 채팅 차단하고 AuthManager에 전달
        event.setCancelled(true);

        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        // onChat은 메인 스레드에서 처리 (AuthManager 스레드 안전성 유지)
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            authManager.onChat(player, message);
        });
    }

    /**
     * 미인증 플레이어 이동 제한
     * 스폰 지점 주변 반경 내에만 있을 수 있습니다.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (authManager.isAuthenticated(player)) return;

        Location from = event.getFrom();
        Location to   = event.getTo();
        if (to == null) return;

        // 블록 위치 변경 없으면 허용 (머리 회전만)
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) return;

        // 대기실(-117, -50, 0) 기준으로 제한
        Location authSpawn = new Location(player.getWorld(), -117.5, -50.0, 0.5);
        if (authSpawn.distanceSquared(to) > LOCK_RADIUS * LOCK_RADIUS) {
            event.setTo(from); // 이동 취소
        }
    }

    /**
     * 미인증 플레이어는 데미지를 받지 않도록 무적 처리 (월드보더, 익사, 낙하 등 방지)
     */
    @EventHandler
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!authManager.isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }
}
