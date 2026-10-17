package kr.ishs.raibit.ishsMinecraftRpg;

import kr.ishs.raibit.ishsMinecraftRpg.config.GameplayConfig;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import kr.ishs.raibit.ishsMinecraftRpg.gui.PluginGui;
import kr.ishs.raibit.ishsMinecraftRpg.player.ExpManager;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData;
import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.event.world.WorldLoadEvent;

/**
 * 공용 이벤트 리스너. 보상 수치, 도전과제 연결, 게임 규칙은 gameplay.yml, 문구는 messages.yml 에서 온다.
 */
public class RpgListener implements Listener {

    private final IshsMinecraftRpg plugin;

    // ★ false: OP/일반 유저 구분 없이 누구나 자유롭게 건축 및 파괴 가능
    private boolean isBuildProtectionEnabled = false;

    // true: 누구나 입장 가능 / false: 관리자(OP) 외 입장 차단 (이미 접속중인 인원은 유지)
    private boolean isEntryOpen = true;

    public RpgListener(IshsMinecraftRpg plugin) {
        this.plugin = plugin;
    }

    public boolean isBuildProtectionEnabled() {
        return isBuildProtectionEnabled;
    }

    public void setBuildProtectionEnabled(boolean enabled) {
        this.isBuildProtectionEnabled = enabled;
    }

    public boolean isEntryOpen() {
        return isEntryOpen;
    }

    public void setEntryOpen(boolean entryOpen) {
        this.isEntryOpen = entryOpen;
    }

