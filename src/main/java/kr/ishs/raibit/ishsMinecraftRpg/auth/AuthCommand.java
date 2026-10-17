package kr.ishs.raibit.ishsMinecraftRpg.auth;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
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
 * 문구는 messages.yml 의 auth 항목에서 온다.
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
            Messages.send(sender, "common.players-only");
            return true;
        }

        String cmdName = command.getName().toLowerCase();

        switch (cmdName) {
            case "인증" -> handleAuthCmd(player, args);
            case "인증정보" -> handleInfoCmd(player);
            case "인증목록" -> handleListCmd(player);
            default -> Messages.send(player, "auth.unknown-command");
        }

        return true;
    }

    private void handleAuthCmd(Player player, String[] args) {
        if (authManager.isAuthenticated(player)) {
            PlayerAuthData data = authManager.getAuthData(player);
            if (data != null) {
                Messages.send(player, "auth.already");
                printAuthInfo(player, data);
            }
        } else {
            // 재인증 시작
            authManager.startAuth(player);
        }
    }

    private void handleInfoCmd(Player player) {
        if (!authManager.isAuthenticated(player)) {
            Messages.send(player, "auth.not-authenticated");
            return;
        }
        PlayerAuthData data = authManager.getAuthData(player);
        if (data == null) {
            Messages.send(player, "auth.no-data");
            return;
        }
        printAuthInfo(player, data);
    }

    private void handleListCmd(Player player) {
        if (!player.isOp()) {
            Messages.send(player, "auth.op-required");
            return;
        }

        Messages.send(player, "auth.list-header");
        int count = 0;
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (authManager.isAuthenticated(p)) {
                PlayerAuthData d = authManager.getAuthData(p);
                String info;
                if (d == null) {
                    info = Messages.get("auth.list-info-none");
                } else if (d.isStudent()) {
                    info = Messages.get("auth.list-info-student", "name", d.getRealName(), "id", d.getStudentId());
                } else {
                    info = Messages.get("auth.list-info-teacher", "name", d.getRealName());
                }
                Messages.send(player, "auth.list-ok", "player", p.getName(), "info", info);
                count++;
            } else {
                Messages.send(player, "auth.list-fail", "player", p.getName());
            }
        }
        Messages.send(player, "auth.list-footer", "count", count, "total", plugin.getServer().getOnlinePlayers().size());
    }

    private void printAuthInfo(Player player, PlayerAuthData data) {
        Messages.send(player, "auth.info-header");
        Messages.send(player, data.isStudent() ? "auth.info-student" : "auth.info-teacher",
                "player", data.getPlayerName(), "name", data.getRealName(),
                "id", data.getStudentId(), "generation", data.getGeneration());
        Messages.send(player, "auth.info-footer");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
