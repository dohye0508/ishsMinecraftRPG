package kr.ishs.raibit.ishsMinecraftRpg.stat;

import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class StatGUI {
    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "스탯 정보");
        PlayerData data = PlayerManager.getInstance().getPlayerData(player.getUniqueId());

        // 스탯 아이템 생성 및 배치
        inv.setItem(20, createStatItem(Material.REDSTONE, "근력 (STR)",
                ChatColor.WHITE + "현재 근력: " + data.getStr(),
                ChatColor.GRAY + "좌클릭: +1 | Shift+좌클릭: +10"));
        inv.setItem(21, createStatItem(Material.FEATHER, "민첩 (DEX)",
                ChatColor.WHITE + "현재 민첩: " + data.getDex(),
                ChatColor.GRAY + "좌클릭: +1 | Shift+좌클릭: +10"));
        inv.setItem(22, createStatItem(Material.BOOK, "지능 (INT)",
                ChatColor.WHITE + "현재 지능: " + data.getIntel(),
                ChatColor.GRAY + "좌클릭: +1 | Shift+좌클릭: +10"));
        inv.setItem(23, createStatItem(Material.APPLE, "체력 (HP)",
                ChatColor.WHITE + "현재 체력: " + data.getHpStat(),
                ChatColor.GRAY + "좌클릭: +1 | Shift+좌클릭: +10"));
        inv.setItem(24, createStatItem(Material.NETHER_STAR, "정신력 (WIS)",
                ChatColor.WHITE + "현재 정신력: " + data.getWis(),
                ChatColor.GRAY + "좌클릭: +1 | Shift+좌클릭: +10"));


        // 플레이어 정보 아이템
        inv.setItem(4, createStatItem(Material.PLAYER_HEAD, ChatColor.AQUA + player.getName(),
                ChatColor.WHITE + "레벨: " + data.getLevel(),
                ChatColor.WHITE + "경험치: " + String.format("%.2f", data.getExperience()) + " / " + String.format("%.0f", data.getRequiredExp()),
                ChatColor.WHITE + "스탯 포인트: " + data.getStatPoints()));

        player.openInventory(inv);
    }

    private static ItemStack createStatItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