    /**
     * 서버는 열려 있어도 관리자가 입장을 아직 허용하지 않았다면 OP를 제외한 모든 접속을 차단한다.
     * 이미 접속중인 플레이어는 영향을 받지 않는다 (신규 로그인만 차단).
     */
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (isEntryOpen) return;
        if (Bukkit.getOfflinePlayer(event.getUniqueId()).isOp()) return;

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messages.get("entry.denied"));
    }

    /**
     * 블록 파괴. 서바이벌에서 파괴 가능한지는 gameplay.yml 의 rules.survival-block-break,
     * 채굴 보상은 rewards.mining 을 따른다.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        // 기본 규칙: 서바이벌 모드에서는 전투(때리기)만 가능하고 블록 채굴/파괴는 불가능하다.
        if (p.getGameMode() == GameMode.SURVIVAL && !GameplayConfig.survivalBlockBreak) {
            event.setCancelled(true);
            return;
        }

        if (!isBuildProtectionEnabled) {
            Material type = event.getBlock().getType();
            for (GameplayConfig.MiningSpecial special : GameplayConfig.miningSpecials()) {
                if (!special.matches(type)) continue;
                CustomAchievement.grant(p, special.achievement);
                if (special.exp > 0) ExpManager.addExp(p, special.exp);
            }

            // 모든 채굴 보상 + 확률로 스탯 포인트
            ExpManager.addExp(p, GameplayConfig.miningExp);
            PlayerData pd = PlayerManager.getInstance().getPlayerData(p);
            if (pd != null && Math.random() < GameplayConfig.miningStatPointChance) {
                pd.addStatPoints(GameplayConfig.miningStatPoints);
                actionBar(p, Messages.get("reward.mining-stat-point", "points", GameplayConfig.miningStatPoints));
            }
            return;
        }

        if (!p.isOp()) {
            event.setCancelled(true);
            Messages.send(p, "build.no-break");
        }
    }

    /**
     * 블록 설치 방지 (isBuildProtectionEnabled가 false이므로 통과됨)
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isBuildProtectionEnabled) return;

        Player player = event.getPlayer();
        if (!player.isOp()) {
            event.setCancelled(true);
            Messages.send(player, "build.no-place");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldLoad(WorldLoadEvent event) {
        plugin.applyWorldSettings(event.getWorld());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.applyWorldSettings(player.getWorld());
        PlayerManager.getInstance().loadPlayer(player);
        // 데이터를 불러온 직후에 스탯(최대 HP/MP, 걷기 속도)을 적용한다.
        // (StatListener 의 접속 처리는 이 리스너보다 먼저 실행되어 데이터가 없는 상태라 적용이 건너뛰어진다)
        plugin.getStatManager().applyStats(player);
        // 입장 메시지는 AuthListener(인증 완료 후)에서 처리
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerManager.getInstance().unloadPlayer(player);
        // 퇴장 메시지는 AuthListener에서 처리
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTimeSkip(TimeSkipEvent event) {
        if (!GameplayConfig.allowTimeSkip) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (GameplayConfig.spawnAnimals) return;
        if (event.getEntityType() == EntityType.BAT || event.getEntity() instanceof Bat || event.getEntity() instanceof Animals) {
            event.setCancelled(true);
        }
    }

    /** 도전과제 창에서 아이템을 꺼내지 못하게 한다 */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (PluginGui.is(event, PluginGui.Type.ACHIEVEMENTS)) {
            event.setCancelled(true);
        }
    }

    /** PvP 규칙: gameplay.yml 의 rules.pvp */
    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            PlayerData pdA = PlayerManager.getInstance().getPlayerData(attacker);
            PlayerData pdV = PlayerManager.getInstance().getPlayerData(victim);

            if (pdA != null && pdV != null) {
                // 같은 동아리끼리는 공격 불가
                if (GameplayConfig.pvpBlockSameClub && pdA.hasClub() && pdV.hasClub() && pdA.getClub().equals(pdV.getClub())) {
                    event.setCancelled(true);
                }
                // 둘 중 하나라도 동아리가 없으면 공격 불가
                else if (GameplayConfig.pvpRequireClub && (!pdA.hasClub() || !pdV.hasClub())) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (!GameplayConfig.explosionBlockDamage) event.blockList().clear();
    }

    /** 처치 보상: gameplay.yml 의 rewards.kill, rewards.club-coop */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player p = event.getEntity().getKiller();
        if (p == null) return;

        // 처치 도전과제 & EXP 보상
        CustomAchievement.grant(p, GameplayConfig.killAchievement);
        if (GameplayConfig.killExp > 0) ExpManager.addExp(p, GameplayConfig.killExp);
        PlayerData pd = PlayerManager.getInstance().getPlayerData(p);
        if (pd == null) return;

        // 처치 시 확률로 스탯 포인트 부여
        if (Math.random() < GameplayConfig.killStatPointChance) {
            pd.addStatPoints(GameplayConfig.killStatPoints);
            actionBar(p, Messages.get("reward.kill-stat-point", "points", GameplayConfig.killStatPoints));
        }

        // 동아리 협동 확인: 같은 동아리원이 근처에 있으면 둘 다 도전과제, 동료는 경험치
        if (pd.hasClub() && event.getEntity() instanceof Monster) {
            double r = GameplayConfig.coopRadius;
            for (Entity e : p.getNearbyEntities(r, r, r)) {
                if (!(e instanceof Player)) continue;
                Player ally = (Player) e;
                PlayerData pdAlly = PlayerManager.getInstance().getPlayerData(ally);
                if (pdAlly != null && pdAlly.hasClub() && pdAlly.getClub().equals(pd.getClub())) {
                    CustomAchievement.grant(p, GameplayConfig.coopAchievement);
                    CustomAchievement.grant(ally, GameplayConfig.coopAchievement);
                    // 협동 시 파트너도 EXP 획득
                    if (GameplayConfig.coopExp > 0) ExpManager.addExp(ally, GameplayConfig.coopExp);
                    break;
                }
            }
        }
    }

    /** 사망/PvP 보상: gameplay.yml 의 rewards.death, rewards.pvp-win */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        CustomAchievement.grant(victim, GameplayConfig.deathAchievement);

        Player killer = victim.getKiller();
        if (killer != null) {
            PlayerData pdK = PlayerManager.getInstance().getPlayerData(killer);
            PlayerData pdV = PlayerManager.getInstance().getPlayerData(victim);
            if (pdK != null && pdV != null && pdK.hasClub() && pdV.hasClub() && !pdK.getClub().equals(pdV.getClub())) {
                boolean granted = CustomAchievement.grant(killer, GameplayConfig.pvpWinAchievement);
                if (granted) {
                    pdK.addStatPoints(GameplayConfig.pvpWinStatPoints); // 큰 보상
                    Messages.send(killer, "reward.pvp-win", "points", GameplayConfig.pvpWinStatPoints);
                }
            }
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (GameplayConfig.hungerLoss) return;
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getFoodLevel() < player.getFoodLevel()) {
                event.setCancelled(true);
            }
        }
    }

    /** 화면 아래쪽에 잠깐 보이는 안내 */
    private void actionBar(Player p, String text) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
    }
}
