package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ClubListener implements Listener {
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");
        if (!args[0].equalsIgnoreCase("/club")) return;

        Player p = event.getPlayer();

        // [수정] 인증이 완료된 플레이어만 /club 명령어를 사용할 수 있도록 제한
        IshsMinecraftRpg plugin = IshsMinecraftRpg.getPlugin(IshsMinecraftRpg.class);
        if (!plugin.getAuthManager().isAuthenticated(p)) {
            p.sendMessage(ChatColor.RED + "먼저 /인증 을 완료해야 합니다.");
            return;
        }

        PlayerData data = PlayerManager.getInstance().getPlayerData(p.getUniqueId());
        if (data == null) return;

        if (args.length == 1) {
            p.sendMessage(ChatColor.YELLOW + "현재 소속: " + (data.hasClub() ? data.getClub() : "없음"));
            p.sendMessage(ChatColor.GRAY + "/club <가입|탈퇴> [동아리명]");
            return;
        }

        if (args[1].equalsIgnoreCase("탈퇴")) {
            if (!data.hasClub()) {
                p.sendMessage(ChatColor.RED + "가입한 동아리가 없습니다.");
                return;
            }
            p.sendMessage(ChatColor.YELLOW + data.getClub() + " 동아리에서 탈퇴했습니다.");
            data.setClub(null);
            // 여기에 탈퇴 시 스탯 회수 로직을 추가할 수 있습니다.
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("동아리(직업) 선택")) return;

        // 아이템을 가져가지 못하도록 이벤트를 취소합니다.
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        Player p = (Player) event.getWhoClicked();
        PlayerData data = PlayerManager.getInstance().getPlayerData(p.getUniqueId());
        if (data == null || data.hasClub()) return;

        String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        String club = itemName.split(" ")[0].toUpperCase();

        data.setClub(club);
        p.sendMessage(ChatColor.GREEN + club + " 동아리에 가입했습니다.");

        // 동아리 가입 시 스탯 보너스
        switch (club.toLowerCase()) {
            case "logic": // 지능 +15, 체력 +5
                data.setIntel(data.getIntel() + 15);
                data.setHpStat(data.getHpStat() + 5);
                break;
            case "win": // 힘 +10, 민첩 +10
                data.setStr(data.getStr() + 10);
                data.setDex(data.getDex() + 10);
                break;
            case "tnt": // 힘 +10, 지능 +10
                data.setStr(data.getStr() + 10);
                data.setIntel(data.getIntel() + 10);
                break;
            case "lottol": // 체력 +15, 민첩 +5
                data.setHpStat(data.getHpStat() + 15);
                data.setDex(data.getDex() + 5);
                break;
            case "quasar": // 지능 +10, 민첩 +10
                data.setIntel(data.getIntel() + 10);
                data.setDex(data.getDex() + 10);
                break;
            case "raibit": // 민첩 +15, 지능 +5
                data.setDex(data.getDex() + 15);
                data.setIntel(data.getIntel() + 5);
                break;
            case "plutonium": // 힘 +10, 체력 +10
                data.setStr(data.getStr() + 10);
                data.setHpStat(data.getHpStat() + 10);
                break;
        }

        // 선택 후 인벤토리 닫기
        p.closeInventory();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals("동아리(직업) 선택")) return;

        Player player = (Player) event.getPlayer();
        PlayerData data = PlayerManager.getInstance().getPlayerData(player.getUniqueId());
        if (data != null && !data.hasClub()) {
            // 직업 선택을 강제하기 위해, 1틱 후에 다시 GUI를 열어줌
            player.sendMessage(ChatColor.RED + "반드시 동아리를 선택해야 합니다!");
            Bukkit.getScheduler().runTaskLater(IshsMinecraftRpg.getPlugin(IshsMinecraftRpg.class), () -> {
                if (player.isOnline() && !data.hasClub()) ClubGUI.openClubGUI(player);
            }, 1L);
        }
    }
}
