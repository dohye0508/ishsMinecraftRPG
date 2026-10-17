package kr.ishs.raibit.ishsMinecraftRpg.item;

import kr.ishs.raibit.ishsMinecraftRpg.config.Constants;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 커스텀 아이템 효과 한 건. items.yml 의 effects 항목에서 읽는다.
 * 효과 발동(실행)은 CustomItemListener 가 담당하고, 여기서는 YAML 해석과 값 검증만 한다.
 */
public final class ItemEffect {

    /** 효과가 발동하는 시점 */
    public enum Trigger {
        /** 아이템을 들고 우클릭 */
        RIGHT_CLICK,
        /** 아이템을 들고 대상을 공격해 적중 */
        HIT
    }

    /** 효과 동작 종류 */
    public enum ActionType {
        /** 사용자 체력 회복 */
        HEAL,
        /** 포션 효과 부여 (사용자 또는 맞은 대상) */
        POTION,
        /** 맞은 대상을 사용자 반대 방향으로 밀쳐냄 (HIT 전용) */
        KNOCKBACK,
        /** 맞은 대상을 위로 띄움 (HIT 전용) */
        LAUNCH
    }

    /** 효과 동작 한 건 */
    public static final class Action {
        public final ActionType type;
        public final double amount;          // HEAL: 회복량 (체력 1 = 반 칸)
        public final boolean onTarget;       // POTION: true면 맞은 대상, false면 사용자
        public final PotionEffectType potion; // POTION
        public final int ticks;              // POTION: 지속시간 (틱)
        public final int amplifier;          // POTION: 단계 - 1
        public final double strength;        // KNOCKBACK: 세기
        public final double lift;            // KNOCKBACK: 위로 뜨는 정도
        public final double height;          // LAUNCH: 위로 솟는 속도

        private Action(ActionType type, double amount, boolean onTarget, PotionEffectType potion,
                       int ticks, int amplifier, double strength, double lift, double height) {
            this.type = type;
            this.amount = amount;
            this.onTarget = onTarget;
            this.potion = potion;
            this.ticks = ticks;
            this.amplifier = amplifier;
            this.strength = strength;
            this.lift = lift;
            this.height = height;
        }
    }

    public final Trigger trigger;
    public final long cooldownMs;          // 0이면 쿨타임 없음
    public final boolean consume;          // RIGHT_CLICK: 발동 시 아이템 1개 소모
    public final String message;           // 사용자에게 보낼 메시지 (없으면 null)
    public final String targetMessage;     // 맞은 대상이 플레이어일 때 보낼 메시지 (없으면 null)
    public final Sound sound;              // 없으면 null
    public final float soundVolume;
    public final float soundPitch;
    public final Particle particle;        // 없으면 null
    public final int particleCount;
    public final double particleSpread;
    public final double particleSpeed;
    public final List<Action> actions;

    private ItemEffect(Trigger trigger, long cooldownMs, boolean consume, String message, String targetMessage,
                       Sound sound, float soundVolume, float soundPitch,
                       Particle particle, int particleCount, double particleSpread, double particleSpeed,
                       List<Action> actions) {
        this.trigger = trigger;
        this.cooldownMs = cooldownMs;
        this.consume = consume;
        this.message = message;
        this.targetMessage = targetMessage;
        this.sound = sound;
        this.soundVolume = soundVolume;
        this.soundPitch = soundPitch;
        this.particle = particle;
        this.particleCount = particleCount;
        this.particleSpread = particleSpread;
        this.particleSpeed = particleSpeed;
        this.actions = Collections.unmodifiableList(actions);
    }

