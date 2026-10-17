package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.CustomAchievement;
import kr.ishs.raibit.ishsMinecraftRpg.config.GameplayConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import org.bukkit.entity.Player;

/**
 * EXP 부여 및 레벨업 처리를 중앙에서 관리하는 유틸 클래스.
 * 필요 경험치, 알림 간격, 레벨 도전과제는 gameplay.yml, 문구는 messages.yml 에서 온다.
 */
public class ExpManager {

    /**
     * 플레이어에게 EXP를 지급하고 레벨업 여부를 처리합니다.
     * @param player 대상 플레이어
     * @param amount 지급할 EXP 양
     */
    public static void addExp(Player player, double amount) {
        PlayerData pd = PlayerManager.getInstance().getPlayerData(player);
        if (pd == null) return;

        pd.addExperience(amount);

        // 레벨업 체크 (여러 레벨 한 번에 오를 수도 있음)
        while (true) {
            double required = pd.getRequiredExp();
            if (required <= 0 || pd.getExperience() < required) break; // 설정 오류로 필요 경험치가 0 이하여도 무한 반복하지 않음

            pd.setExperience(pd.getExperience() - required);
            pd.levelUp();

            int newLevel = pd.getLevel();

            // 레벨업 알림 (본인)
            Messages.send(player, "level-up.title", "level", newLevel);
            Messages.send(player, "level-up.hint");

            // 전체 서버 방송 (설정한 간격의 배수 레벨만)
            if (GameplayConfig.broadcastEveryLevels > 0 && newLevel % GameplayConfig.broadcastEveryLevels == 0) {
                Messages.broadcast("level-up.broadcast", "player", player.getName(), "level", newLevel);
            }

            // 도전과제 체크 (레벨 도달 도전과제는 gameplay.yml 의 leveling.level-achievements)
            for (String achievementId : GameplayConfig.achievementsUpTo(newLevel)) {
                CustomAchievement.grant(player, achievementId);
            }
        }
    }
}
