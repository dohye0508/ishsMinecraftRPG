package kr.ishs.raibit.ishsMinecraftRpg.task;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import kr.ishs.raibit.ishsMinecraftRpg.config.ClubConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.GameplayConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import kr.ishs.raibit.ishsMinecraftRpg.player.ClubScoreManager;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 1초마다 플레이어별 사이드바, 마나 재생, 발광 효과, Tab 목록을 갱신한다.
 * 사이드바/Tab 문구와 줄 구성은 messages.yml, 마나 재생 공식은 gameplay.yml 에서 온다.
 */
public class RpgUpdateTask implements Runnable {

    /** 사이드바 템플릿에서 {club-ranking} 한 줄을 동아리 랭킹 줄들로 바꾸기 위한 임시 표식 */
    private static final String RANKING_MARKER = "\u0001club-ranking\u0001";

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

    @SuppressWarnings("deprecation")
    private void updateScoreboard(Player p) {
        PlayerData pd = PlayerManager.getInstance().getPlayerData(p.getUniqueId());
        if (pd == null) return;

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("is-rpg", "dummy", Messages.get("scoreboard.title"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int remainCd = skillManager.getRemainCooldown(p.getUniqueId());
        String cooldownText = remainCd > 0 ? Messages.get("scoreboard.cooldown", "seconds", remainCd) : "";

        List<String> templateLines = Messages.list("scoreboard.lines",
                "level", pd.getLevel(),
                "player", p.getName(),
                "exp", String.format("%.1f", pd.getExperience()),
                "exp-required", (long) pd.getRequiredExp(),
                "str", pd.getStr(),
                "dex", pd.getDex(),
                "int", pd.getIntel(),
                "hp", pd.getHpStat(),
                "wis", pd.getWis(),
                "club", pd.hasClub() ? pd.getClub() : Messages.get("scoreboard.club-none"),
                "cooldown", cooldownText,
                "mp", (int) pd.getCurrentMp(),
                "max-mp", (int) pd.getMaxMp(),
                "club-ranking", RANKING_MARKER);

        // {club-ranking} 줄을 동아리 랭킹 줄들로 펼친다
        List<String> lines = new ArrayList<>();
        for (String line : templateLines) {
            if (line.equals(RANKING_MARKER)) {
                lines.addAll(rankingLines());
            } else {
                lines.add(line.replace(RANKING_MARKER, ""));
            }
        }

        // 사이드바는 같은 글자의 줄을 하나로 합치므로, 겹치면 보이지 않는 서식 코드를 덧붙여 구분한다
        Set<String> used = new HashSet<>();
        int score = lines.size();
        for (String line : lines) {
            String entry = line;
            while (!used.add(entry)) entry += ChatColor.RESET;
            obj.getScore(entry).setScore(score--);
        }

        p.setScoreboard(board);
    }

    /** 동아리 점수 순위 줄들. 점수가 같으면 clubs.yml 의 순서를 따른다. */
    private List<String> rankingLines() {
        ClubScoreManager scoreMgr = ClubScoreManager.getInstance();
        List<ClubConfig.Club> clubs = ClubConfig.all();
        clubs.sort((c1, c2) -> Integer.compare(scoreMgr.getScore(c2.id), scoreMgr.getScore(c1.id)));

        List<String> out = new ArrayList<>();
        for (int i = 0; i < clubs.size(); i++) {
            ClubConfig.Club club = clubs.get(i);
            out.add(Messages.get("scoreboard.ranking-line",
                    "rank", i + 1, "club", club.prefix(), "score", scoreMgr.getScore(club.id)));
        }
        return out;
    }

    private void regenerateMana(Player p) {
        PlayerData pd = PlayerManager.getInstance().getPlayerData(p.getUniqueId());
        if (pd == null) return;

        // 초당 마나 재생량: gameplay.yml 의 stats.mana-regen
        pd.setCurrentMp(pd.getCurrentMp() + GameplayConfig.manaRegenPerSecond(pd.getIntel()));
    }

    private void applyPermanentEffects(Player p) {
        // 리로스쿨 인증(로그인)이 끝나기 전에는, 또는 규칙에서 꺼져 있으면 발광 효과를 켜지 않는다.
        if (!GameplayConfig.glowingPlayers || !plugin.getAuthManager().isAuthenticated(p)) {
            if (p.hasPotionEffect(PotionEffectType.GLOWING)) {
                p.removePotionEffect(PotionEffectType.GLOWING);
            }
            return;
        }

        if (!p.hasPotionEffect(PotionEffectType.GLOWING)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 999999, 0, false, false, false));
        }
    }

    @SuppressWarnings("deprecation")
    private void updateTabList(Player p) {
        int online = Bukkit.getOnlinePlayers().size();
        String header = String.join("\n", Messages.list("tab.header", "online", online));
        String footer = String.join("\n", Messages.list("tab.footer", "online", online));
        p.setPlayerListHeaderFooter(header, footer);
    }
}
