package kr.ishs.raibit.ishsMinecraftRpg.auth;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * /인증      - 인증 재시작 (이미 인증된 경우 정보 표시)
 * /인증정보  - 본인의 리로스쿨 정보 확인
 * /인증목록  - (OP 전용) 현재 접속자 인증 현황
 */
public class AuthCommand implements CommandExecutor, TabCompleter {

    private final IshsMinecraftRpg plugin;
    private final AuthManager authManager;

    public AuthCommand(IshsMinecraftRpg plugin, AuthManager authManager) {
        this.plugin = plugin;
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c플레이어만 사용할 수 있는 명령어입니다.");
            return true;
        }

        String cmdName = command.getName().toLowerCase();

        switch (cmdName) {
            case "인증" -> handleAuthCmd(player, args);
            case "인증정보" -> handleInfoCmd(player);
            case "인증목록" -> handleListCmd(player);
            default -> player.sendMessage("§c알 수 없는 명령어입니다.");
        }

        return true;
    }

    private void handleAuthCmd(Player player, String[] args) {
        if (authManager.isAuthenticated(player)) {
            PlayerAuthData data = authManager.getAuthData(player);
            if (data != null) {
                player.sendMessage("§a이미 인증되어 있습니다.");
                printAuthInfo(player, data);
            }
        } else {
            // 재인증 시작
            authManager.startAuth(player);
        }
    }

    private void handleInfoCmd(Player player) {
        if (!authManager.isAuthenticated(player)) {
            player.sendMessage("§c아직 인증되지 않았습니다. §e/인증 §c을 먼저 진행해 주세요.");
            return;
        }
        PlayerAuthData data = authManager.getAuthData(player);
        if (data == null) {
            player.sendMessage("§c인증 데이터를 찾을 수 없습니다.");
            return;
        }
        printAuthInfo(player, data);
    }

    private void handleListCmd(Player player) {
        if (!player.isOp()) {
            player.sendMessage("§cOP 권한이 필요합니다.");
            return;
        }

        player.sendMessage("§8§l------ §e인증 현황 §8§l------");
        int count = 0;
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (authManager.isAuthenticated(p)) {
                PlayerAuthData d = authManager.getAuthData(p);
                String info = d != null
                        ? (d.isStudent() ? d.getRealName() + " (" + d.getStudentId() + ")" : d.getRealName() + " 선생님")
                        : "데이터 없음";
                player.sendMessage("§a✔ §f" + p.getName() + " §7" + info);
                count++;
            } else {
                player.sendMessage("§c✘ §f" + p.getName() + " §7(인증 미완료)");
            }
        }
        player.sendMessage("§8§l총 " + count + "/" + plugin.getServer().getOnlinePlayers().size() + "명 인증 완료");
    }

    private void printAuthInfo(Player player, PlayerAuthData data) {
        player.sendMessage("§8§l--------------------------------------------------");
        player.sendMessage("§b§l나의 인증 정보");
        player.sendMessage("§f마인크래프트: §e" + data.getPlayerName());
        player.sendMessage("§f이름: §e" + data.getRealName());
        if (data.isStudent()) {
            player.sendMessage("§f학번: §e" + data.getStudentId());
            player.sendMessage("§f기수: §e" + data.getGeneration() + "기");
        } else {
            player.sendMessage("§f역할: §e교사");
        }
        player.sendMessage("§8§l--------------------------------------------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
