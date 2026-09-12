package kr.ishs.raibit.ishsMinecraftRpg.stat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            StatGUI.open(player);
        } else {
            sender.sendMessage("플레이어만 사용 가능한 명령어입니다.");
        }
        return true;
    }
}