    /**
     * effects 목록의 항목 하나를 해석합니다. 잘못된 값은 콘솔에 경고를 남기고,
     * 효과 전체를 쓸 수 없을 때(trigger 오류)만 null 을 반환합니다.
     */
    public static ItemEffect parse(String itemId, Map<?, ?> raw, Logger log) {
        ConfigurationSection s = new MemoryConfiguration().createSection("effect", raw);
        String where = "[items.yml] " + itemId + " 효과: ";

        Trigger trigger;
        try {
            trigger = Trigger.valueOf(s.getString("trigger", "").trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warning(where + "trigger 가 올바르지 않아 건너뜁니다. (RIGHT_CLICK 또는 HIT)");
            return null;
        }

        long cooldownMs = Math.max(0L, Math.round(s.getDouble("cooldown", 0.0) * 1000.0));
        boolean consume = s.getBoolean("consume", false);
        if (consume && trigger != Trigger.RIGHT_CLICK) {
            log.warning(where + "consume 은 RIGHT_CLICK 에서만 사용할 수 있어 무시합니다.");
            consume = false;
        }

        String message = s.contains("message") ? color(s.getString("message")) : null;
        String targetMessage = s.contains("target-message") ? color(s.getString("target-message")) : null;
        if (targetMessage != null && trigger != Trigger.HIT) {
            log.warning(where + "target-message 는 HIT 에서만 사용할 수 있어 무시합니다.");
            targetMessage = null;
        }

        Sound sound = null;
        if (s.contains("sound")) {
            sound = constant(Sound.class, s.getString("sound"));
            if (sound == null) log.warning(where + "sound '" + s.getString("sound") + "' 을(를) 찾을 수 없어 무시합니다.");
        }
        float soundVolume = (float) s.getDouble("sound-volume", 1.0);
        float soundPitch = (float) s.getDouble("sound-pitch", 1.0);

        Particle particle = null;
        if (s.contains("particle")) {
            particle = constant(Particle.class, s.getString("particle"));
            if (particle == null) {
                log.warning(where + "particle '" + s.getString("particle") + "' 을(를) 찾을 수 없어 무시합니다.");
            } else if (particle.getDataType() != Void.class) {
                log.warning(where + "particle '" + particle.name() + "' 은(는) 추가 데이터가 필요한 종류라 지원하지 않아 무시합니다.");
                particle = null;
            }
        }
        int particleCount = Math.max(1, s.getInt("particle-count", 10));
        double particleSpread = Math.max(0.0, s.getDouble("particle-spread", 0.4));
        double particleSpeed = Math.max(0.0, s.getDouble("particle-speed", 0.0));

        List<Action> actions = new ArrayList<>();
        for (Map<?, ?> rawAction : s.getMapList("actions")) {
            Action action = parseAction(where, trigger, new MemoryConfiguration().createSection("action", rawAction), log);
            if (action != null) actions.add(action);
        }

        if (actions.isEmpty() && message == null && sound == null && particle == null) {
            log.warning(where + "동작(actions), 메시지, 소리, 파티클이 모두 비어 있어 건너뜁니다.");
            return null;
        }

        return new ItemEffect(trigger, cooldownMs, consume, message, targetMessage,
                sound, soundVolume, soundPitch, particle, particleCount, particleSpread, particleSpeed, actions);
    }

    private static Action parseAction(String where, Trigger trigger, ConfigurationSection a, Logger log) {
        ActionType type;
        try {
            type = ActionType.valueOf(a.getString("type", "").trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warning(where + "action type '" + a.getString("type") + "' 이(가) 올바르지 않아 건너뜁니다. (HEAL, POTION, KNOCKBACK, LAUNCH)");
            return null;
        }

        switch (type) {
            case HEAL: {
                double amount = a.getDouble("amount", 0.0);
                if (amount <= 0.0) {
                    log.warning(where + "HEAL 의 amount 는 0보다 커야 합니다. 건너뜁니다.");
                    return null;
                }
                return new Action(type, amount, false, null, 0, 0, 0, 0, 0);
            }
            case POTION: {
                PotionEffectType potion = constant(PotionEffectType.class, a.getString("potion"));
                if (potion == null) {
                    log.warning(where + "POTION 의 potion '" + a.getString("potion") + "' 을(를) 찾을 수 없어 건너뜁니다.");
                    return null;
                }
                double seconds = a.getDouble("seconds", 0.0);
                if (seconds <= 0.0) {
                    log.warning(where + "POTION 의 seconds 는 0보다 커야 합니다. 건너뜁니다.");
                    return null;
                }
                boolean onTarget = "TARGET".equalsIgnoreCase(a.getString("target", "SELF"));
                if (onTarget && trigger != Trigger.HIT) {
                    log.warning(where + "target: TARGET 은 HIT 에서만 사용할 수 있어 건너뜁니다.");
                    return null;
                }
                int level = Math.max(1, a.getInt("level", 1));
                return new Action(type, 0, onTarget, potion, (int) Math.round(seconds * 20.0), level - 1, 0, 0, 0);
            }
            case KNOCKBACK: {
                if (trigger != Trigger.HIT) {
                    log.warning(where + "KNOCKBACK 은 HIT 에서만 사용할 수 있어 건너뜁니다.");
                    return null;
                }
                return new Action(type, 0, true, null, 0, 0, a.getDouble("strength", 1.0), a.getDouble("lift", 0.3), 0);
            }
            case LAUNCH: {
                if (trigger != Trigger.HIT) {
                    log.warning(where + "LAUNCH 는 HIT 에서만 사용할 수 있어 건너뜁니다.");
                    return null;
                }
                return new Action(type, 0, true, null, 0, 0, 0, 0, a.getDouble("height", 1.0));
            }
            default:
                return null;
        }
    }

    private static <T> T constant(Class<T> type, String name) {
        return Constants.find(type, name);
    }

    private static String color(String text) {
        return Messages.color(text);
    }
}
