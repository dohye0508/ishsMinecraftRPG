package kr.ishs.raibit.ishsMinecraftRpg.item;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomItemListener implements Listener {
    private static final Map<UUID, Long> demeritCooldowns = new HashMap<>();
    private static final long DEMERIT_COOLDOWN_MS = 6000L;

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        String id = CustomItems.getId(item);
        if (id == null) return;

        if (CustomItems.JASMINE_FLOWER.equals(id)) {
            event.setCancelled(true);
            double healed = Math.min(player.getHealth() + 6.0, player.getMaxHealth());
            player.setHealth(healed);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_HONEY_BOTTLE_DRINK, 1f, 1.2f);
            player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.2, 0), 6, 0.4, 0.4, 0.4, 0.1);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "🌸 자스민 꽃을 상처에 발랐습니다. 통증이 가라앉는다...");
            consumeOne(player, item);
            return;
        }

        if (CustomItems.SCHEMA.equals(id)) {
            event.setCancelled(true);
            int duration = 20 * 90; // 90초
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1));
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
            player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 40, 0.5, 0.8, 0.5, 1);
            player.sendMessage(ChatColor.GOLD + "📜 스키마를 발동합니다! (90초간 무기 강화 + 이동속도 증가)");
            consumeOne(player, item);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player attacker = (Player) event.getDamager();
        String id = CustomItems.getId(attacker.getInventory().getItemInMainHand());
        if (id == null) return;

        LivingEntity target = (LivingEntity) event.getEntity();

        if (CustomItems.DEMERIT_CARD.equals(id)) {
            if (!offCooldown(attacker, demeritCooldowns, DEMERIT_COOLDOWN_MS)) return;
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 5, 1));
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.6f);
            attacker.sendMessage(ChatColor.RED + "📇 벌점카드 발동! 상대가 약화되었습니다.");
            if (target instanceof Player) {
                ((Player) target).sendMessage(ChatColor.RED + "왕사감의 벌점카드에 정신이 아득해진다... (약화 효과)");
            }
        } else if (CustomItems.CHANGBONG_SPEAR.equals(id)) {
            org.bukkit.util.Vector push = target.getLocation().toVector()
                    .subtract(attacker.getLocation().toVector()).normalize().setY(0.3);
            target.setVelocity(target.getVelocity().add(push.multiply(1.8)));
            target.getWorld().playSound(target.getLocation(), Sound.ITEM_TRIDENT_HIT, 1f, 1f);
        } else if (CustomItems.CHANGBONG_ROD.equals(id)) {
            org.bukkit.util.Vector launch = target.getVelocity();
            launch.setY(1.1);
            target.setVelocity(launch);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1f, 1.4f);
        }
    }

    private boolean offCooldown(Player attacker, Map<UUID, Long> cooldowns, long cooldownMs) {
        long now = System.currentTimeMillis();
        long last = cooldowns.getOrDefault(attacker.getUniqueId(), 0L);
        if (now - last < cooldownMs) return false;
        cooldowns.put(attacker.getUniqueId(), now);
        return true;
    }

    private void consumeOne(Player player, ItemStack item) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (item.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            item.setAmount(item.getAmount() - 1);
        }
    }
}
