package kr.ishs.raibit.ishsMinecraftRpg;

import kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Monster;
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
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (isEntryOpen) return;
        if (Bukkit.getOfflinePlayer(event.getUniqueId()).isOp()) return;

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                ChatColor.RED + "" + ChatColor.BOLD + "⛔ 입장 제한\n\n"
                        + ChatColor.WHITE + "서버가 활성화됐지만\n"
                        + ChatColor.WHITE + "관리자가 아직 입장을 허용하지 않았습니다.\n\n"
                        + ChatColor.GRAY + "잠시 후 다시 접속해주세요.");
    }

    /**
     * 블록 파괴 방지 (isBuildProtectionEnabled가 false이므로 통과됨)
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        // 서바이벌 모드에서는 전투(때리기)만 가능하고 블록 채굴/파괴는 불가능하게 막는다.
        if (p.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
            event.setCancelled(true);
            return;
        }

        if (!isBuildProtectionEnabled) {
            if (event.getBlock().getType() == org.bukkit.Material.DIAMOND_ORE || event.getBlock().getType() == org.bukkit.Material.DEEPSLATE_DIAMOND_ORE) {
                kr.ishs.raibit.ishsMinecraftRpg.CustomAchievement.grant(p, "diamond", "진짜 보물 찾기", "깊은 곳에서 다이아몬드를 발견했습니다.");
                kr.ishs.raibit.ishsMinecraftRpg.player.ExpManager.addExp(p, 30); // 다이아 채광 +30 EXP
            }
            if (event.getBlock().getType().toString().contains("LOG")) {
                kr.ishs.raibit.ishsMinecraftRpg.CustomAchievement.grant(p, "lumberjack", "나무꾼의 길", "처음으로 원목을 채집했습니다.");
            }
            
            // 모든 체광 +2 EXP, +5% 확률 스탯 포인트
            kr.ishs.raibit.ishsMinecraftRpg.player.ExpManager.addExp(p, 2);
            kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData pd = kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager.getInstance().getPlayerData(p);
            if (pd != null && Math.random() < 0.05) {
                pd.addStatPoints(1);
                p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                        new net.md_5.bungee.api.chat.TextComponent(org.bukkit.ChatColor.AQUA + "채광/벌목 중 스탯 포인트를 1 발견했습니다!"));
            }
            return;
        }

        Player player = event.getPlayer();
        if (!player.isOp()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "블록을 부술 권한이 없습니다.");
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
            player.sendMessage(ChatColor.RED + "블록을 설치할 권한이 없습니다.");
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
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.BAT || event.getEntity() instanceof Bat || event.getEntity() instanceof Animals) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title != null && title.contains("[ 🏆 나의 도전과제 ]")) {
            event.setCancelled(true); // 아이템 빼기 방지
        }
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            
            kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData pdA = kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager.getInstance().getPlayerData(attacker);
            kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData pdV = kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager.getInstance().getPlayerData(victim);
            
            if (pdA != null && pdV != null) {
                // 동아리가 같으면 공격 불가
                if (pdA.hasClub() && pdV.hasClub() && pdA.getClub().equals(pdV.getClub())) {
                    event.setCancelled(true);
                } 
                // 둘 중 하나라도 동아리가 없으면 공격 불가
                else if (!pdA.hasClub() || !pdV.hasClub()) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        event.blockList().clear();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player p = event.getEntity().getKiller();
            // 첫 사냥 도전과제 & EXP 보상
            kr.ishs.raibit.ishsMinecraftRpg.CustomAchievement.grant(p, "first_kill", "학업 스트레스 해소", "처음으로 몬스터를 사냥했습니다!");
            kr.ishs.raibit.ishsMinecraftRpg.player.ExpManager.addExp(p, 10); // 사냥 +10 EXP
            kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData pd = kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager.getInstance().getPlayerData(p);

            if (pd != null) {
                // 사냥 시 20% 확률로 스탯 포인트 부여
                if (Math.random() < 0.2) {
                    pd.addStatPoints(1);
                    p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                            new net.md_5.bungee.api.chat.TextComponent(org.bukkit.ChatColor.GOLD + "마력 스탯 포인트를 1 획득했습니다!"));
                }

                // 동아리 협동 쿼스트 확인
                if (pd.hasClub() && event.getEntity() instanceof Monster) {
                    for (org.bukkit.entity.Entity e : p.getNearbyEntities(15, 15, 15)) {
                        if (e instanceof Player) {
                            Player ally = (Player) e;
                            kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData pdAlly = kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager.getInstance().getPlayerData(ally);
                            if (pdAlly != null && pdAlly.hasClub() && pdAlly.getClub().equals(pd.getClub())) {
                                kr.ishs.raibit.ishsMinecraftRpg.CustomAchievement.grant(p, "club_coop", "우리는 하나", "동아리원과 함께 몬스터를 처치했습니다!");
                                kr.ishs.raibit.ishsMinecraftRpg.CustomAchievement.grant(ally, "club_coop", "우리는 하나", "동아리원과 함께 몬스터를 처치했습니다!");
                                // 협동 시 파트너도 EXP 획득
                                kr.ishs.raibit.ishsMinecraftRpg.player.ExpManager.addExp(ally, 5);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        kr.ishs.raibit.ishsMinecraftRpg.CustomAchievement.grant(victim, "first_death", "잠시 쉬어가도 좋아", "너무 무리하지 마세요!");
        
        Player killer = victim.getKiller();
        if (killer != null) {
            kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData pdK = kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager.getInstance().getPlayerData(killer);
            kr.ishs.raibit.ishsMinecraftRpg.player.PlayerData pdV = kr.ishs.raibit.ishsMinecraftRpg.player.PlayerManager.getInstance().getPlayerData(victim);
            if (pdK != null && pdV != null && pdK.hasClub() && pdV.hasClub() && !pdK.getClub().equals(pdV.getClub())) {
                boolean granted = kr.ishs.raibit.ishsMinecraftRpg.CustomAchievement.grant(killer, "pvp_win", "동아리의 자랑", "타 동아리원과의 전투에서 승리했습니다!");
                if (granted) {
                    pdK.addStatPoints(5); // 큰 보상
                    killer.sendMessage(org.bukkit.ChatColor.AQUA + "전투 승리 보상으로 스탯 포인트 5를 획득했습니다!");
                }
            }
        }
    }


    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getFoodLevel() < player.getFoodLevel()) {
                event.setCancelled(true);
            }
        }
    }
}