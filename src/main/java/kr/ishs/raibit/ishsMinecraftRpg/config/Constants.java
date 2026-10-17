package kr.ishs.raibit.ishsMinecraftRpg.config;

import java.util.Locale;

/**
 * 설정 파일에 적힌 이름(대문자)으로 Sound / Particle / PotionEffectType 같은 마인크래프트 상수를 찾는다.
 * 서버 버전에 따라 enum 이거나 인터페이스 상수여도 같은 방식으로 동작한다.
 */
public final class Constants {

    private Constants() {}

    /**
     * 공기 계열 블록인지 확인한다. Material#isAir() 는 서버가 있어야 동작하므로(테스트에서 쓸 수 없음),
     * 서버 없이도 되는 방식으로 직접 확인한다.
     */
    public static boolean isAir(org.bukkit.Material material) {
        return material == org.bukkit.Material.AIR
                || material == org.bukkit.Material.CAVE_AIR
                || material == org.bukkit.Material.VOID_AIR;
    }

    /** 이름으로 상수를 찾는다. 없거나 잘못된 이름이면 null. */
    public static <T> T find(Class<T> type, String name) {
        if (name == null || name.isBlank()) return null;
        try {
            Object value = type.getField(name.trim().toUpperCase(Locale.ROOT)).get(null);
            return type.isInstance(value) ? type.cast(value) : null;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            return null;
        }
    }
}
