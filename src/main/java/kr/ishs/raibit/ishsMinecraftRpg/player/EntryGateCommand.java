package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.RpgListener;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EntryGateCommand implements CommandExecutor {
    private final RpgListener rpgListener;

    public EntryGateCommand(RpgListener rpgListener) {
        this.rpgListener = rpgListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "현재 입장 상태: "
                    + (rpgListener.isEntryOpen() ? ChatColor.GREEN + "허용" : ChatColor.RED + "차단 (관리자만 입장 가능)"));
            sender.sendMessage(ChatColor.GRAY + "사용법: /입장허용 [열기|닫기]");
            return true;
        }

        if (args[0].equalsIgnoreCase("열기")) {
            rpgListener.setEntryOpen(true);
            sender.sendMessage(ChatColor.GREEN + "[시스템] 이제부터 모든 플레이어의 입장이 허용됩니다.");
        } else if (args[0].equalsIgnoreCase("닫기")) {
            rpgListener.setEntryOpen(false);
            sender.sendMessage(ChatColor.RED + "[시스템] 관리자(OP) 외 모든 플레이어의 입장이 차단됩니다. (이미 접속중인 인원은 유지)");
        } else {
            sender.sendMessage(ChatColor.RED + "사용법: /입장허용 [열기|닫기]");
        }
        return true;
    }
}
