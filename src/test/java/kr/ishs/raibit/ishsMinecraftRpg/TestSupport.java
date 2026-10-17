package kr.ishs.raibit.ishsMinecraftRpg;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** 테스트에서 configs/ 와 소스 파일을 읽는 도우미 */
public final class TestSupport {

    private TestSupport() {}

    /** 프로젝트 루트 기준 configs/ishsMinecraftRpg 아래 YAML 파일 */
    public static YamlConfiguration plugin(String fileName) {
        return yaml(Path.of("configs", "ishsMinecraftRpg", fileName));
    }

    public static YamlConfiguration yaml(Path path) {
        try {
            String text = Files.readString(path, StandardCharsets.UTF_8);
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(text);
            return config;
        } catch (Exception e) {
            throw new IllegalStateException(path + " 을(를) 읽을 수 없습니다: " + e.getMessage(), e);
        }
    }

    /** configs/BetonQuest/QuestPackages/default 아래 파일 */
    public static File betonQuest(String relative) {
        return Path.of("configs", "BetonQuest", "QuestPackages", "default").resolve(relative).toFile();
    }

    /** src/main/java 아래의 모든 .java 파일 */
    public static List<Path> javaSources() throws IOException {
        try (Stream<Path> walk = Files.walk(Path.of("src", "main", "java"))) {
            return walk.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
        }
    }
}
