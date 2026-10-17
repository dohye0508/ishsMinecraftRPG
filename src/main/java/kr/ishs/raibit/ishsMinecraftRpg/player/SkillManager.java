package kr.ishs.raibit.ishsMinecraftRpg.player;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import kr.ishs.raibit.ishsMinecraftRpg.config.Constants;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 스킬. 스킬 목록과 수치(쿨타임, 마나, 피해량 등)는 skills.yml, 문구는 messages.yml 에서 온다.
 * 스킬은 "특정 재료 + 특정 이름"의 아이템을 들고 우클릭하면 발동한다.
 */
public class SkillManager implements Listener {

    /** 스킬 종류. 종류는 코드에 연결되어 있고, 수치는 skills.yml 에서 정한다. */
    private enum Type { FIREBALL, EARTHQUAKE, HOLY_BOMB, HEAL }

    /** skills.yml 의 스킬 한 건 */
    private static final class Skill {
        String id;
        Type type;
        Material triggerMaterial;
        String triggerName;      // 색상 적용된 아이템 이름
        int cooldownSeconds;
        double mana;
        double radius;
        int pulses;
        long intervalTicks;
        double damageBase;
        double damagePerStat;    // EARTHQUAKE: 힘, HOLY_BOMB: 지능
        double launchHeight;
        Sound sound;
        float soundVolume;
        Particle particle;
        int particleCount;
        double particleSpread;
        double healBase;
        double healPerIntel;
        int regenTicks;
        int regenAmplifier;
    }

    /** 마지막 스킬 사용 시각과 그 스킬의 쿨타임 */
    private static final class LastUse {
        final long usedAt;
        final long cooldownMs;

        LastUse(long usedAt, long cooldownMs) {
            this.usedAt = usedAt;
            this.cooldownMs = cooldownMs;
        }
    }

    private final IshsMinecraftRpg plugin;
    private final Map<UUID, LastUse> lastUse = new HashMap<>();
    private final List<Skill> skills = new ArrayList<>();

    public SkillManager(IshsMinecraftRpg plugin) {
        this.plugin = plugin;
    }

