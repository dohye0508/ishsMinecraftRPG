package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import kr.ishs.raibit.ishsMinecraftRpg.config.ClubConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.GameplayConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /스탯조정 - 접속 중인 플레이어의 레벨, 경험치, 스탯 포인트, 스탯, 동아리를 수동으로 조절한다. (관리자 전용)
 * [플레이어] 정보 | [플레이어] [항목] 설정|추가 [값] | [플레이어] 동아리 [동아리|없음] | [플레이어] 초기화
 */
public class PlayerAdjustCommand implements CommandExecutor, TabCompleter {

    private final IshsMinecraftRpg plugin;

    public PlayerAdjustCommand(IshsMinecraftRpg plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            Messages.send(sender, "common.no-permission");
            return true;
        }
        if (args.length < 2) {
            Messages.send(sender, "admin.usage", "fields", String.join(", ", PlayerAdjust.fieldNames()));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        PlayerData data = target == null ? null : PlayerManager.getInstance().getPlayerData(target);
        if (target == null || data == null) {
            Messages.send(sender, "admin.player-not-found", "player", args[0]);
            return true;
        }

        String action = args[1];
        if (action.equals("정보") || action.equalsIgnoreCase("info")) {
            sendInfo(sender, target, data);
            return true;
        }
        if (action.equals("초기화") || action.equalsIgnoreCase("reset")) {
            PlayerAdjust.reset(data);
            finish(target, data);
            Messages.send(sender, "admin.reset", "player", target.getName());
            return true;
        }
        if (action.equals("동아리") || action.equalsIgnoreCase("club")) {
            return setClub(sender, target, data, args);
        }

        // [플레이어] [항목] [설정|추가] [값]
        PlayerAdjust.Field field = PlayerAdjust.resolve(action);
        if (field == null) {
            Messages.send(sender, "admin.unknown-field", "field", action, "fields", String.join(", ", PlayerAdjust.fieldNames()));
            return true;
        }
        if (args.length < 4) {
            Messages.send(sender, "admin.usage", "fields", String.join(", ", PlayerAdjust.fieldNames()));
            return true;
        }
        boolean add;
        switch (args[2]) {
            case "설정", "set" -> add = false;
            case "추가", "add" -> add = true;
            default -> {
                Messages.send(sender, "admin.usage", "fields", String.join(", ", PlayerAdjust.fieldNames()));
                return true;
            }
        }
        double value;
        try {
            value = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            Messages.send(sender, "admin.bad-number", "value", args[3]);
            return true;
        }

        PlayerAdjust.Outcome result = PlayerAdjust.apply(data, field, add, value);
        if (!result.ok()) {
            Messages.send(sender, result.error(), "field", result.label(), "range", result.detail());
            return true;
        }
        finish(target, data);
        Messages.send(sender, "admin.changed", "player", target.getName(), "field", result.label(),
                "old", result.oldValue(), "new", result.newValue());
        return true;
    }

    private boolean setClub(CommandSender sender, Player target, PlayerData data, String[] args) {
        if (args.length < 3) {
            Messages.send(sender, "admin.usage", "fields", String.join(", ", PlayerAdjust.fieldNames()));
            return true;
        }
        String arg = args[2];
        if (arg.equals("없음") || arg.equalsIgnoreCase("none")) {
            data.setClub(null);
            finish(target, data);
            Messages.send(sender, "admin.club-changed", "player", target.getName(), "club", Messages.get("scoreboard.club-none"));
            return true;
        }
        ClubConfig.Club club = ClubConfig.get(arg);
        if (club == null) {
            Messages.send(sender, "club-score.unknown-club", "club", arg, "clubs", clubIds());
            return true;
        }
        data.setClub(club.id);
        finish(target, data);
        Messages.send(sender, "admin.club-changed", "player", target.getName(), "club", club.prefix());
        return true;
    }

    private void sendInfo(CommandSender sender, Player target, PlayerData data) {
        Messages.send(sender, "admin.info-header", "player", target.getName());
        Messages.send(sender, "admin.info-basic", "level", data.getLevel(),
                "exp", String.format(Locale.ROOT, "%.1f", data.getExperience()),
                "exp-required", (long) data.getRequiredExp(), "points", data.getStatPoints(),
                "club", data.hasClub() ? data.getClub() : Messages.get("scoreboard.club-none"));
        StringBuilder stats = new StringBuilder();
        for (String id : GameplayConfig.STAT_IDS) {
            if (stats.length() > 0) stats.append("  ");
            stats.append(GameplayConfig.stat(id).label).append(' ').append(data.getStat(id));
        }
        Messages.send(sender, "admin.info-stats", "stats", stats.toString());
    }

    /** 바뀐 값을 능력치(HP/MP/속도)에 반영하고 저장한다. */
    private void finish(Player target, PlayerData data) {
        plugin.getStatManager().applyStats(target);
        PlayerManager.getInstance().savePlayerData(target.getUniqueId(), data);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (!sender.isOp()) return out;
        if (args.length == 1) {
            for (Player p : Bukkit.getOnlinePlayers()) if (p.getName().toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))) out.add(p.getName());
        } else if (args.length == 2) {
            List<String> all = new ArrayList<>(PlayerAdjust.fieldNames());
            all.addAll(List.of("정보", "동아리", "초기화"));
            for (String s : all) if (s.startsWith(args[1])) out.add(s);
        } else if (args.length == 3) {
            if (args[1].equals("동아리")) {
                out.add("없음");
                for (ClubConfig.Club c : ClubConfig.all()) out.add(c.id);
            } else if (PlayerAdjust.resolve(args[1]) != null) {
                out.addAll(List.of("설정", "추가"));
            }
        }
        return out;
    }

    private static String clubIds() {
        List<String> ids = new ArrayList<>();
        for (ClubConfig.Club c : ClubConfig.all()) ids.add(c.id);
        return String.join(", ", ids);
    }
}
