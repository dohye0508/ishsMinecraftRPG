package kr.ishs.raibit.ishsMinecraftRpg.player;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;

        // 최근 전투 중인지 확인 (3초 내 피격)
        if (p.getNoDamageTicks() > 0 || p.isInsideVehicle()) {
            p.sendMessage("\u00a7c전투 중에는 로비로 이동할 수 없습니다!");
            return true;
        }

        // 월드 스폰 위치로 텔레포트
        Location spawnLoc = p.getWorld().getSpawnLocation().clone().add(0.5, 0, 0.5);
        spawnLoc.setYaw(0);
        spawnLoc.setPitch(0);
        p.teleport(spawnLoc);
        p.sendMessage("\u00a7a\u2605 \u00a7f로비로 이동했습니다!");
        return true;
    }
}
