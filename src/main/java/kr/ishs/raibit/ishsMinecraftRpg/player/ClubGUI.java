package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.config.ClubConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.GameplayConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import kr.ishs.raibit.ishsMinecraftRpg.gui.PluginGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 동아리 선택 GUI. 동아리 목록(이름, 아이콘, 위치, 보너스)은 clubs.yml, 문구는 messages.yml 에서 온다.
 */
public class ClubGUI {

    public static void openClubGUI(Player player) {
        Inventory inv = PluginGui.create(PluginGui.Type.CLUB, ClubConfig.guiSize, Messages.get("club.gui-title"));

        for (ClubConfig.Club club : ClubConfig.all()) {
            inv.setItem(club.slot, createItem(club.material, club.displayName(), bonusText(club)));
        }

        player.openInventory(inv);
    }

    /** 동아리 가입 보너스를 "지능 +15, 체력 +5" 같은 한 줄 설명으로 만듭니다. */
    static String bonusText(ClubConfig.Club club) {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, Integer> e : club.bonus.entrySet()) {
            GameplayConfig.StatDef stat = GameplayConfig.stat(e.getKey());
            String label = stat != null ? stat.label : e.getKey();
            parts.add(Messages.get("club.bonus-entry", "stat", label, "amount", e.getValue()));
        }
        return Messages.get("club.bonus-lore", "bonuses", String.join(Messages.get("club.bonus-separator"), parts));
    }

    @SuppressWarnings("deprecation")
    private static ItemStack createItem(Material material, String name, String... lores) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            for (String line : lores) lore.add(line);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
