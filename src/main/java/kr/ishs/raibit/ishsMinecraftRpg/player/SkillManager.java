package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillManager implements Listener {
    private final IshsMinecraftRpg plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public SkillManager(IshsMinecraftRpg plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Action a = event.getAction();
        ItemStack item = event.getItem();

        if ((a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) && item != null) {
            if (item.getType() == Material.BLAZE_ROD && "파이어볼".equals(item.getItemMeta().getDisplayName())) {
                useSkill(p, "fireball", 5, 20);
            } else if (item.getType() == Material.DIAMOND_SWORD && "대지 가르기".equals(item.getItemMeta().getDisplayName())) {
                useSkill(p, "earthquake", 10, 30);
            } else if (item.getType() == Material.ENCHANTED_BOOK && "신성 폭발".equals(item.getItemMeta().getDisplayName())) {
                useSkill(p, "holybomb", 15, 40);
            } else if (item.getType() == Material.GOLDEN_APPLE && "치유의 빛".equals(item.getItemMeta().getDisplayName())) {
                useSkill(p, "heal", 8, 25);
            }
        }
    }

    private void useSkill(Player p, String skillId, int cooldown, int manaCost) {
        UUID uuid = p.getUniqueId();
        if (cooldowns.containsKey(uuid) && System.currentTimeMillis() - cooldowns.get(uuid) < cooldown * 1000) {
            p.sendMessage(ChatColor.RED + "스킬을 아직 사용할 수 없습니다.");
            return;
        }

        PlayerData pd = PlayerManager.getInstance().getPlayerData(uuid);
        if (pd.getCurrentMp() < manaCost) {
            p.sendMessage(ChatColor.RED + "마나가 부족합니다.");
            return;
        }

        cooldowns.put(uuid, System.currentTimeMillis());
        pd.setCurrentMp(pd.getCurrentMp() - manaCost);

        switch (skillId) {
            case "fireball":
                p.launchProjectile(org.bukkit.entity.Fireball.class);
                break;
            case "earthquake":
                Location loc = p.getLocation();
                p.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                new BukkitRunnable() {
                    int i = 0;
                    @Override
                    public void run() {
                        if (i >= 5) {
                            this.cancel();
                            return;
                        }
                        for (Entity e : p.getNearbyEntities(5, 5, 5)) {
                            if (e instanceof LivingEntity && e != p) {
                                ((LivingEntity) e).damage(pd.getStr() * 3.5 + 10.0, p);
                                e.setVelocity(new Vector(0, 1, 0));
                            }
                        }
                        i++;
                    }
                }.runTaskTimer(plugin, 0L, 10L);
                break;
            case "holybomb":
                Location holyLoc = p.getLocation();
                // 가장 기본적인 파티클인 CRIT으로 변경하여 호환성 문제 해결
                p.getWorld().spawnParticle(Particle.CRIT, holyLoc, 50, 3, 3, 3, 0.1);
                p.getWorld().playSound(holyLoc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
                for (Entity e : p.getNearbyEntities(7, 7, 7)) {
                    if (e instanceof LivingEntity && e != p) {
                        ((LivingEntity) e).damage(pd.getIntel() * 3.0 + 15.0, p);
                    }
                }
                break;
            case "heal":
                int heal = (int) (pd.getIntel() * 3 + 10);
                pd.setCurrentHp(pd.getCurrentHp() + heal);
                p.sendMessage(ChatColor.GREEN + "생명력을 " + heal + "만큼 회복했습니다.");
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
                break;
        }
    }
    public int getRemainCooldown(UUID uuid) {
        if (!cooldowns.containsKey(uuid)) return 0;
        long time = System.currentTimeMillis() - cooldowns.get(uuid);
        if (time > 10000) return 0; // 10초 이상이면 표시 안함
        return (int) (10 - time / 1000);
    }
}
