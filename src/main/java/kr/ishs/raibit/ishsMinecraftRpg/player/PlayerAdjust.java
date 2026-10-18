package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.config.GameplayConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 관리자가 플레이어의 레벨, 경험치, 스탯 포인트, 스탯을 수동으로 바꾸는 계산. 서버 없이 검증할 수 있다.
 * (명령 처리는 {@link PlayerAdjustCommand})
 */
public final class PlayerAdjust {

    private PlayerAdjust() {}

    /** 바꿀 수 있는 항목 */
    public enum Kind { LEVEL, EXP, POINTS, STAT }

    /** 입력한 항목 이름을 해석한 결과. statId 는 Kind.STAT 일 때만 있다. */
    public record Field(Kind kind, String statId, String label) {}

    /** 변경 결과. error 가 null 이면 성공. error 는 messages.yml 의 admin.* 키 이름 */
    public record Outcome(String error, String label, String oldValue, String newValue, String detail) {
        public boolean ok() { return error == null; }
    }

    /** 레벨/경험치/포인트/스탯 이름(한글 또는 영어)을 항목으로. 모르는 이름이면 null. */
    public static Field resolve(String token) {
        String t = token.trim().toLowerCase(Locale.ROOT);
        switch (t) {
            case "레벨", "level", "lv" -> { return new Field(Kind.LEVEL, null, "레벨"); }
            case "경험치", "exp", "xp" -> { return new Field(Kind.EXP, null, "경험치"); }
            case "스탯포인트", "포인트", "points", "point" -> { return new Field(Kind.POINTS, null, "스탯 포인트"); }
            default -> { /* 아래에서 스탯 이름으로 찾는다 */ }
        }
        for (String id : GameplayConfig.STAT_IDS) {
            GameplayConfig.StatDef def = GameplayConfig.stat(id);
            if (id.equals(t) || (def != null && def.label.equalsIgnoreCase(token.trim()))) {
                return new Field(Kind.STAT, id, def != null ? def.label : id);
            }
        }
        return null;
    }

    /** 명령에서 쓸 수 있는 항목 이름 목록(안내와 자동완성용) */
    public static List<String> fieldNames() {
        List<String> out = new ArrayList<>(List.of("레벨", "경험치", "스탯포인트"));
        for (String id : GameplayConfig.STAT_IDS) {
            GameplayConfig.StatDef def = GameplayConfig.stat(id);
            out.add(def != null ? def.label : id);
        }
        return out;
    }

    /**
     * 항목 값을 바꾼다. 범위를 벗어나면 아무것도 바꾸지 않고 error 를 돌려준다.
     *
     * @param add true 면 현재 값에 더하고(음수면 뺌), false 면 그 값으로 맞춘다
     */
    public static Outcome apply(PlayerData data, Field field, boolean add, double value) {
        switch (field.kind()) {
            case LEVEL -> {
                long next = Math.round(add ? data.getLevel() + value : value);
                if (next < 1 || next > GameplayConfig.maxLevel) {
                    return error("admin.out-of-range", field.label(), "1~" + GameplayConfig.maxLevel);
                }
                String old = String.valueOf(data.getLevel());
                data.setLevel((int) next);
                return ok(field.label(), old, String.valueOf(next));
            }
            case EXP -> {
                double next = add ? data.getExperience() + value : value;
                if (next < 0) return error("admin.out-of-range", field.label(), "0 이상");
                String old = trim(data.getExperience());
                data.setExperience(next);
                return ok(field.label(), old, trim(next));
            }
            case POINTS -> {
                long next = Math.round(add ? data.getStatPoints() + value : value);
                if (next < 0) return error("admin.out-of-range", field.label(), "0 이상");
                String old = String.valueOf(data.getStatPoints());
                data.setStatPoints((int) next);
                return ok(field.label(), old, String.valueOf(next));
            }
            default -> {
                long next = Math.round(add ? data.getStat(field.statId()) + value : value);
                if (next < 0) return error("admin.out-of-range", field.label(), "0 이상");
                String old = String.valueOf(data.getStat(field.statId()));
                data.addStat(field.statId(), (int) (next - data.getStat(field.statId())));
                return ok(field.label(), old, String.valueOf(next));
            }
        }
    }

    /** 새 캐릭터 상태로 되돌린다. (동아리 가입 보너스도 사라진다) */
    public static void reset(PlayerData data) {
        data.setLevel(1);
        data.setExperience(0);
        data.setStatPoints(GameplayConfig.startingPoints);
        for (String id : GameplayConfig.STAT_IDS) {
            data.addStat(id, GameplayConfig.stat(id).starting - data.getStat(id));
        }
        data.setClub(null);
    }

    private static Outcome ok(String label, String oldValue, String newValue) {
        return new Outcome(null, label, oldValue, newValue, null);
    }

    private static Outcome error(String key, String label, String detail) {
        return new Outcome(key, label, null, null, detail);
    }

    private static String trim(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.ROOT, "%.1f", v);
    }
}
