package kr.ishs.raibit.ishsMinecraftRpg.player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AchievementCommand implements CommandExecutor {

    // 3x3 중앙 정렬 그리드 (9칸)
    private static final int[] SLOTS = {12, 13, 14, 21, 22, 23, 30, 31, 32};

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;
        PlayerData pd = PlayerManager.getInstance().getPlayerData(p);

        List<AchEntry> entries = new ArrayList<>();
        entries.add(new AchEntry(Material.TOTEM_OF_UNDYING, "인천과학고 입학", "동아리를 선택하여 무사히 입학 완료!",
                pd != null && pd.hasAchievement("join_club")));
        entries.add(new AchEntry(Material.DIAMOND, "진짜 보물 찾기", "깊은 곳에서 다이아몬드를 발견했습니다.",
                pd != null && pd.hasAchievement("diamond")));
        entries.add(new AchEntry(Material.OAK_LOG, "나무꾼의 길", "처음으로 원목을 채집했습니다.",
                pd != null && pd.hasAchievement("lumberjack")));
        entries.add(new AchEntry(Material.ZOMBIE_HEAD, "학업 스트레스 해소", "처음으로 몬스터를 사냥했습니다!",
                pd != null && pd.hasAchievement("first_kill")));
        entries.add(new AchEntry(Material.EXPERIENCE_BOTTLE, "자기계발의 시작", "나만의 길을 걷기 위해 스탯을 올렸습니다.",
                pd != null && pd.hasAchievement("stat_up")));
        entries.add(new AchEntry(Material.SKELETON_SKULL, "잠시 쉬어가도 좋아", "가끔은 쓰러져도 괜찮습니다.",
                pd != null && pd.hasAchievement("first_death")));
        entries.add(new AchEntry(Material.IRON_SWORD, "동아리의 자랑", "타 동아리원과의 전투에서 승리했습니다!",
                pd != null && pd.hasAchievement("pvp_win")));
        entries.add(new AchEntry(Material.GOLDEN_APPLE, "우리는 하나", "동아리원과 함께 몬스터를 처치했습니다!",
                pd != null && pd.hasAchievement("club_coop")));

        int npcCount = 0;
        if (pd != null) {
            for (String a : pd.getAchievements()) {
                if (a.startsWith("first_npc_")) npcCount++;
            }
        }
        entries.add(new AchEntry(Material.EMERALD, "NPC와의 조우", "서버의 특별한 NPC들과 " + npcCount + "명 만남!", npcCount > 0));

        int unlockedCount = 0;
        for (AchEntry e : entries) if (e.unlocked) unlockedCount++;

        // RpgListener의 아이템 취득 방지 로직이 "[ 🏆 나의 도전과제 ]" 문자열을 그대로 찾으므로
        // 이 부분은 색상 코드로 끊기지 않도록 유지한다.
        // 인벤토리 제목 배경은 밝은 색이라 GOLD/YELLOW 계열은 대비가 낮아 잘 안 보이므로 BLACK 사용.
        Inventory inv = Bukkit.createInventory(null, 54,
                ChatColor.BLACK + "[ 🏆 나의 도전과제 ] " + ChatColor.DARK_GRAY + "(" + unlockedCount + "/" + entries.size() + ")");

        ItemStack filler = createFiller();
        for (int i = 0; i < 54; i++) inv.setItem(i, filler);

        inv.setItem(4, createProgressItem(unlockedCount, entries.size()));

        for (int i = 0; i < entries.size() && i < SLOTS.length; i++) {
            inv.setItem(SLOTS[i], createItem(entries.get(i)));
        }

        p.openInventory(inv);
        return true;
    }

    private static class AchEntry {
        final Material material;
        final String title;
        final String desc;
        final boolean unlocked;

        AchEntry(Material material, String title, String desc, boolean unlocked) {
            this.material = material;
            this.title = title;
            this.desc = desc;
            this.unlocked = unlocked;
        }
    }

    @SuppressWarnings("deprecation")
    private ItemStack createItem(AchEntry e) {
        ItemStack item = new ItemStack(e.unlocked ? e.material : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (e.unlocked) {
                meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + e.title);
                meta.setLore(Arrays.asList(
                        ChatColor.WHITE + e.desc,
                        "",
                        ChatColor.GREEN + "✔ 달성 완료"
                ));
            } else {
                meta.setDisplayName(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "??? " + ChatColor.DARK_GRAY + e.title);
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "아직 밝혀지지 않은 업적입니다.",
                        "",
                        ChatColor.RED + "✘ 미달성"
                ));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @SuppressWarnings("deprecation")
    private ItemStack createProgressItem(int unlocked, int total) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "달성 현황");
            int percent = total == 0 ? 0 : (int) ((unlocked * 100.0) / total);
            meta.setLore(Arrays.asList(
                    ChatColor.WHITE + "" + unlocked + " / " + total + " 개 달성",
                    ChatColor.YELLOW + "" + percent + "% 완료"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    @SuppressWarnings("deprecation")
    private ItemStack createFiller() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }
}
