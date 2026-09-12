package kr.ishs.raibit.ishsMinecraftRpg;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class CustomAchievement {

    public static boolean grant(Player p, String trackId, String title, String subtitle) {
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
