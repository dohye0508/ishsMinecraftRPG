package kr.ishs.raibit.ishsMinecraftRpg.stat;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatColor;


public class StatManager {

    private final IshsMinecraftRpg plugin;
    private final PlayerManager playerManager;

    public StatManager(IshsMinecraftRpg plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }

    public void applyStats(Player player) {
        PlayerData data = playerManager.getPlayerData(player.getUniqueId());
        if (data == null) return;

        // 최대 HP 계산: 1pt당 +15
        double maxHp = 100 + (data.getHpStat() * 15); // 기본 HP 100으로 가정
        data.setMaxHp(maxHp);

        // 최대 MP 계산: 1pt당 +10
        double maxMp = 50 + (data.getIntel() * 10); // 기본 MP 50으로 가정
        data.setMaxMp(maxMp);

        // 접속 시 체력/마나를 최대로 설정 (또는 저장된 값 로드 후 보정)
        if (data.getCurrentHp() <= 0 || data.getCurrentHp() > maxHp) {
            data.setCurrentHp(maxHp);
        }
        if (data.getCurrentMp() <= 0 || data.getCurrentMp() > maxMp) {
            data.setCurrentMp(maxMp);
        }

        // 이동 속도 계산: 1pt당 +0.03% (기본 0.2)
        float baseSpeed = 0.2f;
        float speedBonus = (float) (data.getDex() * 0.0003); // 0.03% -> 0.0003
        float finalSpeed = baseSpeed + (baseSpeed * Math.min(speedBonus, 0.5f)); // 최대 50% 증가
        player.setWalkSpeed(finalSpeed);

        updateHealthBar(player);
    }

    public void updateHealthBar(Player player) {
        PlayerData data = playerManager.getPlayerData(player.getUniqueId());
        if (data == null) return;

        double maxHp = data.getMaxHp();
        double currentHp = data.getCurrentHp();

        player.setMaxHealth(20.0); // 마인크래프트 기본 체력바는 20으로 고정
        // 현재 체력을 비율에 맞게 설정, 최소 1.0으로 설정하여 죽지 않게 함
        player.setHealth(Math.max(1.0, (currentHp / maxHp) * 20.0));
    }

    public void updateActionBar(Player player) {
        PlayerData data = playerManager.getPlayerData(player.getUniqueId());
        if (data == null || !player.isOnline()) return;

        // "❤ HP: 150/150 | 🔮 MP: 80/100"
        TextComponent hpComponent = new TextComponent("❤ HP: " + (int)data.getCurrentHp() + "/" + (int)data.getMaxHp());
        hpComponent.setColor(ChatColor.RED);

        TextComponent separator = new TextComponent("  |  ");
        separator.setColor(ChatColor.GRAY);

        TextComponent mpComponent = new TextComponent("🔮 MP: " + (int)data.getCurrentMp() + "/" + (int)data.getMaxMp());
        mpComponent.setColor(ChatColor.AQUA);

        TextComponent finalComponent = new TextComponent();
        finalComponent.addExtra(hpComponent);
        finalComponent.addExtra(separator);
        finalComponent.addExtra(mpComponent);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, finalComponent);
    }

    public void startActionBarTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updateActionBar(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 5L); // 0.25초(5틱) 간격으로 실행
    }
}
