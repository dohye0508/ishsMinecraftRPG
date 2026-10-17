package kr.ishs.raibit.ishsMinecraftRpg;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class CustomAchievement {

    /**
     * 도전과제를 달성 처리합니다.
     * title/subtitle 은 achievements.yml 에 같은 ID가 없을 때만 쓰이는 기본 문구입니다.
     * (ID가 있으면 achievements.yml 의 title/popup 문구를 사용합니다)
     */
    public static boolean grant(Player p, String trackId, String title, String subtitle) {
        AchievementConfig.Def def = AchievementConfig.get(trackId);
        if (def != null) {
            title = def.title;
            subtitle = def.popup;
        }

        kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData pd = kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager.getInstance().getPlayerData(p);
        if (pd != null) {
            if (pd.hasAchievement(trackId)) return false; // 이미 달성함
            pd.addAchievement(trackId);
        }

        // 성공 사운드 재생
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        try {
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        } catch(Throwable ignored) {} // 버전 호환성 방어

        // 화려한 화면 중앙 타이틀
        p.sendTitle(ChatColor.GOLD + "🌟 " + title + " 🌟", ChatColor.YELLOW + subtitle, 10, 70, 20);
        
        // 부가적인 채팅 안내
        p.sendMessage("");
        p.sendMessage(ChatColor.GREEN + "=============================");
        p.sendMessage(ChatColor.GOLD + " [ 🏆 도전과제 달성! 🏆 ]");
        p.sendMessage(ChatColor.WHITE + " 목표: " + ChatColor.AQUA + subtitle + " 달성");
        p.sendMessage(ChatColor.GREEN + "=============================");
        p.sendMessage("");
        return true;
    }
}
