package kr.ishs.raibit.ishsMinecraftRpg.npc;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcConfigTest {

    private static YamlConfiguration yaml(String text) {
        try {
            YamlConfiguration c = new YamlConfiguration();
            c.loadFromString(text);
            return c;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void parsesFieldsAndDefaults() {
        Map<String, NpcConfig.Npc> npcs = NpcConfig.parse(yaml("""
                npcs:
                  teacher:
                    name: "&6선생님"
                    skin: Notch
                    location: {x: 1.5, y: -61, z: 2, yaw: 90}
                """), null);
        NpcConfig.Npc t = npcs.get("teacher");
        assertEquals("&6선생님", t.name);
        assertEquals("Notch", t.skin);
        assertEquals(1.5, t.x);
        assertEquals(90f, t.yaw);
        assertTrue(t.lookAtPlayer, "look-at-player 기본값은 true");
        assertEquals("teacher", t.conversation, "conversation 기본값은 NPC 키");
        assertEquals("", t.world);
    }

    @Test
    void offsetMovesEveryNpc() {
        Map<String, NpcConfig.Npc> npcs = NpcConfig.parse(yaml("""
                offset: {x: 10, y: 0, z: -5}
                npcs:
                  a:
                    location: {x: 1, y: 2, z: 3}
                """), null);
        assertEquals(11, npcs.get("a").x);
        assertEquals(2, npcs.get("a").y);
        assertEquals(-2, npcs.get("a").z);
    }

    @Test
    void invalidEntriesAreSkipped() {
        Map<String, NpcConfig.Npc> npcs = NpcConfig.parse(yaml("""
                npcs:
                  BadKey:
                    location: {x: 1, y: 2, z: 3}
                  no_location:
                    name: "x"
                  ok:
                    location: {x: 1, y: 2, z: 3}
                """), null);
        assertEquals(java.util.Set.of("ok"), npcs.keySet());
    }

    @Test
    void betonQuestBindingsLinkKeyToCitizensIdAndConversation() {
        Map<String, NpcConfig.Npc> defs = NpcConfig.parse(yaml("""
                npcs:
                  principal:
                    location: {x: 1, y: 2, z: 3}
                    conversation: principal_talk
                """), null);
        Map<String, Integer> ids = new LinkedHashMap<>();
        ids.put("principal", 7);
        String out = NpcConfig.betonQuestBindings(ids, defs);

        YamlConfiguration parsed = yaml(out);
        assertEquals("citizens 7", parsed.getString("npcs.principal"));
        assertEquals("principal_talk", parsed.getString("npc_conversations.principal"));
        assertFalse(out.isBlank());
    }

    @Test
    void typeDefaultsToPlayerAndAcceptsAnimals() {
        Map<String, NpcConfig.Npc> npcs = NpcConfig.parse(yaml("""
                npcs:
                  person:
                    location: {x: 1, y: 2, z: 3}
                  cat:
                    type: cat
                    variant: SIAMESE
                    sitting: true
                    location: {x: 1, y: 2, z: 3}
                """), null);
        assertEquals(org.bukkit.entity.EntityType.PLAYER, npcs.get("person").type);
        assertEquals(org.bukkit.entity.EntityType.CAT, npcs.get("cat").type);
        assertEquals("SIAMESE", npcs.get("cat").variant);
        assertTrue(npcs.get("cat").sitting);
        assertFalse(npcs.get("person").sitting);
    }

    @Test
    void unusableTypeFallsBackToPlayer() {
        Map<String, NpcConfig.Npc> npcs = NpcConfig.parse(yaml("""
                npcs:
                  typo:
                    type: CATT
                    location: {x: 1, y: 2, z: 3}
                  arrow:
                    type: ARROW
                    location: {x: 1, y: 2, z: 3}
                """), null);
        assertEquals(org.bukkit.entity.EntityType.PLAYER, npcs.get("typo").type);
        assertEquals(org.bukkit.entity.EntityType.PLAYER, npcs.get("arrow").type, "살아 있는 생물이 아니면 쓸 수 없다");
    }
}
