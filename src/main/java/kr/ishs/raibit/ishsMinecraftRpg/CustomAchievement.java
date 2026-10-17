package kr.ishs.raibit.ishsMinecraftRpg;

import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class CustomAchievement {

    /**
     * 도전과제를 달성 처리합니다. 이름과 알림 문구는 achievements.yml 에서 가져옵니다.
     * (achievements.yml 에 없는 ID 이면 ID 를 그대로 보여 줍니다)
     *
     * @return 이번에 새로 달성했으면 true, 이미 달성했거나 ID 가 비어 있으면 false
     */
    @SuppressWarnings("deprecation")
    public static boolean grant(Player p, String id) {
        if (id == null || id.isBlank()) return false;

        PlayerData pd = PlayerManager.getInstance().getPlayerData(p);
        if (pd != null) {
            if (pd.hasAchievement(id)) return false; // 이미 달성함
            pd.addAchievement(id);
        }

        AchievementConfig.Def def = AchievementConfig.get(id);
        String title = def != null ? def.title : id;
        String subtitle = def != null ? def.popup : id;

        // 성공 사운드 재생
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        try {
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        } catch (Throwable ignored) {} // 버전 호환성 방어

        // 화면 중앙 타이틀과 채팅 안내 (문구는 achievements.yml 의 texts)
        p.sendTitle(Messages.apply(AchievementConfig.popupTitle, "title", title, "subtitle", subtitle),
                Messages.apply(AchievementConfig.popupSubtitle, "title", title, "subtitle", subtitle), 10, 70, 20);
        for (String line : AchievementConfig.popupChat) {
            p.sendMessage(Messages.apply(line, "title", title, "subtitle", subtitle));
        }
        return true;
    }
}
