package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import kr.ishs.raibit.ishsMinecraftRpg.config.ClubConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import kr.ishs.raibit.ishsMinecraftRpg.gui.PluginGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Map;

public class ClubListener implements Listener {
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");
        if (!args[0].equalsIgnoreCase("/club")) return;

        Player p = event.getPlayer();

        // 인증이 완료된 플레이어만 /club 명령어를 사용할 수 있도록 제한
        IshsMinecraftRpg plugin = IshsMinecraftRpg.getPlugin(IshsMinecraftRpg.class);
        if (!plugin.getAuthManager().isAuthenticated(p)) {
            Messages.send(p, "club.must-auth");
            return;
        }

        PlayerData data = PlayerManager.getInstance().getPlayerData(p.getUniqueId());
        if (data == null) return;

        if (args.length == 1) {
            Messages.send(p, "club.current",
                    "club", data.hasClub() ? data.getClub() : Messages.get("club.current-none"));
            Messages.send(p, "club.usage");
            return;
        }

        if (args[1].equalsIgnoreCase(Messages.get("club.keyword-leave"))) {
            if (!data.hasClub()) {
                Messages.send(p, "club.leave-none");
                return;
            }
            Messages.send(p, "club.left", "club", data.getClub());
            data.setClub(null);
            // 여기에 탈퇴 시 스탯 회수 로직을 추가할 수 있습니다.
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!PluginGui.is(event, PluginGui.Type.CLUB)) return;

        // 아이템을 가져가지 못하도록 이벤트를 취소합니다.
        event.setCancelled(true);

        // GUI 위쪽 칸을 클릭한 경우만 처리 (내 인벤토리 클릭은 무시)
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        Player p = (Player) event.getWhoClicked();
        PlayerData data = PlayerManager.getInstance().getPlayerData(p.getUniqueId());
        if (data == null || data.hasClub()) return;

        // 클릭한 칸에 있는 동아리 (칸 위치와 동아리는 clubs.yml)
        ClubConfig.Club club = ClubConfig.bySlot(event.getRawSlot());
        if (club == null) return;

        data.setClub(club.id);
        Messages.send(p, "club.joined", "club", club.id);

        // 동아리 가입 시 스탯 보너스 (clubs.yml 의 bonus)
        for (Map.Entry<String, Integer> bonus : club.bonus.entrySet()) {
            data.addStat(bonus.getKey(), bonus.getValue());
        }

        // 선택 후 인벤토리 닫기
        p.closeInventory();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!PluginGui.is(event, PluginGui.Type.CLUB)) return;

        Player player = (Player) event.getPlayer();
        PlayerData data = PlayerManager.getInstance().getPlayerData(player.getUniqueId());
        if (data != null && !data.hasClub()) {
            // 직업 선택을 강제하기 위해, 1틱 후에 다시 GUI를 열어줌
            Messages.send(player, "club.must-choose");
            Bukkit.getScheduler().runTaskLater(IshsMinecraftRpg.getPlugin(IshsMinecraftRpg.class), () -> {
                if (player.isOnline() && !data.hasClub()) ClubGUI.openClubGUI(player);
            }, 1L);
        }
    }
}
