package kr.ishs.raibit.ishsMinecraftRpg.item;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 커스텀 아이템 효과 실행기. 어떤 아이템이 어떤 효과를 내는지는 items.yml 의 effects 항목에 정의되어 있고,
 * 이 클래스는 그 정의대로 발동만 한다. (ItemEffect 참고)
 */
public class CustomItemListener implements Listener {

    /** "플레이어ID:아이템ID:효과번호" → 마지막 발동 시각(ms) */
    private static final Map<String, Long> LAST_USED = new HashMap<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        String id = CustomItems.getId(item);
        if (id == null) return;

        List<ItemEffect> effects = CustomItems.getEffects(id);
        boolean hasRightClick = false;
        boolean consume = false;

        for (int i = 0; i < effects.size(); i++) {
            ItemEffect effect = effects.get(i);
            if (effect.trigger != ItemEffect.Trigger.RIGHT_CLICK) continue;
            hasRightClick = true;

            if (!offCooldown(player, id, i, effect.cooldownMs)) continue;
            run(effect, player, null);
            if (effect.consume) consume = true;
        }

        // 우클릭 효과가 있는 아이템은 쿨타임 중이어도 기본 우클릭 동작(블록 설치 등)을 막는다
        if (hasRightClick) event.setCancelled(true);
        if (consume) consumeOne(player, item);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player attacker = (Player) event.getDamager();
        String id = CustomItems.getId(attacker.getInventory().getItemInMainHand());
        if (id == null) return;

        LivingEntity target = (LivingEntity) event.getEntity();
        List<ItemEffect> effects = CustomItems.getEffects(id);

        for (int i = 0; i < effects.size(); i++) {
            ItemEffect effect = effects.get(i);
            if (effect.trigger != ItemEffect.Trigger.HIT) continue;
            if (!offCooldown(attacker, id, i, effect.cooldownMs)) continue;
            run(effect, attacker, target);
        }
    }

    /** 효과 한 건을 실행한다. target 은 HIT 일 때만 있다. */
    @SuppressWarnings("deprecation")
    private void run(ItemEffect effect, Player user, LivingEntity target) {
        for (ItemEffect.Action action : effect.actions) {
            switch (action.type) {
                case HEAL:
                    user.setHealth(Math.min(user.getHealth() + action.amount, user.getMaxHealth()));
                    break;
                case POTION: {
                    LivingEntity receiver = action.onTarget ? target : user;
                    if (receiver != null) {
                        receiver.addPotionEffect(new PotionEffect(action.potion, action.ticks, action.amplifier));
                    }
                    break;
                }
                case KNOCKBACK: {
                    if (target == null) break;
                    Vector push = target.getLocation().toVector().subtract(user.getLocation().toVector());
                    if (push.lengthSquared() == 0) break; // 같은 위치면 방향을 정할 수 없다
                    push.normalize().setY(action.lift);
                    target.setVelocity(target.getVelocity().add(push.multiply(action.strength)));
                    break;
                }
                case LAUNCH: {
                    if (target == null) break;
                    Vector launch = target.getVelocity();
                    launch.setY(action.height);
                    target.setVelocity(launch);
                    break;
                }
                default:
                    break;
            }
        }

        // 소리/파티클: 우클릭은 사용자 위치, 공격 적중은 맞은 대상 위치
        Location where = (target != null ? target : user).getLocation();
        if (effect.sound != null) {
            where.getWorld().playSound(where, effect.sound, effect.soundVolume, effect.soundPitch);
        }
        if (effect.particle != null) {
            where.getWorld().spawnParticle(effect.particle, where.clone().add(0, 1.0, 0), effect.particleCount,
                    effect.particleSpread, effect.particleSpread, effect.particleSpread, effect.particleSpeed);
        }

        if (effect.message != null) user.sendMessage(effect.message);
        if (effect.targetMessage != null && target instanceof Player) {
            ((Player) target).sendMessage(effect.targetMessage);
        }
    }

    private boolean offCooldown(Player user, String itemId, int effectIndex, long cooldownMs) {
        if (cooldownMs <= 0) return true;
        String key = user.getUniqueId() + ":" + itemId + ":" + effectIndex;
        long now = System.currentTimeMillis();
        long last = LAST_USED.getOrDefault(key, 0L);
        if (now - last < cooldownMs) return false;
        LAST_USED.put(key, now);
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
