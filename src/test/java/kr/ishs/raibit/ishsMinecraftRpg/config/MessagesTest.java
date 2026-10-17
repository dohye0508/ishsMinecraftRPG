package kr.ishs.raibit.ishsMinecraftRpg.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessagesTest {

    private final List<String> warnings = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        Logger log = Logger.getLogger("test-messages-" + System.nanoTime());
        log.setUseParentHandlers(false);
        log.addHandler(new Handler() {
            @Override public void publish(LogRecord record) { warnings.add(record.getMessage()); }
            @Override public void flush() {}
            @Override public void close() {}
        });

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(String.join("\n",
                "single: \"&aHello {name}, level {level}!\"",
                "multi:",
                "  - \"&7line one {name}\"",
                "  - \"\"",
                "  - \"line three\"",
                "plain: \"no placeholders\""));
        Messages.load(yaml, log);
    }

    @Test
    void replacesNamedPlaceholdersAndColors() {
        assertEquals("§aHello Dohye, level 5!", Messages.get("single", "name", "Dohye", "level", 5));
    }

    @Test
    void leavesUnknownPlaceholdersUntouched() {
        assertEquals("§aHello Dohye, level {level}!", Messages.get("single", "name", "Dohye"));
    }

    @Test
    void valuesWithAmpersandAreNotTreatedAsColorCodes() {
        assertEquals("§aHello &cX, level 1!", Messages.get("single", "name", "&cX", "level", 1));
    }

    @Test
    void listKeepsLinesAndGetJoinsThemWithNewlines() {
        assertEquals(List.of("§7line one A", "", "line three"), Messages.list("multi", "name", "A"));
        assertEquals("§7line one A\n\nline three", Messages.get("multi", "name", "A"));
    }

    @Test
    void singleValueIsAOneLineList() {
        assertEquals(List.of("no placeholders"), Messages.list("plain"));
    }

    @Test
    void missingKeyReturnsTheKeyAndWarnsOnlyOnce() {
        assertEquals("nope.key", Messages.get("nope.key"));
        assertEquals("nope.key", Messages.get("nope.key"));
        assertEquals(1, warnings.size());
    }

    @Test
    void nullValueBecomesEmpty() {
        assertEquals("§aHello , level 2!", Messages.get("single", "name", null, "level", 2));
    }
}
