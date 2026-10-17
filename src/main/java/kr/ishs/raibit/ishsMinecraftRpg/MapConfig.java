package kr.ishs.raibit.ishsMinecraftRpg;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * 맵 관련 설정(인증 장소, 최초 스폰, 맵 경계)을 config.yml에서 읽어 보관합니다.
 * 기획팀이 코드를 건드리지 않고 config.yml의 값만 고쳐서 바꿀 수 있도록 하기 위한 클래스입니다.
 *
 * config.yml에는 블록 좌표(F3 화면에 보이는 정수 좌표)를 적고,
 * 여기서 블록 중앙(+0.5)으로 변환해 사용합니다.
 */
public final class MapConfig {

    private MapConfig() {}

    // ---- 리로스쿨 인증 ----
    public static boolean authEnabled = true;
    public static double authX = 231.5;
    public static double authY = -61.0;
    public static double authZ = 363.5;
    /** 인증 중 이동 가능 범위: 대기소 중심에서 상하좌우로 이만큼 (한 변 길이의 절반) */
    public static double authLockHalf = 7.5;

    // ---- 최초 스폰 ----
    public static double spawnX = 305.5;
    public static double spawnY = -61.0;
    public static double spawnZ = 156.5;

    // ---- 맵 경계 (직사각형) ----
    public static boolean borderEnabled = true;
    public static double borderMinX = 20.0;
    public static double borderMaxX = 551.0; // 큰 쪽 블록의 끝(+1)
    public static double borderMinZ = 15.0;
    public static double borderMaxZ = 221.0; // 큰 쪽 블록의 끝(+1)

    /** config.yml 값을 읽어 반영합니다. 값이 없으면 jar 안의 기본 config.yml 값이 쓰입니다. */
    public static void load(FileConfiguration c) {
        authEnabled = c.getBoolean("auth.enabled", true);
        authX = c.getInt("auth.room.x", 231) + 0.5;
        authY = c.getDouble("auth.room.y", -61.0);
        authZ = c.getInt("auth.room.z", 363) + 0.5;
        authLockHalf = Math.max(1.0, c.getDouble("auth.lock-size", 15.0) / 2.0);

        spawnX = c.getInt("spawn.first.x", 305) + 0.5;
        spawnY = c.getDouble("spawn.first.y", -61.0);
        spawnZ = c.getInt("spawn.first.z", 156) + 0.5;

        borderEnabled = c.getBoolean("border.enabled", true);
        int x1 = c.getInt("border.corner1.x", 20);
        int z1 = c.getInt("border.corner1.z", 220);
        int x2 = c.getInt("border.corner2.x", 550);
        int z2 = c.getInt("border.corner2.z", 15);
        // 꼭짓점 순서와 상관없이 작은 값/큰 값으로 정리 (큰 쪽은 블록의 끝까지 포함)
        borderMinX = Math.min(x1, x2);
        borderMaxX = Math.max(x1, x2) + 1.0;
        borderMinZ = Math.min(z1, z2);
        borderMaxZ = Math.max(z1, z2) + 1.0;
    }

    /** 리로스쿨 인증 대기소 위치 */
    public static Location authLocation(World world) {
        return new Location(world, authX, authY, authZ);
    }

    /** 최초 스폰 위치 */
    public static Location firstSpawnLocation(World world) {
        return new Location(world, spawnX, spawnY, spawnZ);
    }
}
