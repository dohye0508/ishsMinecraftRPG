package kr.ishs.raibit.ishsMinecraftRpg.player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class QuestCommand implements CommandExecutor {
    // BetonQuest 오브젝티브 ID -> 표시용 이름
    // configs/BetonQuest/QuestPackages/default/main.yml 의 objectives 섹션과 짝이 맞아야 함
    private static final Map<String, String> QUEST_NAMES = new LinkedHashMap<>();
    static {
        QUEST_NAMES.put("logic_quest_obj", "LOGIC 부짱 - 좀비 사냥");
        QUEST_NAMES.put("win_quest_obj", "WIN 부짱 - 스켈레톤 사냥");
        QUEST_NAMES.put("tnt_quest_obj", "TNT 부짱 - 크리퍼 사냥");
        QUEST_NAMES.put("lottol_quest_obj", "LOTTOL 부짱 - 거미 사냥");
        QUEST_NAMES.put("quasar_quest_obj", "QUASAR 부짱 - 돌 채굴");
        QUEST_NAMES.put("raibit_quest_obj", "RAIBIT 부짱 - 레드스톤 광석 채굴");
        QUEST_NAMES.put("plutonium_quest_obj", "PLUTONIUM 부짱 - 철 광석 채굴");
        QUEST_NAMES.put("dorm1_quest_obj", "1학년 사감쌤 - 빵 제작");
        QUEST_NAMES.put("dorm2_quest_obj", "2학년 사감쌤 - 다이아몬드 채굴");
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
