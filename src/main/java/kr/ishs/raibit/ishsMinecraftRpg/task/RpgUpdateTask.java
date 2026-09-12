package kr.ishs.raibit.ishsMinecraftRpg.task;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager;
import kr.ishs.raibit.ishsMinecraftRpg.player.SkillManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class RpgUpdateTask implements Runnable {
    private final IshsMinecraftRpg plugin;
    private final SkillManager skillManager;

    public RpgUpdateTask(IshsMinecraftRpg plugin, SkillManager skillManager) {
        this.plugin = plugin;
        this.skillManager = skillManager;
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            updateScoreboard(p);
            regenerateMana(p);
            applyPermanentEffects(p);
            updateTabList(p);
        }
    }

    private void updateScoreboard(Player p) {
        PlayerData pd = PlayerManager.getInstance().getPlayerData(p.getUniqueId());
        if (pd == null) return;

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("is-rpg", "dummy", ChatColor.GOLD + "ISHS RPG");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // 클라이언트 사이드바는 화면에 보이는 줄 수가 제한적이라 15줄 이내로 맞춘다.
        obj.getScore(ChatColor.WHITE + "Lv. " + pd.getLevel() + " " + p.getName()).setScore(15);
        obj.getScore(ChatColor.WHITE + "EXP: " + String.format("%.1f", pd.getExperience()) + " / " + (long)pd.getRequiredExp()).setScore(14);
        obj.getScore(ChatColor.WHITE + "🗡 STR: " + ChatColor.RED + pd.getStr() + ChatColor.WHITE + " 🏃 DEX: " + ChatColor.BLUE + pd.getDex()).setScore(13);
        obj.getScore(ChatColor.WHITE + "🔮 INT: " + ChatColor.AQUA + pd.getIntel() + ChatColor.WHITE + " ❤ HP: " + ChatColor.GREEN + pd.getHpStat() + ChatColor.WHITE + " ✨ WIS: " + ChatColor.LIGHT_PURPLE + pd.getWis()).setScore(12);

        String club = pd.hasClub() ? pd.getClub() : "없음";
        obj.getScore(ChatColor.WHITE + "소속: " + ChatColor.YELLOW + club).setScore(11);

        int remainCd = skillManager.getRemainCooldown(p.getUniqueId());
        String cdText = (remainCd > 0) ? ChatColor.YELLOW + " (쿨타임 " + remainCd + "초)" : "";
        obj.getScore(ChatColor.WHITE + "스킬:" + cdText).setScore(10);

        String manaText = ChatColor.AQUA + "💧 마나: " + (int)pd.getCurrentMp() + " / " + (int)pd.getMaxMp();
        obj.getScore(manaText).setScore(9);

        obj.getScore(ChatColor.GRAY + "--- [ 동아리 랭킹 ] ---").setScore(8);

        kr.ishs.raibit.ishsMinecraftRpg.player.ClubScoreManager scoreMgr = kr.ishs.raibit.ishsMinecraftRpg.player.ClubScoreManager.getInstance();
        String[] clubs = {"LOGIC", "WIN", "TNT", "LOTTOL", "QUASAR", "RAIBIT", "PLUTONIUM"};
        java.util.List<String> clubList = new java.util.ArrayList<>(java.util.Arrays.asList(clubs));
        clubList.sort((c1, c2) -> Integer.compare(scoreMgr.getScore(c2), scoreMgr.getScore(c1)));

        int line = 7;
        for (int i = 0; i < clubList.size(); i++) {
            String c = clubList.get(i);
            int s = scoreMgr.getScore(c);
            obj.getScore(ChatColor.WHITE + String.valueOf(i + 1) + "위. " + getClubPrefix(c) + ChatColor.WHITE + " " + s + "점").setScore(line);
            line--;
        }

        p.setScoreboard(board);
    }

    private void regenerateMana(Player p) {
        PlayerData pd = PlayerManager.getInstance().getPlayerData(p.getUniqueId());
        if (pd == null) return;

        // 초당 (2 + 지능/5) 만큼 마나 재생
        pd.setCurrentMp(pd.getCurrentMp() + (2 + pd.getIntel() / 5.0));
    }

    private void applyPermanentEffects(Player p) {
        // 리로스쿨 인증(로그인)이 끝나기 전에는 발광 효과를 켜지 않는다.
        if (!plugin.getAuthManager().isAuthenticated(p)) {
            if (p.hasPotionEffect(PotionEffectType.GLOWING)) {
                p.removePotionEffect(PotionEffectType.GLOWING);
            }
            return;
        }

        if (!p.hasPotionEffect(PotionEffectType.GLOWING)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 999999, 0, false, false, false));
        }
    }

    private void updateTabList(Player p) {
        String header = ChatColor.GOLD + "" + ChatColor.BOLD + "⚔ ISHS RPG ⚔\n"
                + ChatColor.GRAY + "현재 접속자: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size() + "명\n";
        String footer = "\n" + ChatColor.YELLOW + "[ Tab 안내 ]\n"
                + ChatColor.WHITE + "/도움말" + ChatColor.GRAY + " 전체 명령어  " + ChatColor.WHITE + "/stat" + ChatColor.GRAY + " 스탯 분배\n"
                + ChatColor.WHITE + "/퀘스트" + ChatColor.GRAY + " 진행 퀘스트  " + ChatColor.WHITE + "/도전과제" + ChatColor.GRAY + " 업적 확인\n"
                + ChatColor.WHITE + "/lobby" + ChatColor.GRAY + " 로비 이동";
        p.setPlayerListHeaderFooter(header, footer);
    }

    private String getClubPrefix(String club) {
        switch (club) {
            case "LOGIC": return ChatColor.AQUA + "📘 LOGIC";
            case "WIN": return ChatColor.RED + "🔨 WIN";
            case "TNT": return ChatColor.YELLOW + "🧨 TNT";
            case "LOTTOL": return ChatColor.GREEN + "🌾 LOTTOL";
            case "QUASAR": return ChatColor.DARK_PURPLE + "🔮 QUASAR";
            case "RAIBIT": return ChatColor.GOLD + "💻 RAIBIT";
            case "PLUTONIUM": return ChatColor.DARK_GRAY + "⚙ PLUTONIUM";
            default: return ChatColor.GRAY + "❔ " + club;
        }
    }
}
