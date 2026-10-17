package kr.ishs.raibit.ishsMinecraftRpg.player;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * /도움말 명령어 처리기. 문구는 help.yml 에서 읽는다. (기획팀이 코드 없이 수정 가능)
 */
@SuppressWarnings("deprecation")
public class HelpCommand implements CommandExecutor {

    private static final List<String> LINES = new ArrayList<>();
    private static final List<String> OP_LINES = new ArrayList<>();
    private static final List<String> FOOTER = new ArrayList<>();

    /** help.yml 을 읽어 도움말 문구를 갱신합니다. */
    public static void load(FileConfiguration config) {
        replace(LINES, config.getStringList("lines"));
        replace(OP_LINES, config.getStringList("op-lines"));
        replace(FOOTER, config.getStringList("footer"));
    }

    private static void replace(List<String> target, List<String> source) {
        target.clear();
        for (String line : source) {
            target.add(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }

        Player player = (Player) sender;

        for (String line : LINES) player.sendMessage(line);
        if (player.isOp()) {
            for (String line : OP_LINES) player.sendMessage(line);
        }
        for (String line : FOOTER) player.sendMessage(line);

        return true;
    }
}
