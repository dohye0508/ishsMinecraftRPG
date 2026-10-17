package kr.ishs.raibit.ishsMinecraftRpg.stat;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import kr.ishs.raibit.ishsMinecraftRpg.config.GameplayConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 스탯으로 능력치(최대 HP/MP, 걷기 속도)를 계산해 적용하고 체력/마나 표시를 갱신한다.
 * 계산 공식의 수치는 gameplay.yml (GameplayConfig)에서 온다.
 */
public class StatManager {

    /** 마인크래프트 기본 체력바는 칸 수(20)가 고정이라, 커스텀 HP를 이 비율로 환산해 보여 준다. */
    private static final double VANILLA_HEALTH_BAR = 20.0;

    private final IshsMinecraftRpg plugin;
    private final PlayerManager playerManager;

    public StatManager(IshsMinecraftRpg plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }

    public void applyStats(Player player) {
        PlayerData data = playerManager.getPlayerData(player.getUniqueId());
        if (data == null) return;

        double maxHp = GameplayConfig.maxHp(data.getHpStat());
        data.setMaxHp(maxHp);

        double maxMp = GameplayConfig.maxMp(data.getIntel());
        data.setMaxMp(maxMp);

        // 접속 시 체력/마나를 최대로 설정 (또는 저장된 값 로드 후 보정)
        if (data.getCurrentHp() <= 0 || data.getCurrentHp() > maxHp) {
            data.setCurrentHp(maxHp);
        }
        if (data.getCurrentMp() <= 0 || data.getCurrentMp() > maxMp) {
            data.setCurrentMp(maxMp);
        }

        player.setWalkSpeed(GameplayConfig.walkSpeed(data.getDex()));

        updateHealthBar(player);
    }

    @SuppressWarnings("deprecation")
    public void updateHealthBar(Player player) {
        PlayerData data = playerManager.getPlayerData(player.getUniqueId());
        if (data == null) return;

        double maxHp = data.getMaxHp();
        double currentHp = data.getCurrentHp();

        player.setMaxHealth(VANILLA_HEALTH_BAR);
        // 현재 체력을 비율에 맞게 설정, 최소 1.0으로 설정하여 죽지 않게 함
        player.setHealth(Math.max(1.0, (currentHp / maxHp) * VANILLA_HEALTH_BAR));
    }

    public void updateActionBar(Player player) {
        PlayerData data = playerManager.getPlayerData(player.getUniqueId());
        if (data == null || !player.isOnline()) return;

        String text = Messages.get("stat.actionbar",
                "hp", (int) data.getCurrentHp(), "max-hp", (int) data.getMaxHp(),
                "mp", (int) data.getCurrentMp(), "max-mp", (int) data.getMaxMp());
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
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
