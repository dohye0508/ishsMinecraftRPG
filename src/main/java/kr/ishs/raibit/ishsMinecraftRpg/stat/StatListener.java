package kr.ishs.raibit.ishsMinecraftRpg.stat;

import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class StatListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("스탯 정보")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        PlayerData data = PlayerManager.getInstance().getPlayerData(player.getUniqueId());
        if (data == null || event.getCurrentItem() == null) return;

        Material type = event.getCurrentItem().getType();
        int amount = event.isShiftClick() ? 10 : 1;

        if (data.getStatPoints() < amount) {
            player.sendMessage("§c스탯 포인트가 부족합니다.");
            return;
        }

        switch (type) {
            case REDSTONE:
                data.setStr(data.getStr() + amount);
                break;
            case FEATHER:
                data.setDex(data.getDex() + amount);
                break;
            case BOOK:
                data.setIntel(data.getIntel() + amount);
                break;
            case APPLE:
                data.setHpStat(data.getHpStat() + amount);
                break;
            case NETHER_STAR:
                data.setWis(data.getWis() + amount);
                break;
            default:
                return;
        }

        data.setStatPoints(data.getStatPoints() - amount);
        StatGUI.open(player); // GUI 갱신
    }
}