    /** skills.yml 을 읽어 스킬 목록을 갱신합니다. */
    public void load(FileConfiguration config, Logger log) {
        skills.clear();
        ConfigurationSection section = config.getConfigurationSection("skills");
        if (section == null) {
            log.warning("[skills.yml] skills 항목이 없습니다. 스킬이 없습니다.");
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(id);
            if (s == null) continue;
            String where = "[skills.yml] " + id + ": ";

            Skill skill = new Skill();
            skill.id = id;
            try {
                skill.type = Type.valueOf(s.getString("type", "").trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                log.warning(where + "type 이 올바르지 않아 건너뜁니다. (FIREBALL, EARTHQUAKE, HOLY_BOMB, HEAL)");
                continue;
            }

            skill.triggerMaterial = Material.matchMaterial(s.getString("trigger.material", ""));
            if (skill.triggerMaterial == null || Constants.isAir(skill.triggerMaterial)) {
                log.warning(where + "trigger.material 을(를) 찾을 수 없어 건너뜁니다.");
                continue;
            }
            String name = s.getString("trigger.name", "");
            if (name.isBlank()) {
                log.warning(where + "trigger.name 이 비어 있어 건너뜁니다.");
                continue;
            }
            skill.triggerName = Messages.color(name);

            skill.cooldownSeconds = s.getInt("cooldown", 0);
            skill.mana = s.getDouble("mana", 0.0);
            skill.radius = s.getDouble("radius", 0.0);
            skill.pulses = Math.max(1, s.getInt("pulses", 1));
            skill.intervalTicks = Math.max(1, s.getLong("interval-ticks", 10L));
            skill.launchHeight = s.getDouble("launch-height", 0.0);
            skill.damageBase = s.getDouble("damage.base", 0.0);
            skill.damagePerStat = skill.type == Type.EARTHQUAKE
                    ? s.getDouble("damage.per-str", 0.0) : s.getDouble("damage.per-intel", 0.0);

            if (s.contains("sound")) {
                skill.sound = Constants.find(Sound.class, s.getString("sound"));
                if (skill.sound == null) log.warning(where + "sound '" + s.getString("sound") + "' 을(를) 찾을 수 없어 무시합니다.");
            }
            skill.soundVolume = (float) s.getDouble("sound-volume", 1.0);

            if (s.contains("particle")) {
                skill.particle = Constants.find(Particle.class, s.getString("particle"));
                if (skill.particle == null || skill.particle.getDataType() != Void.class) {
                    log.warning(where + "particle '" + s.getString("particle") + "' 을(를) 사용할 수 없어 무시합니다.");
                    skill.particle = null;
                }
            }
            skill.particleCount = Math.max(1, s.getInt("particle-count", 10));
            skill.particleSpread = s.getDouble("particle-spread", 0.0);

            skill.healBase = s.getDouble("heal.base", 0.0);
            skill.healPerIntel = s.getDouble("heal.per-intel", 0.0);
            skill.regenTicks = (int) Math.round(s.getDouble("regeneration.seconds", 0.0) * 20.0);
            skill.regenAmplifier = Math.max(1, s.getInt("regeneration.level", 1)) - 1;

            skills.add(skill);
        }
    }

    public int skillCount() {
        return skills.size();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Action a = event.getAction();
        ItemStack item = event.getItem();

        if ((a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) && item != null) {
            Skill skill = find(item);
            if (skill != null) useSkill(p, skill);
        }
    }

    /** 들고 있는 아이템에 해당하는 스킬 (없으면 null) */
    @SuppressWarnings("deprecation")
    private Skill find(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String displayName = meta.getDisplayName();
        for (Skill skill : skills) {
            if (item.getType() == skill.triggerMaterial && skill.triggerName.equals(displayName)) return skill;
        }
        return null;
    }

    private void useSkill(Player p, Skill skill) {
        UUID uuid = p.getUniqueId();
        LastUse last = lastUse.get(uuid);
        if (last != null && System.currentTimeMillis() - last.usedAt < skill.cooldownSeconds * 1000L) {
            Messages.send(p, "skill.cooldown");
            return;
        }

        PlayerData pd = PlayerManager.getInstance().getPlayerData(uuid);
        if (pd == null) return;
        if (pd.getCurrentMp() < skill.mana) {
            Messages.send(p, "skill.no-mana");
            return;
        }

        lastUse.put(uuid, new LastUse(System.currentTimeMillis(), skill.cooldownSeconds * 1000L));
        pd.setCurrentMp(pd.getCurrentMp() - skill.mana);

        switch (skill.type) {
            case FIREBALL:
                p.launchProjectile(org.bukkit.entity.Fireball.class);
                break;
            case EARTHQUAKE:
                playSound(p.getLocation(), skill);
                new BukkitRunnable() {
                    int i = 0;
                    @Override
                    public void run() {
                        if (i >= skill.pulses) {
                            this.cancel();
                            return;
                        }
                        for (Entity e : p.getNearbyEntities(skill.radius, skill.radius, skill.radius)) {
                            if (e instanceof LivingEntity && e != p) {
                                ((LivingEntity) e).damage(pd.getStr() * skill.damagePerStat + skill.damageBase, p);
                                e.setVelocity(new Vector(0, skill.launchHeight, 0));
                            }
                        }
                        i++;
                    }
                }.runTaskTimer(plugin, 0L, skill.intervalTicks);
                break;
            case HOLY_BOMB:
                Location holyLoc = p.getLocation();
                if (skill.particle != null) {
                    p.getWorld().spawnParticle(skill.particle, holyLoc, skill.particleCount,
                            skill.particleSpread, skill.particleSpread, skill.particleSpread, 0.1);
                }
                playSound(holyLoc, skill);
                for (Entity e : p.getNearbyEntities(skill.radius, skill.radius, skill.radius)) {
                    if (e instanceof LivingEntity && e != p) {
                        ((LivingEntity) e).damage(pd.getIntel() * skill.damagePerStat + skill.damageBase, p);
                    }
                }
                break;
            case HEAL:
                int heal = (int) (pd.getIntel() * skill.healPerIntel + skill.healBase);
                pd.setCurrentHp(pd.getCurrentHp() + heal);
                Messages.send(p, "skill.heal", "amount", heal);
                if (skill.regenTicks > 0) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, skill.regenTicks, skill.regenAmplifier));
                }
                break;
        }
    }

    private void playSound(Location loc, Skill skill) {
        if (skill.sound != null) loc.getWorld().playSound(loc, skill.sound, skill.soundVolume, 1.0f);
    }

    /** 사이드바에 보여 줄 남은 쿨타임(초). 쿨타임 중이 아니면 0 */
    public int getRemainCooldown(UUID uuid) {
        LastUse last = lastUse.get(uuid);
        if (last == null) return 0;
        long remainMs = last.cooldownMs - (System.currentTimeMillis() - last.usedAt);
        if (remainMs <= 0) return 0;
        return (int) Math.ceil(remainMs / 1000.0);
    }
}
