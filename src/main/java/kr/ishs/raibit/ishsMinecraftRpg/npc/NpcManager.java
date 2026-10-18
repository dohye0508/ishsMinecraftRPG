package kr.ishs.raibit.ishsMinecraftRpg.npc;

import kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg;
import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.trait.versioned.CatTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kr.ishs.raibit.ishsMinecraftRpg.config.Constants;

/**
 * npcs.yml 의 내용대로 Citizens NPC 를 만들고, 옮기고, 지운다. (YAML 이 기준이고 게임 안 NPC 는 결과물이다)
 *
 * <ul>
 *   <li>이 플러그인이 만든 NPC 는 Citizens 데이터에 키({@value #DATA_KEY})가 적혀 있어서 번호가 바뀌어도 찾을 수 있다.</li>
 *   <li>npcs.yml 에서 빠진 NPC 는 게임에서도 삭제된다. 키가 없는 NPC(직접 만든 것)는 건드리지 않는다.</li>
 *   <li>BetonQuest 에는 NPC 키와 Citizens 번호의 연결을 파일({@value #BQ_FILE})로 만들어 알려 준다.</li>
 * </ul>
 */
public final class NpcManager {

    static final String DATA_KEY = "ishsrpg-key";
    static final String BQ_FILE = "npcs_generated.yml";

    private final IshsMinecraftRpg plugin;

    public NpcManager(IshsMinecraftRpg plugin) {
        this.plugin = plugin;
    }

    /** @return 반영된 NPC 수. Citizens 가 없으면 -1 */
    public int sync() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            plugin.getLogger().warning("[npcs.yml] Citizens 플러그인이 없어 NPC 를 만들지 못했습니다.");
            return -1;
        }

        Map<String, NpcConfig.Npc> defs = NpcConfig.all();
        NPCRegistry registry = CitizensAPI.getNPCRegistry();

        // 1. 이 플러그인이 만든 NPC 를 키별로 모으고, 목록에서 빠졌거나 중복된 것은 삭제한다
        Map<String, NPC> existing = new HashMap<>();
        List<NPC> stale = new ArrayList<>();
        for (NPC npc : registry) {
            Object key = npc.data().get(DATA_KEY);
            if (key == null) continue;
            if (!defs.containsKey(String.valueOf(key)) || existing.putIfAbsent(String.valueOf(key), npc) != null) {
                stale.add(npc);
            }
        }
        for (NPC npc : stale) npc.destroy();

        // 2. 목록의 NPC 를 만들거나 갱신한다
        Map<String, Integer> ids = new LinkedHashMap<>();
        for (NpcConfig.Npc def : defs.values()) {
            World world = def.world.isEmpty() ? defaultWorld() : Bukkit.getWorld(def.world);
            if (world == null) {
                plugin.getLogger().warning("[npcs.yml] '" + def.key + "' 의 월드 '" + def.world + "' 를 찾을 수 없어 건너뜁니다.");
                continue;
            }
            Location loc = new Location(world, def.x, def.y, def.z, def.yaw, def.pitch);

            NPC npc = existing.get(def.key);
            if (npc == null) {
                npc = registry.createNPC(def.type, Messages.color(def.name));
                npc.data().setPersistent(DATA_KEY, def.key);
            } else {
                npc.setName(Messages.color(def.name));
                if (npc.getOrAddTrait(MobType.class).getType() != def.type) {
                    npc.setBukkitEntityType(def.type); // 형태가 바뀌었으면 다시 만들어진다
                }
            }

            // 사람이 아닌 NPC(고양이 등)도 사람 NPC 와 같은 이름표(검은 배경)로 보이게 한다
            npc.setAlwaysUseNameHologram(true);

            if (!npc.isSpawned()) {
                npc.spawn(loc);
            } else if (moved(npc.getStoredLocation(), loc)) {
                npc.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }

            npc.getOrAddTrait(LookClose.class).lookClose(def.lookAtPlayer);
            if (def.type == EntityType.PLAYER && !def.skin.isEmpty()) {
                npc.getOrAddTrait(SkinTrait.class).setSkinName(def.skin);
            }
            if (def.type == EntityType.CAT) applyCat(npc, def);
            ids.put(def.key, npc.getId());
        }

        writeBetonQuestBindings(ids, defs);
        return ids.size();
    }

    /** 고양이 NPC 의 무늬와 자세. variant 는 Cat.Type 이름(예: BRITISH_SHORTHAIR) */
    private void applyCat(NPC npc, NpcConfig.Npc def) {
        CatTrait trait = npc.getOrAddTrait(CatTrait.class);
        if (!def.variant.isEmpty()) {
            Cat.Type variant = Constants.find(Cat.Type.class, def.variant);
            if (variant != null) {
                trait.setType(variant);
            } else {
                plugin.getLogger().warning("[npcs.yml] '" + def.key + "' 의 variant '" + def.variant + "' 를 찾을 수 없어 무시합니다. (예: TABBY, BLACK, SIAMESE, BRITISH_SHORTHAIR, CALICO)");
            }
        }
        trait.setSitting(def.sitting);
    }

    private World defaultWorld() {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.NORMAL) return w;
        }
        return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
    }

    private static boolean moved(Location a, Location b) {
        return a == null || a.getWorld() != b.getWorld() || a.distanceSquared(b) > 0.0001
                || Math.abs(a.getYaw() - b.getYaw()) > 0.5f;
    }

    /** BetonQuest 가 NPC 키로 Citizens NPC 를 찾을 수 있게 연결 파일을 쓰고, 내용이 바뀌었으면 BetonQuest 를 다시 읽게 한다. */
    private void writeBetonQuestBindings(Map<String, Integer> ids, Map<String, NpcConfig.Npc> defs) {
        File dir = new File(plugin.getDataFolder().getParentFile(), "BetonQuest/QuestPackages/default");
        if (!dir.isDirectory()) return;

        File file = new File(dir, BQ_FILE);
        String content = NpcConfig.betonQuestBindings(ids, defs);
        try {
            String old = file.exists() ? Files.readString(file.toPath(), StandardCharsets.UTF_8) : "";
            if (old.equals(content)) return;
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.getLogger().warning("[npcs.yml] BetonQuest 연결 파일을 쓰지 못했습니다: " + e.getMessage());
            return;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("BetonQuest")) {
            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bq reload"));
        }
    }
}
