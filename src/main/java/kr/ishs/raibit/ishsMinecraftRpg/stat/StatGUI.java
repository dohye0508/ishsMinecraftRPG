package kr.ishs.raibit.ishsMinecraftRpg.stat;

import kr.ishs.raibit.ishsMinecraftRpg.config.GameplayConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import kr.ishs.raibit.ishsMinecraftRpg.gui.PluginGui;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * /stat GUI. 스탯의 이름, 아이콘, 위치, 클릭당 증가량은 gameplay.yml, 문구는 messages.yml 에서 온다.
 */
public class StatGUI {

    @SuppressWarnings("deprecation")
    public static void open(Player player) {
        PlayerData data = PlayerManager.getInstance().getPlayerData(player.getUniqueId());
        if (data == null) return;

        Inventory inv = PluginGui.create(PluginGui.Type.STATS, GameplayConfig.statGuiSize, Messages.get("stat.gui-title"));

        // 스탯 아이템 생성 및 배치
        for (GameplayConfig.StatDef stat : GameplayConfig.stats()) {
            inv.setItem(stat.slot, createItem(stat.material, Messages.color(stat.guiName),
                    Messages.get("stat.gui-stat", "label", stat.label, "value", data.getStat(stat.id)),
                    Messages.get("stat.gui-hint", "click", GameplayConfig.clickAmount, "shift", GameplayConfig.shiftClickAmount)));
        }

        // 플레이어 정보 아이템
        List<String> infoLore = Messages.list("stat.gui-info",
                "level", data.getLevel(),
                "exp", String.format("%.2f", data.getExperience()),
                "exp-required", String.format("%.0f", data.getRequiredExp()),
                "points", data.getStatPoints());
        inv.setItem(GameplayConfig.statInfoSlot, createItem(Material.PLAYER_HEAD,
                Messages.get("stat.gui-player", "player", player.getName()), infoLore.toArray(new String[0])));

        player.openInventory(inv);
    }

    @SuppressWarnings("deprecation")
    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(new ArrayList<>(Arrays.asList(lore)));
        item.setItemMeta(meta);
        return item;
    }
}
