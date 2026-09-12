package kr.ishs.raibit.ishsMinecraftRpg.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class CustomItems {
    public static final String DEMERIT_CARD = "demerit_card";
    public static final String WANIL_STAFF = "wanil_staff";
    public static final String JASMINE_FLOWER = "jasmine_flower";
    public static final String SCHEMA = "schema";
    public static final String CHANGBONG_SPEAR = "changbong_spear";
    public static final String CHANGBONG_ROD = "changbong_rod";

    public static final List<String> ALL_IDS = Arrays.asList(
            DEMERIT_CARD, WANIL_STAFF, JASMINE_FLOWER, SCHEMA, CHANGBONG_SPEAR, CHANGBONG_ROD);

    private static NamespacedKey key;

    public static void init(Plugin plugin) {
        key = new NamespacedKey(plugin, "ishsrpg_item");
    }

    public static NamespacedKey getKey() {
        return key;
    }

    public static ItemStack create(String id) {
        if (id == null) return null;
        switch (id.toLowerCase()) {
            case DEMERIT_CARD:
                return build(Material.PAPER, ChatColor.RED + "왕사감의 벌점카드",
                        Arrays.asList(
                                ChatColor.GRAY + "왕사감 선생님의 위압적인 손길이 담긴 카드.",
                                ChatColor.GRAY + "이 카드에 찍히는 자, 정신이 아득해진다.",
                                "",
                                ChatColor.DARK_RED + "공격 적중 시 대상에게 약화 효과 부여"
                        ), DEMERIT_CARD, true);
            case WANIL_STAFF:
                return build(Material.STICK, ChatColor.AQUA + "완일쌤의 지팡이",
                        Arrays.asList(
                                ChatColor.GRAY + "평범해 보이지만 완일쌤의 손때가 묻은 무기.",
                                ChatColor.GRAY + "특별한 힘은 없지만 그 자체로 믿음직스럽다."
                        ), WANIL_STAFF, true);
            case JASMINE_FLOWER:
                return build(Material.AZURE_BLUET, ChatColor.LIGHT_PURPLE + "자스민 꽃",
                        Arrays.asList(
                                ChatColor.GRAY + "으깨어 상처에 바르면 통증이 가라앉는다.",
                                "",
                                ChatColor.LIGHT_PURPLE + "우클릭 시 체력 회복 (소모품)"
                        ), JASMINE_FLOWER, false);
            case SCHEMA:
                return build(Material.BOOK, ChatColor.GOLD + "스키마",
                        Arrays.asList(
                                ChatColor.GRAY + "정신을 각성시키는 비전의 공식.",
                                "",
                                ChatColor.GOLD + "우클릭 시 일정 시간 무기 강화 및 이동속도 증가 (소모품)"
                        ), SCHEMA, false);
            case CHANGBONG_SPEAR:
                return build(Material.TRIDENT, ChatColor.DARK_AQUA + "이창봉도 창이다",
                        Arrays.asList(
                                ChatColor.GRAY + "이창봉 선생님의 이름처럼, 이 창도 창이다.",
                                "",
                                ChatColor.DARK_AQUA + "공격 적중 시 대상을 강하게 찔러 밀쳐낸다"
                        ), CHANGBONG_SPEAR, true);
            case CHANGBONG_ROD:
                return build(Material.BLAZE_ROD, ChatColor.GOLD + "이창봉도 봉이다",
                        Arrays.asList(
                                ChatColor.GRAY + "이창봉 선생님의 이름처럼, 이 봉도 봉이다.",
                                "",
                                ChatColor.GOLD + "공격 적중 시 대상을 후려쳐 하늘로 띄운다"
                        ), CHANGBONG_ROD, true);
            default:
                return null;
        }
    }

    private static ItemStack build(Material material, String name, List<String> lore, String id, boolean unbreakable) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.setUnbreakable(unbreakable);
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
