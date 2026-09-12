package kr.ishs.raibit.ishsMinecraftRpg.listener;

import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager;
import kr.ishs.raibit.ishsMinecraftRpg.stat.StatGUI;
import kr.ishs.raibit.ishsMinecraftRpg.stat.StatManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class StatListener implements Listener {

    private final PlayerManager playerManager;
    private final StatManager statManager;

    public StatListener(PlayerManager playerManager, StatManager statManager) {
        this.playerManager = playerManager;
        this.statManager = statManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // 플레이어 데이터가 로드된 후 스탯 적용
        statManager.applyStats(player);
        statManager.updateActionBar(player);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerData data = playerManager.getPlayerData(player.getUniqueId());
            if (data == null) return;

            // 기본 데미지 시스템을 무시하고 커스텀 HP를 직접 제어
            event.setCancelled(true);

            double damage = event.getFinalDamage();
            data.setCurrentHp(data.getCurrentHp() - damage);

            if (data.getCurrentHp() <= 0) {
                // 플레이어 사망 처리
                player.setHealth(0);
            } else {
                // 체력바 및 액션바 즉시 업데이트
                statManager.updateHealthBar(player);
                statManager.updateActionBar(player);
            }
        }
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerData data = playerManager.getPlayerData(player.getUniqueId());
            if (data == null) return;

            // 기본 회복 시스템을 무시하고 커스텀 HP를 직접 제어
            event.setCancelled(true);

            double regainAmount = event.getAmount();
            data.setCurrentHp(data.getCurrentHp() + regainAmount);

            // 체력바 및 액션바 즉시 업데이트
            statManager.updateHealthBar(player);
            statManager.updateActionBar(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("스탯 정보")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        PlayerData data = playerManager.getPlayerData(player.getUniqueId());
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
        statManager.applyStats(player); // 스탯 재적용
        StatGUI.open(player); // GUI 갱신
    }
}
