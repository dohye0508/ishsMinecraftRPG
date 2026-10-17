package kr.ishs.raibit.ishsMinecraftRpg.npc;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

/**
 * /엔피씨 추가 [키] [이름]  - 지금 서 있는 곳에 NPC 를 추가한다. (npcs.yml 맨 아래에 항목이 적힌다)
 * /엔피씨 이동 [키]        - NPC 를 지금 서 있는 곳(바라보는 방향 포함)으로 옮긴다. (npcs.yml 좌표도 바뀐다)
 * /엔피씨 삭제 [키]        - NPC 를 삭제한다. (npcs.yml 에서도 지워진다)
 * /엔피씨 목록            - npcs.yml 의 NPC 목록
 * 이름, 스킨 수정은 npcs.yml 을 직접 고친 뒤 /설정리로드 로 적용한다. (관리자 전용)
 */
public class NpcCommand implements CommandExecutor {

    private final IshsMinecraftRpg plugin;

    public NpcCommand(IshsMinecraftRpg plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            Messages.send(sender, "common.no-permission");
            return true;
        }
        if (args.length == 0) {
            Messages.send(sender, "npc.usage");
            return true;
        }

        switch (args[0]) {
            case "목록", "list" -> {
                Messages.send(sender, "npc.list-header", "count", NpcConfig.all().size());
                for (NpcConfig.Npc npc : NpcConfig.all().values()) {
                    Messages.send(sender, "npc.list-line", "key", npc.key, "name", npc.name,
                            "x", (int) Math.floor(npc.x), "y", (int) Math.floor(npc.y), "z", (int) Math.floor(npc.z),
                            "conversation", npc.conversation);
                }
            }
            case "추가", "add" -> add(sender, args);
            case "이동", "move" -> move(sender, args);
            case "삭제", "remove" -> remove(sender, args);
            default -> Messages.send(sender, "npc.usage");
        }
        return true;
    }

    private void add(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Messages.send(sender, "common.players-only");
            return;
        }
        if (args.length < 3) {
            Messages.send(sender, "npc.usage");
            return;
        }
        String key = args[1].toLowerCase(Locale.ROOT);
        if (!NpcConfig.KEY_PATTERN.matcher(key).matches()) {
            Messages.send(sender, "npc.bad-key", "key", args[1]);
            return;
        }
        if (NpcConfig.all().containsKey(key)) {
            Messages.send(sender, "npc.already-exists", "key", key);
            return;
        }

        String name = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        Location loc = player.getLocation();
        String block = String.format(Locale.ROOT,
                "\n  %s:\n    name: \"%s\"\n    location: {x: %.2f, y: %.2f, z: %.2f, yaw: %d}\n    look-at-player: true\n    conversation: %s\n",
                key, name.replace("\\", "\\\\").replace("\"", "\\\""),
                loc.getX(), loc.getY(), loc.getZ(), Math.round(loc.getYaw()), key);

        File file = new File(plugin.getDataFolder(), "npcs.yml");
        try {
            String text = Files.exists(file.toPath()) ? Files.readString(file.toPath(), StandardCharsets.UTF_8) : "npcs:\n";
            if (!text.endsWith("\n")) text += "\n";
            Files.writeString(file.toPath(), text + block, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Messages.send(sender, "npc.write-failed", "error", e.getMessage());
            return;
        }

        plugin.reloadNpcs();
        Messages.send(sender, "npc.added", "key", key, "name", name);
    }

    private void move(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Messages.send(sender, "common.players-only");
            return;
        }
        if (args.length < 2) {
            Messages.send(sender, "npc.usage");
            return;
        }
        String key = args[1].toLowerCase(Locale.ROOT);
        Location loc = player.getLocation();
        String inline = String.format(Locale.ROOT, "{x: %.2f, y: %.2f, z: %.2f, yaw: %d}",
                loc.getX(), loc.getY(), loc.getZ(), Math.round(loc.getYaw()));
        if (edit(sender, key, text -> NpcFile.setLocation(text, key, inline))) {
            Messages.send(sender, "npc.moved", "key", key);
        }
    }

    private void remove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Messages.send(sender, "npc.usage");
            return;
        }
        String key = args[1].toLowerCase(Locale.ROOT);
        if (edit(sender, key, text -> NpcFile.remove(text, key))) {
            Messages.send(sender, "npc.removed", "key", key);
        }
    }

    /** npcs.yml 을 고쳐 저장하고 NPC 를 다시 맞춘다. 키가 없거나 쓰기에 실패하면 안내하고 false. */
    private boolean edit(CommandSender sender, String key, java.util.function.UnaryOperator<String> change) {
        File file = new File(plugin.getDataFolder(), "npcs.yml");
        try {
            String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            String changed = change.apply(text);
            if (changed == null) {
                Messages.send(sender, "npc.unknown-key", "key", key);
                return false;
            }
            Files.writeString(file.toPath(), changed, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Messages.send(sender, "npc.write-failed", "error", e.getMessage());
            return false;
        }
        plugin.reloadNpcs();
        return true;
    }
}
