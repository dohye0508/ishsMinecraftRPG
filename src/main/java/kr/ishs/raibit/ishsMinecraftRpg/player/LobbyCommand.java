package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Messages.send(sender, "common.players-only");
            return true;
        }
        Player p = (Player) sender;

        // 최근 전투 중인지 확인 (3초 내 피격)
        if (p.getNoDamageTicks() > 0 || p.isInsideVehicle()) {
            Messages.send(p, "lobby.in-combat");
            return true;
        }

        // 월드 스폰 위치로 텔레포트 (월드 스폰은 config.yml 의 spawn.first)
        Location spawnLoc = p.getWorld().getSpawnLocation().clone().add(0.5, 0, 0.5);
        spawnLoc.setYaw(0);
        spawnLoc.setPitch(0);
        p.teleport(spawnLoc);
        Messages.send(p, "lobby.moved");
        return true;
    }
}
