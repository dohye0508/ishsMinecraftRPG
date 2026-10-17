package kr.ishs.raibit.ishsMinecraftRpg.listener;

import kr.ishs.raibit.ishsMinecraftRpg.config.GameplayConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import kr.ishs.raibit.ishsMinecraftRpg.gui.PluginGui;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager;
import kr.ishs.raibit.ishsMinecraftRpg.stat.StatGUI;
import kr.ishs.raibit.ishsMinecraftRpg.stat.StatManager;
import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

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

    /**
     * 리스폰하면 HP/MP 를 가득 채운다. (커스텀 HP 는 죽어도 0 으로 남아 있어서, 채워 주지 않으면
     * 리스폰 직후 아주 작은 피해에도 계속 죽는다)
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData data = playerManager.getPlayerData(player.getUniqueId());
        if (data == null) return;

        statManager.applyStats(player); // 최대 HP/MP 다시 계산
        data.setCurrentHp(data.getMaxHp());
        data.setCurrentMp(data.getMaxMp());

        // 바닐라가 리스폰 때 체력을 채운 다음에 체력바와 액션바를 맞춘다
        Bukkit.getScheduler().runTaskLater(IshsMinecraftRpg.getPlugin(IshsMinecraftRpg.class), () -> {
            if (player.isOnline()) {
                statManager.updateHealthBar(player);
                statManager.updateActionBar(player);
            }
        }, 1L);
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

    /** /stat GUI 클릭: 클릭한 칸의 스탯을 올린다. (스탯 위치와 올라가는 양은 gameplay.yml) */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!PluginGui.is(event, PluginGui.Type.STATS)) return;
        event.setCancelled(true);

        // GUI 위쪽 칸을 클릭한 경우만 처리 (내 인벤토리 클릭은 무시)
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        Player player = (Player) event.getWhoClicked();
        PlayerData data = playerManager.getPlayerData(player.getUniqueId());
        if (data == null) return;

        GameplayConfig.StatDef stat = GameplayConfig.statBySlot(event.getRawSlot());
        if (stat == null) return;

        int amount = event.isShiftClick() ? GameplayConfig.shiftClickAmount : GameplayConfig.clickAmount;
        if (data.getStatPoints() < amount) {
            Messages.send(player, "stat.not-enough-points");
            return;
        }

        data.addStat(stat.id, amount);
        data.setStatPoints(data.getStatPoints() - amount);
        statManager.applyStats(player); // 스탯 재적용
        StatGUI.open(player); // GUI 갱신
    }
}
