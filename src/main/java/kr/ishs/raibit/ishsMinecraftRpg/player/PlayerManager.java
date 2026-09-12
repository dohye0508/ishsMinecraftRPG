package kr.ishs.raibit.ishsMinecraftRpg.player;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    private static final PlayerManager instance = new PlayerManager();
    private final Map<UUID, PlayerData> playerDataMap;

    private PlayerManager() {
        this.playerDataMap = new HashMap<>();
    }

    public static PlayerManager getInstance() {
        return instance;
    }

    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerDataMap.containsKey(uuid)) {
            PlayerData data = new PlayerData(uuid);
            kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg plugin = kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg.getPlugin(kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg.class);
            java.io.File file = new java.io.File(plugin.getDataFolder(), "userdata/" + uuid.toString() + ".yml");
            if (file.exists()) {
                org.bukkit.configuration.file.YamlConfiguration config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
                data.loadFromYaml(config);
            }
            playerDataMap.put(uuid, data);
        }
    }

    public void unloadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (playerDataMap.containsKey(uuid)) {
            PlayerData data = playerDataMap.get(uuid);
            savePlayerData(uuid, data);
            playerDataMap.remove(uuid);
        }
    }
    
    public void savePlayerData(UUID uuid, PlayerData data) {
        try {
            kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg plugin = kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg.getPlugin(kr.ishs.raibit.ishsMinecraftRpg.IshsMinecraftRpg.class);
            java.io.File dir = new java.io.File(plugin.getDataFolder(), "userdata");
            if (!dir.exists()) dir.mkdirs();
            java.io.File file = new java.io.File(dir, uuid.toString() + ".yml");
            org.bukkit.configuration.file.YamlConfiguration config = new org.bukkit.configuration.file.YamlConfiguration();
            data.saveToYaml(config);
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void saveAll() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            savePlayerData(entry.getKey(), entry.getValue());
        }
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.get(player.getUniqueId());
    }
    
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }
}
