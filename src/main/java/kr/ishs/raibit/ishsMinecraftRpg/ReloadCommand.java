package kr.ishs.raibit.ishsMinecraftRpg;

import kr.ishs.raibit.ishsMinecraftRpg.item.CustomItems;
import kr.ishs.raibit.ishsMinecraftRpg.listener.MapBorderListener;
import kr.ishs.raibit.ishsMinecraftRpg.player.QuestCommand;
import kr.ishs.raibit.ishsMinecraftRpg.stat.GameConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * /설정리로드 - config.yml을 다시 읽어 서버 재시작 없이 적용합니다. (관리자 전용)
 * 기획팀이 config.yml의 값(스폰, 인증 장소, 맵 경계, 퀘스트 표시 이름 등)을 고친 뒤 사용합니다.
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
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        }

        plugin.reloadConfig();
        GameConfig.load(plugin.getConfig());
        MapConfig.load(plugin.getConfig());
        QuestCommand.loadQuestNames(plugin.getConfig());
        plugin.reloadContent(); // items.yml, achievements.yml, help.yml

        for (World world : Bukkit.getWorlds()) {
            plugin.applyWorldSettings(world); // 월드 스폰 지점 등 다시 적용
        }
        mapBorderListener.refresh(); // 맵 경계 표시/범위 다시 적용

        sender.sendMessage(ChatColor.GREEN + "[시스템] config.yml을 다시 불러왔습니다.");
        sender.sendMessage(ChatColor.GRAY + " - 리로스쿨 인증: " + (MapConfig.authEnabled ? "사용 (다음 접속부터 적용)" : "끔"));
        sender.sendMessage(ChatColor.GRAY + " - 맵 경계: " + (MapConfig.borderEnabled
                ? "x " + (int) MapConfig.borderMinX + "~" + ((int) MapConfig.borderMaxX - 1)
                + ", z " + (int) MapConfig.borderMinZ + "~" + ((int) MapConfig.borderMaxZ - 1)
                : "끔"));
        sender.sendMessage(ChatColor.GRAY + " - 최초 스폰: " + (int) Math.floor(MapConfig.spawnX) + ", "
                + (int) Math.floor(MapConfig.spawnY) + ", " + (int) Math.floor(MapConfig.spawnZ));
        sender.sendMessage(ChatColor.GRAY + " - 아이템 " + CustomItems.getIds().size() + "종, 도전과제 "
                + AchievementConfig.all().size() + "개 불러옴 (items.yml, achievements.yml, help.yml)");
        sender.sendMessage(ChatColor.GRAY + "BetonQuest(대화/퀘스트) 수정은 /bq reload 로 따로 적용하세요.");
        return true;
    }
}
