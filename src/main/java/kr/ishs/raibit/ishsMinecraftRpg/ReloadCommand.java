package kr.ishs.raibit.ishsMinecraftRpg;

import kr.ishs.raibit.ishsMinecraftRpg.config.ClubConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import kr.ishs.raibit.ishsMinecraftRpg.item.CustomItems;
import kr.ishs.raibit.ishsMinecraftRpg.listener.MapBorderListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * /설정리로드 - 모든 설정 파일을 다시 읽어 서버 재시작 없이 적용합니다. (관리자 전용)
 * 기획팀이 config/gameplay/clubs/items/achievements/help/skills/messages.yml 을 고친 뒤 사용합니다.
 */
public class ReloadCommand implements CommandExecutor {

    private final IshsMinecraftRpg plugin;
    private final MapBorderListener mapBorderListener;

    public ReloadCommand(IshsMinecraftRpg plugin, MapBorderListener mapBorderListener) {
        this.plugin = plugin;
        this.mapBorderListener = mapBorderListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            Messages.send(sender, "common.no-permission");
            return true;
        }

        plugin.reloadAll();

        for (World world : Bukkit.getWorlds()) {
            plugin.applyWorldSettings(world); // 게임 규칙, 월드 스폰 지점 다시 적용
        }
        mapBorderListener.refresh(); // 맵 경계 표시/범위 다시 적용

        Messages.send(sender, "reload.done");
        Messages.send(sender, "reload.auth", "state",
                MapConfig.authEnabled ? Messages.get("reload.auth-on") : Messages.get("reload.auth-off"));
        Messages.send(sender, "reload.border", "state", MapConfig.borderEnabled
                ? Messages.get("reload.border-range",
                        "min-x", (int) MapConfig.borderMinX, "max-x", (int) MapConfig.borderMaxX - 1,
                        "min-z", (int) MapConfig.borderMinZ, "max-z", (int) MapConfig.borderMaxZ - 1)
                : Messages.get("reload.border-off"));
        Messages.send(sender, "reload.spawn",
                "x", (int) Math.floor(MapConfig.spawnX), "y", (int) Math.floor(MapConfig.spawnY), "z", (int) Math.floor(MapConfig.spawnZ));
        Messages.send(sender, "reload.content",
                "items", CustomItems.getIds().size(), "achievements", AchievementConfig.all().size(), "clubs", ClubConfig.all().size());
        int npcs = plugin.reloadNpcs();
        if (npcs >= 0) Messages.send(sender, "reload.npc", "count", npcs);
        else Messages.send(sender, "reload.npc-missing");
        Messages.send(sender, "reload.betonquest");
        return true;
    }
}
