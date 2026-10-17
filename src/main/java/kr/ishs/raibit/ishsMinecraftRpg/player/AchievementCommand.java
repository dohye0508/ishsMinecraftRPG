package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.AchievementConfig;
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

/**
 * /도전과제 GUI. 도전과제 목록과 문구는 achievements.yml 에서 읽는다.
 */
public class AchievementCommand implements CommandExecutor {

    // 9개 이하일 때: 3x3 중앙 정렬 그리드 (9칸)
    private static final int[] SLOTS_SMALL = {12, 13, 14, 21, 22, 23, 30, 31, 32};
    // 10개 이상일 때: 가로 7칸 x 세로 4줄 그리드 (최대 28개)
    private static final int[] SLOTS_LARGE = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;
        PlayerData pd = PlayerManager.getInstance().getPlayerData(p);

        List<AchEntry> entries = new ArrayList<>();
        for (AchievementConfig.Def def : AchievementConfig.all()) {
            if (!def.showInGui) continue;

            boolean unlocked;
            String desc = def.description;
            if (def.countPrefix != null) {
                // 접두어로 시작하는 달성 기록의 개수를 센다 (예: first_npc_*)
                int count = 0;
                if (pd != null) {
                    for (String a : pd.getAchievements()) {
                        if (a.startsWith(def.countPrefix)) count++;
                    }
                }
                desc = desc.replace("{count}", String.valueOf(count));
                unlocked = count > 0;
            } else {
                unlocked = pd != null && pd.hasAchievement(def.id);
            }
            entries.add(new AchEntry(def.material, def.title, desc, unlocked));
        }

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

        int[] slots = entries.size() <= SLOTS_SMALL.length ? SLOTS_SMALL : SLOTS_LARGE;
        for (int i = 0; i < entries.size() && i < slots.length; i++) {
            inv.setItem(slots[i], createItem(entries.get(i)));
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
                        ChatColor.GREEN + AchievementConfig.unlockedLabel
                ));
            } else {
                meta.setDisplayName(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "??? " + ChatColor.DARK_GRAY + e.title);
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + AchievementConfig.lockedDescription,
                        "",
                        ChatColor.RED + AchievementConfig.lockedLabel
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
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + AchievementConfig.progressTitle);
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
