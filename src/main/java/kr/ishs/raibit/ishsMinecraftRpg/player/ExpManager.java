package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.CustomAchievement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * EXP 부여 및 레벨업 처리를 중앙에서 관리하는 유틸 클래스
 */
public class ExpManager {

    /**
     * 플레이어에게 EXP를 지급하고 레벨업 여부를 처리합니다.
     * @param player 대상 플레이어
     * @param amount 지급할 EXP 양
     */
    @SuppressWarnings("deprecation")
    public static void addExp(Player player, double amount) {
        PlayerData pd = PlayerManager.getInstance().getPlayerData(player);
        if (pd == null) return;

        pd.addExperience(amount);

        // 레벨업 체크 (여러 레벨 한 번에 오를 수도 있음)
        while (pd.getExperience() >= pd.getRequiredExp()) {
            pd.setExperience(pd.getExperience() - pd.getRequiredExp());
            pd.levelUp();

            int newLevel = pd.getLevel();

            // 레벨업 알림 (본인)
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "★ LEVEL UP! ★ " + ChatColor.YELLOW + "Lv." + newLevel + " 달성!");
            player.sendMessage(ChatColor.GRAY + "스탯이 자동으로 상승했습니다.");

            // 전체 서버 방송 (5의 배수 레벨만)
            if (newLevel % 5 == 0) {
                Bukkit.broadcastMessage(ChatColor.AQUA + "[서버] " + ChatColor.WHITE + player.getName()
                        + ChatColor.YELLOW + " 님이 Lv." + newLevel + " 에 도달했습니다! 🎉");
            }

            // 도전과제 체크
            if (newLevel >= 5) CustomAchievement.grant(player, "level_5", "신입의 눈빛", "레벨 5에 도달했습니다.");
            if (newLevel >= 10) CustomAchievement.grant(player, "level_10", "과학고생다워지는 중", "레벨 10에 도달했습니다.");
            if (newLevel >= 20) CustomAchievement.grant(player, "level_20", "이젠 선배", "레벨 20에 도달했습니다.");
            if (newLevel >= 50) CustomAchievement.grant(player, "level_50", "전설의 시작", "레벨 50에 도달했습니다.");
        }
    }
}
