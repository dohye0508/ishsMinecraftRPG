package kr.ishs.raibit.ishsMinecraftRpg.player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ClubGUI {

    public static void openClubGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "동아리(직업) 선택");

        inv.setItem(10, createItem(Material.BOOK, ChatColor.AQUA + "LOGIC (수학)", ChatColor.GRAY + "지능 +15, 체력 +5"));
        inv.setItem(11, createItem(Material.ANVIL, ChatColor.RED + "WIN (물리)", ChatColor.GRAY + "근력 +10, 민첩 +10"));
        inv.setItem(12, createItem(Material.TNT, ChatColor.YELLOW + "TNT (화학)", ChatColor.GRAY + "근력 +10, 지능 +10"));
        inv.setItem(13, createItem(Material.WHEAT, ChatColor.GREEN + "LOTTOL (생명과학)", ChatColor.GRAY + "체력 +15, 민첩 +5"));
        inv.setItem(14, createItem(Material.ENDER_PEARL, ChatColor.DARK_PURPLE + "QUASAR (천문학)", ChatColor.GRAY + "지능 +10, 민첩 +10"));
        inv.setItem(15, createItem(Material.REDSTONE, ChatColor.GOLD + "RAIBIT (정보)", ChatColor.GRAY + "민첩 +15, 지능 +5"));
        inv.setItem(16, createItem(Material.IRON_BLOCK, ChatColor.DARK_GRAY + "PLUTONIUM (공학)", ChatColor.GRAY + "근력 +10, 체력 +10"));

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material material, String name, String... lores) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lores));
            item.setItemMeta(meta);
        }
        return item;
    }
}
