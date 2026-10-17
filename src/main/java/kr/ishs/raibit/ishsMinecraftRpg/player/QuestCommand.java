package kr.ishs.raibit.ishsMinecraftRpg.player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class QuestCommand implements CommandExecutor {
    // BetonQuest 오브젝티브 ID -> 표시용 이름
    // config.yml 의 quests 항목에서 읽는다. (코드 수정 없이 기획팀이 추가/수정 가능)
    // 키는 BetonQuest QuestPackages/default/main.yml 의 objectives 섹션 ID와 짝이 맞아야 한다.
    private static final Map<String, String> QUEST_NAMES = new LinkedHashMap<>();

    /** config.yml 의 quests 항목을 읽어 표시 이름 목록을 갱신합니다. */
    public static void loadQuestNames(FileConfiguration config) {
        QUEST_NAMES.clear();
        ConfigurationSection section = config.getConfigurationSection("quests");
        if (section == null) return;
        for (String objectiveId : section.getKeys(false)) {
            String displayName = section.getString(objectiveId);
            if (displayName != null && !displayName.isBlank()) {
                QUEST_NAMES.put(objectiveId, displayName);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return false;
        }
        Player p = (Player) sender;

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            p.sendMessage(ChatColor.RED + "퀘스트 정보를 불러올 수 없습니다. (PlaceholderAPI 미설치)");
            return true;
        }

        p.sendMessage(ChatColor.GOLD + "==== [ 📜 진행중인 퀘스트 ] ====");
        boolean any = false;
        for (Map.Entry<String, String> entry : QUEST_NAMES.entrySet()) {
            Integer left = getObjectiveLeft(p, entry.getKey());
            if (left == null || left <= 0) continue;
            any = true;
            p.sendMessage(ChatColor.WHITE + "• " + entry.getValue() + ChatColor.YELLOW + " (남음: " + left + ")");
        }
        if (!any) {
            p.sendMessage(ChatColor.GRAY + "현재 진행 중인 퀘스트가 없습니다. NPC에게 말을 걸어보세요!");
        }
        p.sendMessage(ChatColor.GOLD + "===============================");
        return true;
    }

    private Integer getObjectiveLeft(Player p, String objectiveId) {
        String raw = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p,
                "%betonquest_default:objective." + objectiveId + ".left%");
        if (raw == null) return null;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return null; // 아직 시작 안 했거나 이미 완료된 퀘스트
        }
    }
}
