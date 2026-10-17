package kr.ishs.raibit.ishsMinecraftRpg.npc;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Citizens 가 저장된 NPC 를 모두 불러온 직후에 NPC 를 맞춘다.
 * 그보다 먼저 맞추면 이미 있는 NPC 를 못 보고 새로 만들어서, 재시작할 때마다 NPC 가 중복된다.
 */
public class NpcStartupListener implements Listener {

    private final IshsMinecraftRpg plugin;

    public NpcStartupListener(IshsMinecraftRpg plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCitizensLoaded(CitizensEnableEvent event) {
        plugin.reloadNpcs();
    }
}
