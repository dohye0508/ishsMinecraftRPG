package kr.ishs.raibit.ishsMinecraftRpg.npc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcFileTest {

    private static final String FILE = """
            # 맨 위 주석은 남아야 한다
            offset:
              x: 0

            npcs:
              principal:
                name: "A"
                location: {x: 1, y: 2, z: 3, yaw: 0}
                conversation: principal

              # 교감 메모
              vice:
                name: "B"
                location: {x: 4, y: 5, z: 6, yaw: 0}
                conversation: vice
            """;

    @Test
    void setLocationChangesOnlyThatNpcAndKeepsComments() {
        String out = NpcFile.setLocation(FILE, "vice", "{x: 9, y: 9, z: 9, yaw: 90}");
        assertTrue(out.contains("# 맨 위 주석은 남아야 한다"));
        assertTrue(out.contains("# 교감 메모"));
        assertTrue(out.contains("location: {x: 1, y: 2, z: 3, yaw: 0}"), "다른 NPC 좌표는 그대로");
        assertTrue(out.contains("location: {x: 9, y: 9, z: 9, yaw: 90}"));
        assertTrue(!out.contains("x: 4"));
    }

    @Test
    void unknownKeyReturnsNull() {
        assertNull(NpcFile.setLocation(FILE, "nobody", "{x: 1}"));
        assertNull(NpcFile.remove(FILE, "nobody"));
        assertNull(NpcFile.setLocation(FILE, "offset", "{x: 1}"), "npcs: 밖의 키는 NPC 가 아님");
    }

    @Test
    void removeDeletesOnlyThatBlock() {
        String out = NpcFile.remove(FILE, "principal");
        assertTrue(!out.contains("principal"));
        assertTrue(out.contains("vice:") && out.contains("# 교감 메모"));
        assertTrue(out.contains("offset:"));
    }

    @Test
    void removingLastNpcKeepsFileValid() {
        String out = NpcFile.remove(NpcFile.remove(FILE, "principal"), "vice");
        assertTrue(out.contains("npcs:"));
        assertTrue(!out.contains("vice:"));
    }

    @Test
    void worksWithWindowsLineEndings() {
        String crlf = FILE.replace("\n", "\r\n");
        String out = NpcFile.setLocation(crlf, "principal", "{x: 7, y: 7, z: 7, yaw: 0}");
        assertTrue(out.contains("location: {x: 7, y: 7, z: 7, yaw: 0}\r\n"));
        assertEquals(crlf.split("\r\n").length, out.split("\r\n").length);
    }
}
