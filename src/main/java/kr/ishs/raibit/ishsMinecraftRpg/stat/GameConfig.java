package kr.ishs.raibit.ishsMinecraftRpg.stat;

import org.bukkit.configuration.file.FileConfiguration;

public class GameConfig {
    public static int startStr = 1;
    public static int startDex = 1;
    public static int startIntel = 1;
    public static int startHp = 1;
    public static int startWis = 1;
    public static int startStatPoints = 5;

    public static void load(FileConfiguration config) {
        startStr = config.getInt("starting-stats.str", 1);
        startDex = config.getInt("starting-stats.dex", 1);
        startIntel = config.getInt("starting-stats.intel", 1);
        startHp = config.getInt("starting-stats.hp", 1);
        startWis = config.getInt("starting-stats.wis", 1);
        startStatPoints = config.getInt("starting-stat-points", 5);
    }
}
