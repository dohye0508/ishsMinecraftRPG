package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.AchievementConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import kr.ishs.raibit.ishsMinecraftRpg.gui.PluginGui;
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

    private static final int GUI_SIZE = 54;
    private static final int PROGRESS_SLOT = 4;

    // 9개 이하일 때: 3x3 중앙 정렬 그리드 (9칸)
    private static final int[] SLOTS_SMALL = {12, 13, 14, 21, 22, 23, 30, 31, 32};
    // 10개 이상일 때: 가로 7칸 x 세로 4줄 그리드 (최대 28개)
    private static final int[] SLOTS_LARGE = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Messages.send(sender, "common.players-only");
            return true;
        }
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

        Inventory inv = PluginGui.create(PluginGui.Type.ACHIEVEMENTS, GUI_SIZE,
                Messages.apply(AchievementConfig.guiTitle, "unlocked", unlockedCount, "total", entries.size()));

        ItemStack filler = createFiller();
        for (int i = 0; i < GUI_SIZE; i++) inv.setItem(i, filler);

        inv.setItem(PROGRESS_SLOT, createProgressItem(unlockedCount, entries.size()));

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
                meta.setDisplayName(Messages.apply(AchievementConfig.unlockedName, "title", e.title));
                meta.setLore(Arrays.asList(
                        Messages.apply(AchievementConfig.unlockedDescription, "description", e.desc),
                        "",
                        Messages.apply(AchievementConfig.unlockedLabel)
                ));
            } else {
                meta.setDisplayName(Messages.apply(AchievementConfig.lockedName, "title", e.title));
                meta.setLore(Arrays.asList(
                        Messages.apply(AchievementConfig.lockedDescription),
                        "",
                        Messages.apply(AchievementConfig.lockedLabel)
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
            meta.setDisplayName(Messages.apply(AchievementConfig.progressTitle));
            int percent = total == 0 ? 0 : (int) ((unlocked * 100.0) / total);
            meta.setLore(Arrays.asList(
                    Messages.apply(AchievementConfig.progressCount, "unlocked", unlocked, "total", total),
                    Messages.apply(AchievementConfig.progressPercent, "percent", percent)
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
