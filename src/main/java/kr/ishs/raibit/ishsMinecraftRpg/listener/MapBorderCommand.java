package kr.ishs.raibit.ishsMinecraftRpg.listener;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import kr.ishs.raibit.ishsMinecraftRpg.MapConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * /맵경계 켜기|끄기|상태 - 맵 경계(직사각형 월드보더)를 바로 켜거나 끈다. (관리자 전용)
 * 건축 등으로 경계가 방해될 때 쓴다. 바뀐 값은 config.yml 의 border.enabled 에도 적어 두어
 * /설정리로드 와 서버 재시작 뒤에도 유지된다.
 */
public class MapBorderCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of("켜기", "끄기", "상태");

    private final IshsMinecraftRpg plugin;
    private final MapBorderListener listener;

    public MapBorderCommand(IshsMinecraftRpg plugin, MapBorderListener listener) {
        this.plugin = plugin;
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            Messages.send(sender, "common.no-permission");
            return true;
        }
        if (args.length == 0) {
            sendStatus(sender);
            Messages.send(sender, "map-border.usage");
            return true;
        }

        switch (args[0]) {
            case "켜기", "on", "true" -> set(sender, true);
            case "끄기", "off", "false" -> set(sender, false);
            case "상태", "status" -> sendStatus(sender);
            default -> Messages.send(sender, "map-border.usage");
        }
        return true;
    }

    private void set(CommandSender sender, boolean enabled) {
        MapConfig.borderEnabled = enabled;
        listener.refresh(); // 이미 보낸 파란 벽을 지우고, 켜면 다음 갱신 때 다시 보여 준다

        boolean saved = saveToConfig(enabled);
        Messages.send(sender, enabled ? "map-border.turned-on" : "map-border.turned-off");
        if (!saved) Messages.send(sender, "map-border.not-saved");
    }

    private void sendStatus(CommandSender sender) {
        Messages.send(sender, "map-border.status", "state", MapConfig.borderEnabled
                ? Messages.get("map-border.state-on") : Messages.get("map-border.state-off"));
        Messages.send(sender, "map-border.range",
                "min-x", (int) MapConfig.borderMinX, "max-x", (int) MapConfig.borderMaxX - 1,
                "min-z", (int) MapConfig.borderMinZ, "max-z", (int) MapConfig.borderMaxZ - 1);
    }

    /** config.yml 의 border.enabled 줄만 고쳐 쓴다. (주석 유지) 실패하면 false */
    private boolean saveToConfig(boolean enabled) {
        File file = new File(plugin.getDataFolder(), "config.yml");
        try {
            String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            String changed = MapConfig.withBorderEnabled(text, enabled);
            if (changed == null) return false;
            Files.writeString(file.toPath(), changed, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("[config.yml] border.enabled 를 저장하지 못했습니다: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (sender.isOp() && args.length == 1) {
            for (String s : SUBS) if (s.startsWith(args[0])) out.add(s);
        }
        return out;
    }
}
