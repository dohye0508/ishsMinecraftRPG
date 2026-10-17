package kr.ishs.raibit.ishsMinecraftRpg.config;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 플레이어에게 보이는 모든 문구. messages.yml 에서 읽는다. (기획팀이 코드 없이 수정 가능)
 *
 * <ul>
 *   <li>키는 점(.)으로 구분된 경로입니다. 예) {@code level-up.title}</li>
 *   <li>문구 안의 {@code {이름}} 은 코드가 넘겨주는 값으로 바뀝니다. 예) {@code {level}}</li>
 *   <li>색상은 {@code &} + 글자(예: {@code &c})로 씁니다.</li>
 *   <li>값이 목록(여러 줄)이면 {@link #send} 는 한 줄씩 보내고, {@link #get} 은 줄바꿈으로 이어 붙입니다.</li>
 * </ul>
 */
public final class Messages {

    private Messages() {}

    private static FileConfiguration config;
    private static Logger logger;
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    public static void load(FileConfiguration cfg, Logger log) {
        config = cfg;
        logger = log;
        WARNED.clear();
    }

    /**
     * 문구 하나. 목록이면 줄바꿈으로 이어 붙입니다.
     *
     * @param pairs 자리표시자 이름과 값을 번갈아 (예: "level", 5, "player", "도혜")
     */
    public static String get(String key, Object... pairs) {
        if (config == null || !config.contains(key)) {
            warnMissing(key);
            return key;
        }
        if (config.isList(key)) {
            return String.join("\n", list(key, pairs));
        }
        return apply(config.getString(key, key), pairs);
    }

    /** 여러 줄 문구. 한 줄짜리 값이면 한 줄 목록으로 돌려줍니다. */
    public static List<String> list(String key, Object... pairs) {
        List<String> out = new ArrayList<>();
        if (config == null || !config.contains(key)) {
            warnMissing(key);
            out.add(key);
            return out;
        }
        if (config.isList(key)) {
            for (String line : config.getStringList(key)) out.add(apply(line, pairs));
        } else {
            out.add(apply(config.getString(key, key), pairs));
        }
        return out;
    }

    /** 문구를 대상에게 보냅니다. 목록이면 한 줄씩 보냅니다. */
    public static void send(CommandSender to, String key, Object... pairs) {
        for (String line : list(key, pairs)) to.sendMessage(line);
    }

    /** 문구를 접속한 모든 플레이어에게 방송합니다. */
    @SuppressWarnings("deprecation")
    public static void broadcast(String key, Object... pairs) {
        for (String line : list(key, pairs)) Bukkit.broadcastMessage(line);
    }

    /**
     * 다른 설정 파일에서 읽은 문구 템플릿에 색상과 자리표시자를 적용합니다.
     * 색상 코드를 먼저 변환하므로, 자리표시자 값에 들어 있는 &amp; 는 색상으로 해석되지 않습니다.
     */
    public static String apply(String template, Object... pairs) {
        String text = color(template);
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            Object value = pairs[i + 1];
            text = text.replace("{" + pairs[i] + "}", value == null ? "" : String.valueOf(value));
        }
        return text;
    }

    @SuppressWarnings("deprecation")
    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    private static void warnMissing(String key) {
        if (logger != null && WARNED.add(key)) {
            logger.warning("[messages.yml] '" + key + "' 문구가 없습니다. 키 이름이 그대로 표시됩니다.");
        }
    }
}
