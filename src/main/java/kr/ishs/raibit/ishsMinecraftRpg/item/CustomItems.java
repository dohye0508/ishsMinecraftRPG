package kr.ishs.raibit.ishsMinecraftRpg.item;

import kr.ishs.raibit.ishsMinecraftRpg.config.Constants;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 커스텀 아이템. 이름, 재료, 설명과 효과(effects)는 모두 items.yml에서 읽는다.
 * (기획팀이 코드 없이 수정 가능) 효과 발동은 CustomItemListener 가 담당한다.
 */
public class CustomItems {

    private static NamespacedKey key;
    private static Logger logger;

    /** items.yml에서 읽은 아이템 정의 (ID는 소문자) */
    private static final Map<String, Def> DEFS = new LinkedHashMap<>();

    private static class Def {
        final Material material;
        final String name;
        final List<String> lore;
        final boolean unbreakable;
        final List<ItemEffect> effects;

        Def(Material material, String name, List<String> lore, boolean unbreakable, List<ItemEffect> effects) {
            this.material = material;
            this.name = name;
            this.lore = lore;
            this.unbreakable = unbreakable;
            this.effects = effects;
        }
    }

    public static void init(Plugin plugin) {
        key = new NamespacedKey(plugin, "ishsrpg_item");
        logger = plugin.getLogger();
    }

    public static NamespacedKey getKey() {
        return key;
    }

    /** items.yml의 items 항목을 읽어 아이템 정의를 갱신합니다. */
    public static void load(FileConfiguration config) {
        DEFS.clear();
        ConfigurationSection items = config.getConfigurationSection("items");
        if (items == null) {
            if (logger != null) logger.warning("[items.yml] items 항목이 없습니다. 커스텀 아이템이 없습니다.");
            return;
        }

        for (String id : items.getKeys(false)) {
            ConfigurationSection section = items.getConfigurationSection(id);
            if (section == null) continue;

            String materialName = section.getString("material", "");
            Material material = Material.matchMaterial(materialName);
            if (material == null || Constants.isAir(material)) {
                if (logger != null) {
                    logger.warning("[items.yml] '" + id + "' 아이템의 material '" + materialName
                            + "' 을(를) 찾을 수 없어 건너뜁니다. (영어 대문자 재료 이름인지 확인하세요)");
                }
                continue;
            }

            String name = color(section.getString("name", id));
            List<String> lore = new ArrayList<>();
            for (String line : section.getStringList("lore")) lore.add(color(line));

            List<ItemEffect> effects = new ArrayList<>();
            for (Map<?, ?> rawEffect : section.getMapList("effects")) {
                ItemEffect effect = ItemEffect.parse(id, rawEffect, logger);
                if (effect != null) effects.add(effect);
            }

            DEFS.put(id.toLowerCase(), new Def(material, name, lore, section.getBoolean("unbreakable", false), effects));
        }
    }

    /** 아이템 ID에 설정된 효과 목록 (없으면 빈 목록) */
    public static List<ItemEffect> getEffects(String id) {
        if (id == null) return new ArrayList<>();
        Def def = DEFS.get(id.toLowerCase());
        return def == null ? new ArrayList<>() : def.effects;
    }

    /** 지급 가능한 아이템 ID 목록 */
    public static List<String> getIds() {
        return new ArrayList<>(DEFS.keySet());
    }

    public static ItemStack create(String id) {
        if (id == null) return null;
        Def def = DEFS.get(id.toLowerCase());
        if (def == null) return null;
        return build(def, id.toLowerCase());
    }

    @SuppressWarnings("deprecation")
    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    @SuppressWarnings("deprecation")
    private static ItemStack build(Def def, String id) {
        ItemStack item = new ItemStack(def.material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(def.name);
        meta.setLore(new ArrayList<>(def.lore));
        meta.setUnbreakable(def.unbreakable);
        // 일반 바닐라 아이템과 구별되도록 마법부여 반짝임 테두리 효과를 부여 (실제 마법부여는 없음)
        meta.setEnchantmentGlintOverride(true);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    public static String getId(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || item.getItemMeta() == null || key == null) return null;
        return item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }
}
