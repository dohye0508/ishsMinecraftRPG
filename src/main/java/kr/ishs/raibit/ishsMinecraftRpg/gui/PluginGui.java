package kr.ishs.raibit.ishsMinecraftRpg.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * 이 플러그인이 여는 GUI 를 구분하는 표식.
 * GUI 제목(문구)은 messages.yml 에서 바뀔 수 있으므로, 제목 글자가 아니라 이 표식으로 어떤 GUI 인지 판별한다.
 */
public final class PluginGui implements InventoryHolder {

    public enum Type {
        STATS,
        CLUB,
        ACHIEVEMENTS
    }

    private final Type type;
    private Inventory inventory;

    private PluginGui(Type type) {
        this.type = type;
    }

    /** 표식이 붙은 GUI 를 만듭니다. */
    @SuppressWarnings("deprecation")
    public static Inventory create(Type type, int size, String title) {
        PluginGui holder = new PluginGui(type);
        holder.inventory = Bukkit.createInventory(holder, size, title);
        return holder.inventory;
    }

    /** 이 GUI 가 해당 종류인지 확인합니다. */
    public static boolean is(Inventory inventory, Type type) {
        return inventory != null && inventory.getHolder() instanceof PluginGui && ((PluginGui) inventory.getHolder()).type == type;
    }

    /** 이벤트가 열린 GUI(위쪽 인벤토리) 기준으로 해당 종류인지 확인합니다. */
    public static boolean is(InventoryEvent event, Type type) {
        return is(event.getView().getTopInventory(), type);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Type getType() {
        return type;
    }

    /** 플레이어가 지금 이 종류의 GUI 를 열고 있는지 */
    public static boolean hasOpen(HumanEntity player, Type type) {
        return is(player.getOpenInventory().getTopInventory(), type);
    }
}
