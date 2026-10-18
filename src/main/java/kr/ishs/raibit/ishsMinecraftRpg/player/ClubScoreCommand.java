package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.config.ClubConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /동아리점수 - 동아리 점수를 수동으로 조절한다. (관리자 전용)
 * 목록 | 설정 [동아리] [점수] | 추가 [동아리] [점수] | 초기화 [동아리|전체]
 */
public class ClubScoreCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of("목록", "설정", "추가", "초기화");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            Messages.send(sender, "common.no-permission");
            return true;
        }
        if (args.length == 0) {
            Messages.send(sender, "club-score.usage");
            return true;
        }

        ClubScoreManager scores = ClubScoreManager.getInstance();
        switch (args[0]) {
            case "목록", "list" -> {
                Messages.send(sender, "club-score.list-header");
                for (ClubConfig.Club club : ClubConfig.all()) {
                    Messages.send(sender, "club-score.list-line", "club", club.prefix(), "score", scores.getScore(club.id));
                }
            }
            case "설정", "set", "추가", "add" -> {
                boolean add = args[0].equals("추가") || args[0].equals("add");
                if (args.length < 3) {
                    Messages.send(sender, "club-score.usage");
                    return true;
                }
                ClubConfig.Club club = ClubConfig.get(args[1]);
                if (club == null) {
                    Messages.send(sender, "club-score.unknown-club", "club", args[1], "clubs", clubIds());
                    return true;
                }
                int value;
                try {
                    value = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    Messages.send(sender, "club-score.bad-number", "value", args[2]);
                    return true;
                }
                if (add) scores.addScore(club.id, value);
                else scores.setScore(club.id, value);
                Messages.send(sender, "club-score.changed", "club", club.prefix(), "score", scores.getScore(club.id));
            }
            case "초기화", "reset" -> {
                if (args.length < 2 || args[1].equals("전체") || args[1].equalsIgnoreCase("all")) {
                    scores.resetAll();
                    Messages.send(sender, "club-score.reset-all");
                } else {
                    ClubConfig.Club club = ClubConfig.get(args[1]);
                    if (club == null) {
                        Messages.send(sender, "club-score.unknown-club", "club", args[1], "clubs", clubIds());
                        return true;
                    }
                    scores.reset(club.id);
                    Messages.send(sender, "club-score.changed", "club", club.prefix(), "score", 0);
                }
            }
            default -> Messages.send(sender, "club-score.usage");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (!sender.isOp()) return out;
        if (args.length == 1) {
            SUBS.stream().filter(s -> s.startsWith(args[0])).forEach(out::add);
        } else if (args.length == 2 && !args[0].equals("목록")) {
            String prefix = args[1].toUpperCase(Locale.ROOT);
            for (ClubConfig.Club c : ClubConfig.all()) if (c.id.startsWith(prefix)) out.add(c.id);
            if (args[0].equals("초기화") && "전체".startsWith(args[1])) out.add("전체");
        }
        return out;
    }

    private static String clubIds() {
        List<String> ids = new ArrayList<>();
        for (ClubConfig.Club c : ClubConfig.all()) ids.add(c.id);
        return String.join(", ", ids);
    }
}
