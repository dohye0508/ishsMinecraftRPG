package kr.ishs.raibit.ishsMinecraftRpg.listener;

import kr.ishs.raibit.ishsMinecraftRpg.MapConfig;
import kr.ishs.raibit.ishsMinecraftRpg.auth.AuthManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 직사각형 맵 경계.
 * 바닐라 월드보더는 정사각형만 가능해서 다음 두 가지로 직사각형처럼 동작시킨다.
 * <ul>
 *   <li>이동 제한: 이동 이벤트로 직사각형 밖으로 못 나가게 막는다. (텔레포트는 막지 않음)</li>
 *   <li>시각 표시: 플레이어마다 가상 월드보더(Player#setWorldBorder)를 보내서,
 *       가까운 경계 변에 바닐라 월드보더의 파란 벽이 정확히 겹쳐 보이게 한다.</li>
 * </ul>
 *
 * 경계 범위와 사용 여부는 config.yml(border 항목)에서 읽는다. (MapConfig 참고)
 * 인증 중인 플레이어는 인증 대기소(경계 밖)에 있으므로 여기서 제외하고,
 * 인증 대기소 이동 제한은 AuthListener가 처리한다.
 */
public class MapBorderListener implements Listener {

    /** 경계 밖으로 나가려 할 때 안쪽으로 밀어 넣는 여유 거리 */
    private static final double MARGIN = 0.3;

    /**
     * 가상 월드보더의 한 변 길이.
     * 가까운 변만 실제 경계에 맞추고, 나머지 변은 이 크기만큼 멀리 떨어뜨려 보이지 않게 한다.
     */
    private static final double VIRTUAL_BORDER_SIZE = 100000.0;

    /** 갱신 주기 (틱, 10틱 = 0.5초) */
    private static final long VISUAL_PERIOD_TICKS = 10L;

    private final AuthManager authManager;

    /** UUID → 마지막으로 보낸 가상 보더 종류 (같은 값이면 패킷을 다시 보내지 않는다) */
    private final Map<UUID, Integer> lastBorderKey = new ConcurrentHashMap<>();

    public MapBorderListener(AuthManager authManager) {
        this.authManager = authManager;
    }

    /** 플레이어별 가상 월드보더를 계속 맞춰 주는 반복 작업을 시작한다 */
    public void startVisualTask(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateVirtualBorder(player);
            }
        }, VISUAL_PERIOD_TICKS, VISUAL_PERIOD_TICKS);
    }

    /** config.yml을 다시 불러온 뒤 호출: 이미 보낸 가상 보더를 지우고 다음 갱신 때 새 값으로 다시 보낸다 */
    public void refresh() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (lastBorderKey.remove(player.getUniqueId()) != null) {
                player.setWorldBorder(null);
            }
        }
    }

    private void updateVirtualBorder(Player player) {
        UUID uuid = player.getUniqueId();
        World world = player.getWorld();

        // 경계를 끄는 설정이거나, 오버월드가 아니거나, 인증 중이면 가상 보더를 쓰지 않는다
        if (!MapConfig.borderEnabled
                || world.getEnvironment() != World.Environment.NORMAL
                || !authManager.isAuthenticated(player)) {
            if (lastBorderKey.remove(uuid) != null) {
                player.setWorldBorder(null);
            }
            return;
        }

        Location loc = player.getLocation();
        // 가까운 쪽 변을 고른다 (서쪽/동쪽, 북쪽/남쪽)
        boolean nearWest = (loc.getX() - MapConfig.borderMinX) <= (MapConfig.borderMaxX - loc.getX());
        boolean nearNorth = (loc.getZ() - MapConfig.borderMinZ) <= (MapConfig.borderMaxZ - loc.getZ());
        int key = (nearWest ? 1 : 0) | (nearNorth ? 2 : 0);

        Integer last = lastBorderKey.get(uuid);
        if (last != null && last == key) return;

        // 가까운 변이 정확히 실제 경계에 오도록 정사각형을 배치한다
        double squareMinX = nearWest ? MapConfig.borderMinX : MapConfig.borderMaxX - VIRTUAL_BORDER_SIZE;
        double squareMinZ = nearNorth ? MapConfig.borderMinZ : MapConfig.borderMaxZ - VIRTUAL_BORDER_SIZE;

        WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(squareMinX + VIRTUAL_BORDER_SIZE / 2.0, squareMinZ + VIRTUAL_BORDER_SIZE / 2.0);
        border.setSize(VIRTUAL_BORDER_SIZE);
        border.setDamageAmount(0.0);
        border.setDamageBuffer(0.0);
        border.setWarningDistance(0);
        border.setWarningTime(0);

        player.setWorldBorder(border);
        lastBorderKey.put(uuid, key);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastBorderKey.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!MapConfig.borderEnabled) return;

        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null) return;

        World world = to.getWorld();
        if (world == null || world.getEnvironment() != World.Environment.NORMAL) return;

        // 인증 중인 플레이어는 인증 대기소(경계 밖)에 있으므로 제외
        if (!authManager.isAuthenticated(player)) return;

        // 블록 위치 변경 없으면 무시 (머리 회전만)
        Location from = event.getFrom();
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) return;

        double minX = MapConfig.borderMinX, maxX = MapConfig.borderMaxX;
        double minZ = MapConfig.borderMinZ, maxZ = MapConfig.borderMaxZ;
        double x = to.getX();
        double z = to.getZ();
        if (x >= minX && x < maxX && z >= minZ && z < maxZ) return; // 경계 안

        // 경계 밖이면 가장 가까운 경계 안쪽으로 되돌림
        Location clamped = to.clone();
        clamped.setX(Math.min(Math.max(x, minX + MARGIN), maxX - MARGIN));
        clamped.setZ(Math.min(Math.max(z, minZ + MARGIN), maxZ - MARGIN));
        event.setTo(clamped);
    }
}
